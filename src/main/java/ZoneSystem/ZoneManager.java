package ZoneSystem;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;


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
            Type zoneListType = new TypeToken<List<Zone>>() {}.getType();
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

    public boolean isInZone(Player player) {
        return zones.stream().anyMatch(zone -> zone.isInside(player.getLocation().getBlockX(), player.getLocation().getBlockZ()));
    }

    public boolean canBuild(Player player, int x, int z) {
        return zones.stream().anyMatch(zone -> zone.isOwner(player.getUniqueId()) && zone.isInside(x, z));
    }


    public Zone getPlayerZone(UUID playerId) {
        return zones.stream()
                .filter(zone -> zone.getOwner().equals(playerId))
                .findFirst()
                .orElse(null);
    }


    public int getZoneCountForPlayer(UUID playerId) {
        return (int) zones.stream().filter(zone -> zone.getOwner().equals(playerId)).count();
    }

    public void resetZones() {
        zones.clear();
        saveZones();
    }

    public void removeZone(Zone zone) {
        zones.remove(zone);
        saveZones();
    }

    public Optional<Zone> getPlayerZoneOptional(UUID playerId) {
        return zones.stream()
                .filter(zone -> zone.getOwner().equals(playerId))
                .findFirst();
    }

}
