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
import java.util.HashMap;


public class ZoneManager {
    private final File zoneFile;
    private final Gson gson;
    private final List<Zone> zones;
    private final List<SubZone> subZones = new ArrayList<>();
    private final HashMap<UUID, Zone> activeSubZoneCreations = new HashMap<>();
    private final HashMap<String, ZonePermissionEntry> zonePermissions = new HashMap<>();

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
            ZoneData data = gson.fromJson(reader, ZoneData.class);

            zones.clear();
            subZones.clear();
            zonePermissions.clear();

            if (data != null) {
                if (data.zones != null) zones.addAll(data.zones);
                if (data.subZones != null) subZones.addAll(data.subZones);
                if (data.permissions != null) zonePermissions.putAll(data.permissions);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveZones() {
        try (FileWriter writer = new FileWriter(zoneFile)) {
            ZoneData data = new ZoneData(zones, subZones, zonePermissions);
            gson.toJson(data, writer);
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

    public int getTotalAreaForPlayer(UUID playerId) {
        return zones.stream()
                .filter(zone -> zone.getOwnerUUID().equals(playerId))
                .mapToInt(Zone::getArea)
                .sum();
    }

    public boolean canPlayerCreateZone(UUID playerId, Zone newZone) {
        List<Zone> playerZones = getZonesForPlayer(playerId);

        if (playerZones.size() >= ZoneLimits.MAX_ZONES_PER_PLAYER) {
            return false;
        }

        int currentArea = getTotalAreaForPlayer(playerId);
        int newZoneArea = newZone.getArea();

        return (currentArea + newZoneArea) <= ZoneLimits.MAX_TOTAL_AREA;
    }

    public void addSubZone(SubZone subZone) {
        subZones.add(subZone);
        saveZones();
    }

    public int getNextSubZoneNumber(UUID ownerUUID, int mainZoneNumber) {
        return (int) subZones.stream()
                .filter(sz -> sz.getOwnerUUID().equals(ownerUUID) && sz.getMainZoneNumber() == mainZoneNumber)
                .count() + 1;
    }

    public boolean isSubZoneWithinMainZone(SubZone subZone, Zone mainZone) {
        return subZone.getMinX() >= mainZone.getMinX() &&
                subZone.getMaxX() <= mainZone.getMaxX() &&
                subZone.getMinZ() >= mainZone.getMinZ() &&
                subZone.getMaxZ() <= mainZone.getMaxZ() &&
                subZone.getMinY() >= mainZone.getY1() &&
                subZone.getMaxY() <= mainZone.getY2();
    }

    public void setActiveSubZoneCreation(UUID playerId, Zone mainZone) {
        activeSubZoneCreations.put(playerId, mainZone);
    }

    public Zone getActiveSubZoneCreation(UUID playerId) {
        return activeSubZoneCreations.get(playerId);
    }

    public void clearActiveSubZoneCreation(UUID playerId) {
        activeSubZoneCreations.remove(playerId);
    }

    public SubZone getSubZoneAt(Location loc) {
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();

        return subZones.stream()
                .filter(sz -> sz.isInside(x, y, z))
                .findFirst()
                .orElse(null);
    }

    public SubZone getSubZoneByNumbers(String playerName, int mainZoneNumber, int subZoneNumber) {
        Player player = Bukkit.getPlayer(playerName);
        UUID targetUUID = null;

        if (player != null) {
            targetUUID = player.getUniqueId();
        } else {
            Optional<UUID> offlineUUID = Arrays.stream(Bukkit.getOfflinePlayers())
                    .filter(p -> p.getName() != null && p.getName().equalsIgnoreCase(playerName))
                    .map(OfflinePlayer::getUniqueId)
                    .findFirst();

            if (offlineUUID.isPresent()) {
                targetUUID = offlineUUID.get();
            }
        }

        if (targetUUID == null) return null;

        UUID finalTargetUUID = targetUUID;
        return subZones.stream()
                .filter(sz -> sz.getOwnerUUID().equals(finalTargetUUID) &&
                        sz.getMainZoneNumber() == mainZoneNumber &&
                        sz.getSubZoneNumber() == subZoneNumber)
                .findFirst()
                .orElse(null);
    }

    public void removeSubZone(SubZone subZone) {
        subZones.remove(subZone);
        saveZones();
    }

    public int getSubZoneCountForZone(UUID ownerUUID, int mainZoneNumber) {
        return (int) subZones.stream()
                .filter(sz -> sz.getOwnerUUID().equals(ownerUUID) &&
                        sz.getMainZoneNumber() == mainZoneNumber)
                .count();
    }


    public boolean canCreateSubZone(SubZone newSubZone) {
        return subZones.stream()
                .noneMatch(existingSubZone -> subZonesOverlap(existingSubZone, newSubZone));
    }

    private boolean subZonesOverlap(SubZone sz1, SubZone sz2) {
        return sz1.getMinX() <= sz2.getMaxX() && sz1.getMaxX() >= sz2.getMinX() &&
                sz1.getMinY() <= sz2.getMaxY() && sz1.getMaxY() >= sz2.getMinY() &&
                sz1.getMinZ() <= sz2.getMaxZ() && sz1.getMaxZ() >= sz2.getMinZ();
    }

    public void removeZoneWithSubZones(Zone zone) {
        List<SubZone> zonesToRemove = subZones.stream()
                .filter(sz -> sz.getOwnerUUID().equals(zone.getOwnerUUID()) &&
                        sz.getMainZoneNumber() == zone.getZoneNumber())
                .toList();

        subZones.removeAll(zonesToRemove);

        zones.remove(zone);
        saveZones();
    }


    public void setZonePermission(UUID zoneOwner, int mainZone, Integer subZone, UUID user, ZonePermission permission, boolean value) {
        String key = buildPermissionKey(zoneOwner, mainZone, subZone);
        zonePermissions.putIfAbsent(key, new ZonePermissionEntry(zoneOwner, mainZone, subZone));

        if (user == null) {
            zonePermissions.get(key).setGlobalPermission(permission, value);
        } else {
            zonePermissions.get(key).setUserPermission(user, permission, value);
        }
        saveZones();
    }

    public boolean hasZonePermission(UUID user, Location loc, ZonePermission permission) {
        SubZone subZone = getSubZoneAt(loc);
        Zone mainZone = getZoneAt(loc);

        if (mainZone == null) return false;
        if (mainZone.isOwner(user)) return true;

        // Prüfe Subzone zuerst
        if (subZone != null) {
            String subKey = buildPermissionKey(mainZone.getOwnerUUID(), mainZone.getZoneNumber(), subZone.getSubZoneNumber());
            if (zonePermissions.containsKey(subKey)) {
                ZonePermissionEntry subEntry = zonePermissions.get(subKey);

                if (subEntry.hasExplicitUserPermission(user, permission)) {
                    return subEntry.getUserPermission(user, permission);
                }

                if (subEntry.hasExplicitGlobalPermission(permission)) {
                    Boolean globalPerm = subEntry.getGlobalPermission(permission);
                    if (globalPerm != null) return globalPerm;
                }
            }
        }

        String mainKey = buildPermissionKey(mainZone.getOwnerUUID(), mainZone.getZoneNumber(), null);
        if (zonePermissions.containsKey(mainKey)) {
            return zonePermissions.get(mainKey).hasPermission(user, permission);
        }

        return false;
    }


    public ZonePermissionEntry getPermissionEntry(UUID zoneOwner, int mainZone, Integer subZone) {
        String key = buildPermissionKey(zoneOwner, mainZone, subZone);
        return zonePermissions.get(key);
    }

    private String buildPermissionKey(UUID owner, int mainZone, Integer subZone) {
        return subZone == null ? owner + "#" + mainZone : owner + "#" + mainZone + "." + subZone;
    }


    private static class ZoneData {
        List<Zone> zones;
        List<SubZone> subZones;
        HashMap<String, ZonePermissionEntry> permissions;

        ZoneData(List<Zone> zones, List<SubZone> subZones, HashMap<String, ZonePermissionEntry> permissions) {
            this.zones = zones;
            this.subZones = subZones;
            this.permissions = permissions;
        }
    }

    public List<Zone> getAllZones() {
        return new ArrayList<>(zones);
    }







}