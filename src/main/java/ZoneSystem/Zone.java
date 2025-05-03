package ZoneSystem;

import lombok.Getter;

import java.util.List;
import java.util.UUID;

public class Zone {
    private final UUID ownerUUID;
    private final String ownerName;
    private final int x1, y1, z1;
    private final int x2, y2, z2;
    private final int zoneNumber;

    public Zone(UUID ownerUUID, String ownerName, int x1, int z1, int x2, int z2) {
        this.ownerUUID = ownerUUID;
        this.ownerName = ownerName;
        this.x1 = Math.min(x1, x2);
        this.z1 = Math.min(z1, z2);
        this.x2 = Math.max(x1, x2);
        this.z2 = Math.max(z1, z2);
        this.y1 = -63;
        this.y2 = 319;

        ZoneManager zoneManager = ZonePlugin.getInstance().getZoneManager();
        List<Zone> playerZones = zoneManager.getZonesForPlayer(ownerUUID);
        this.zoneNumber = playerZones.size() + 1;
    }

    public Zone(UUID ownerUUID, String ownerName, int x1, int z1, int x2, int z2, int zoneNumber) {
        this.ownerUUID = ownerUUID;
        this.ownerName = ownerName;
        this.x1 = Math.min(x1, x2);
        this.z1 = Math.min(z1, z2);
        this.x2 = Math.max(x1, x2);
        this.z2 = Math.max(z1, z2);
        this.y1 = -63;
        this.y2 = 319;
        this.zoneNumber = zoneNumber;
    }

    public int getZoneNumber() {
        return zoneNumber;
    }



    public String getOwnerName() {
        return ownerName;
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public boolean isOwner(UUID playerUUID) {
        return ownerUUID.equals(playerUUID);
    }

    public boolean isInside(int x, int y, int z) {
        return x >= x1 && x <= x2 &&
                z >= z1 && z <= z2 &&
                y >= y1 && y <= y2;
    }

    public boolean overlaps(Zone other) {
        return this.x1 <= other.x2 && this.x2 >= other.x1 && this.z1 <= other.z2 && this.z2 >= other.z1;
    }

    public int getMinX() {
        return x1;
    }

    public int getMaxX() {
        return x2;
    }

    public int getMinZ() {
        return z1;
    }

    public int getMaxZ() {
        return z2;
    }

    public int getY1() {
        return this.y1;
    }

    public int getY2() {
        return this.y2;
    }




}