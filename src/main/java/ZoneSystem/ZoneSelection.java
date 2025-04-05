package ZoneSystem;

import java.util.UUID;

public class ZoneSelection {

    private Integer x1, z1, x2, z2;

    public void setPosition1(int x, int z) {
        this.x1 = x;
        this.z1 = z;
    }

    public void setPosition2(int x, int z) {
        this.x2 = x;
        this.z2 = z;
    }

    public boolean isComplete() {
        return x1 != null && x2 != null && z1 != null && z2 != null;
    }

    public Zone toZone(UUID owner, String name) {
        if (!isComplete()) return null;
        return new Zone(owner, name, x1, z1, x2, z2);
    }
}
