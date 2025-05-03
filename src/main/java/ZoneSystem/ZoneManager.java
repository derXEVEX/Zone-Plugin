package ZoneSystem;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.OfflinePlayer;
import java.util.Arrays;
import com.sk89q.worldedit.extent.clipboard.*;
import com.sk89q.worldedit.extent.clipboard.io.*;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;

public class ZoneManager {
    private final File zoneFile;
    private final Gson gson;
    private final List<Zone> zones;


    public ZoneManager() {
        this.zoneFile = new File(ZonePlugin.getInstance().getDataFolder(), "zones.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.zones = new ArrayList<>();


        if (!zoneFile.exists()) {
            try {
                zoneFile.getParentFile().mkdirs();
                zoneFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        loadZones();
    }

    public void loadZones() {
        try (FileReader reader = new FileReader(zoneFile)) {
            Type zoneListType = new TypeToken<List<Zone>>() {
            }.getType();
            List<Zone> loadedZones = gson.fromJson(reader, zoneListType);
            if (loadedZones != null) {
                zones.addAll(loadedZones);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveZones() {
        try (FileWriter writer = new FileWriter(zoneFile)) {
            gson.toJson(zones, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean canCreateZone(Zone newZone) {
        return zones.stream().noneMatch(existingZone -> existingZone.overlaps(newZone));
    }

    public void addZone(Zone zone) {
        zones.add(zone);
        saveZones();
    }


    public boolean canBuild(Player player, int x, int y, int z) {
        return zones.stream().anyMatch(zone ->
                zone.isOwner(player.getUniqueId()) && zone.isInside(x, y, z)
        );
    }

    public Zone getPlayerZone(UUID playerId) {
        return zones.stream()
                .filter(zone -> zone.getOwnerUUID().equals(playerId))
                .findFirst()
                .orElse(null);
    }


    public int getZoneCountForPlayer(UUID playerId) {
        return (int) zones.stream().filter(zone -> zone.getOwnerUUID().equals(playerId)).count();
    }

    public void resetZones() {
        zones.clear();
        saveZones();
    }

    public void removeZone(Zone zone) {
        zones.remove(zone);
        saveZones();
    }

    public Zone getZoneAt(Location loc) {
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();

        return zones.stream()
                .filter(zone -> zone.isInside(x, y, z))
                .findFirst()
                .orElse(null);
    }

    public List<Zone> getZonesForPlayer(UUID playerId) {
        return zones.stream()
                .filter(zone -> zone.getOwnerUUID().equals(playerId))
                .toList();
    }


    public void saveZoneBackup(Zone zone) {
        try {
            ensureBackupFolderExists();
        
        File backupFolder = new File(ZonePlugin.getInstance().getDataFolder(), "backups");
        File schematicFile = new File(backupFolder,
                zone.getOwnerUUID() + "_zone" + zone.getZoneNumber() + "_backup.schem");

        World world = Bukkit.getWorld("world");
        if (world == null) {
            ZonePlugin.getInstance().getLogger().warning("Could not find world to create backup!");
            return;
        }

        // Logging hinzufügen
        ZonePlugin.getInstance().getLogger().info("Creating backup for zone " + zone.getZoneNumber() + 
            " owned by " + zone.getOwnerName());
        ZonePlugin.getInstance().getLogger().info("Backup file: " + schematicFile.getAbsolutePath());

        BlockVector3 min = BlockVector3.at(zone.getMinX(), zone.getY1(), zone.getMinZ());
        BlockVector3 max = BlockVector3.at(zone.getMaxX(), zone.getY2(), zone.getMaxZ());
        CuboidRegion region = new CuboidRegion(min, max);

        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);

        try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(world))) {
            ForwardExtentCopy copy = new ForwardExtentCopy(
                    editSession, region, clipboard, region.getMinimumPoint()
            );
            copy.setCopyingEntities(true);
            Operations.complete(copy);
        }

        try (FileOutputStream fos = new FileOutputStream(schematicFile);
             ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(fos)) {
            writer.write(clipboard);
            ZonePlugin.getInstance().getLogger().info("Backup successfully created!");
        }

    } catch (Exception e) {
        e.printStackTrace();
        ZonePlugin.getInstance().getLogger().severe("Error creating zone backup: " + e.getMessage());
    }
}

    public void restoreZoneBackup(Zone zone) {
    try {
        ensureBackupFolderExists();
        
        File backupFolder = new File(ZonePlugin.getInstance().getDataFolder(), "backups");
        File schematicFile = new File(backupFolder,
            zone.getOwnerUUID() + "_zone" + zone.getZoneNumber() + "_backup.schem");
            
        if (!schematicFile.exists()) {
            ZonePlugin.getInstance().getLogger().warning("No backup found for zone at: " + schematicFile.getAbsolutePath());
            return;
        }

        ZonePlugin.getInstance().getLogger().info("Restoring backup from: " + schematicFile.getAbsolutePath());

        World world = Bukkit.getWorld("world");
        if (world == null) {
            ZonePlugin.getInstance().getLogger().warning("World not found for restore!");
            return;
        }

        Clipboard clipboard;
        try (FileInputStream fis = new FileInputStream(schematicFile);
             ClipboardReader reader = BuiltInClipboardFormat.SPONGE_SCHEMATIC.getReader(fis)) {
            clipboard = reader.read();
        }

        try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(world))) {
            Operation operation = new ClipboardHolder(clipboard)
                    .createPaste(editSession)
                    .to(BlockVector3.at(zone.getMinX(), zone.getY1(), zone.getMinZ()))
                    .ignoreAirBlocks(false)
                    .build();

            Operations.complete(operation);
            ZonePlugin.getInstance().getLogger().info("Zone successfully restored!");
        }

    } catch (Exception e) {
        e.printStackTrace();
        ZonePlugin.getInstance().getLogger().severe("Error restoring zone backup: " + e.getMessage());
    }
}

    public Zone getZoneByPlayerAndNumber(String playerName, int zoneNumber) {
        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            Optional<UUID> playerUUID = Arrays.stream(Bukkit.getOfflinePlayers())
                    .filter(p -> p.getName() != null && p.getName().equalsIgnoreCase(playerName))
                    .map(OfflinePlayer::getUniqueId)
                    .findFirst();

            if (playerUUID.isEmpty()) {
                return null;
            }

            return zones.stream()
                    .filter(z -> z.getOwnerUUID().equals(playerUUID.get()) && z.getZoneNumber() == zoneNumber)
                    .findFirst()
                    .orElse(null);
        }

        return zones.stream()
                .filter(z -> z.getOwnerUUID().equals(player.getUniqueId()) && z.getZoneNumber() == zoneNumber)
                .findFirst()
                .orElse(null);
    } private void ensureBackupFolderExists() {
        File backupFolder = new File(ZonePlugin.getInstance().getDataFolder(), "backups");
        if (!backupFolder.exists()) {
            backupFolder.mkdirs();
        }
    }

}