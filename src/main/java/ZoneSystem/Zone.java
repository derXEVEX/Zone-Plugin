package ZoneSystem;
import javax.xml.stream.Location;
import java.util.UUID;

class Zone {
    private UUID owner;
    private int x1, z1, x2, z2;

    public Zone(UUID owner, int x1, int z1, int x2, int z2) {
        this.owner = owner;
        this.x1 = Math.min(x1, x2);
        this.z1 = Math.min(z1, z2);
        this.x2 = Math.max(x1, x2);
        this.z2 = Math.max(z1, z2);
    }

    public UUID getOwner() {
        return owner;
    }

    public boolean isOwner(UUID playerUUID) {
        return owner.equals(playerUUID);
    }

    public boolean isInside(int x, int z) {
        return x >= x1 && x <= x2 && z >= z1 && z <= z2;
    }

    public boolean overlaps(Zone other) {
        return this.x1 <= other.x2 && this.x2 >= other.x1 && this.z1 <= other.z2 && this.z2 >= other.z1;
    }

}