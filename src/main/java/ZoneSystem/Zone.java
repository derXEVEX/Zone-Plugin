package ZoneSystem;

import lombok.Getter;

import java.util.UUID;

public class Zone {

    private final UUID ownerUUID;
    private final String ownerName;
    private final int x1, z1, x2, z2;

    public Zone(UUID ownerUUID, String ownerName, int x1, int z1, int x2, int z2) {
        this.ownerUUID = ownerUUID;
        this.ownerName = ownerName;
        this.x1 = Math.min(x1, x2);
        this.z1 = Math.min(z1, z2);
        this.x2 = Math.max(x1, x2);
        this.z2 = Math.max(z1, z2);
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

    public boolean isInside(int x, int z) {
        return x >= x1 && x <= x2 && z >= z1 && z <= z2;
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
}