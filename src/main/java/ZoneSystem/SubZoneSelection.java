package ZoneSystem;

import java.util.UUID;

public class SubZoneSelection {
    private Integer x1, y1, z1, x2, y2, z2;

    public Integer getX1() { return x1; }
    public Integer getY1() { return y1; }
    public Integer getZ1() { return z1; }
    public Integer getX2() { return x2; }
    public Integer getY2() { return y2; }
    public Integer getZ2() { return z2; }


    public void setPosition1(int x, int y, int z) {
        this.x1 = x;
        this.y1 = y;
        this.z1 = z;
    }

    public void setPosition2(int x, int y, int z) {
        this.x2 = x;
        this.y2 = y;
        this.z2 = z;
    }

    public boolean isComplete() {
        return x1 != null && y1 != null && z1 != null &&
                x2 != null && y2 != null && z2 != null;
    }

    public SubZone toSubZone(UUID owner, String name, int mainZoneNumber, int subZoneNumber) {
        if (!isComplete()) return null;
        return new SubZone(owner, name, mainZoneNumber, subZoneNumber, x1, y1, z1, x2, y2, z2);
    }
}
