package ZoneSystem;

import java.util.UUID;

public class SubZone {
    private final UUID ownerUUID;
    private final String ownerName;
    private final int mainZoneNumber;
    private final int subZoneNumber;
    private final int x1, y1, z1;
    private final int x2, y2, z2;

    public SubZone(UUID ownerUUID, String ownerName, int mainZoneNumber, int subZoneNumber,
                   int x1, int y1, int z1, int x2, int y2, int z2) {
        this.ownerUUID = ownerUUID;
        this.ownerName = ownerName;
        this.mainZoneNumber = mainZoneNumber;
        this.subZoneNumber = subZoneNumber;
        this.x1 = Math.min(x1, x2);
        this.y1 = Math.min(y1, y2);
        this.z1 = Math.min(z1, z2);
        this.x2 = Math.max(x1, x2);
        this.y2 = Math.max(y1, y2);
        this.z2 = Math.max(z1, z2);
    }

    public UUID getOwnerUUID() { return ownerUUID; }
    public String getOwnerName() { return ownerName; }
    public int getMainZoneNumber() { return mainZoneNumber; }
    public int getSubZoneNumber() { return subZoneNumber; }
    public String getFullZoneName() { return mainZoneNumber + "." + subZoneNumber; }

    public boolean isInside(int x, int y, int z) {
        return x >= x1 && x <= x2 &&
                y >= y1 && y <= y2 &&
                z >= z1 && z <= z2;
    }

    public boolean isOwner(UUID playerUUID) {
        return ownerUUID.equals(playerUUID);
    }

    public int getMinX() { return x1; }
    public int getMinY() { return y1; }
    public int getMinZ() { return z1; }
    public int getMaxX() { return x2; }
    public int getMaxY() { return y2; }
    public int getMaxZ() { return z2; }
}
