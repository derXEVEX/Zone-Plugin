package ZoneSystem;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ZonePermissionEntry {
    private final UUID zoneOwner;
    private final int mainZoneNumber;
    private final Integer subZoneNumber;
    private final Map<UUID, Map<ZonePermission, Boolean>> userPermissions;
    private final Map<ZonePermission, Boolean> globalPermissions;

    public ZonePermissionEntry(UUID zoneOwner, int mainZoneNumber, Integer subZoneNumber) {
        this.zoneOwner = zoneOwner;
        this.mainZoneNumber = mainZoneNumber;
        this.subZoneNumber = subZoneNumber;
        this.userPermissions = new HashMap<>();
        this.globalPermissions = new HashMap<>();
    }

    public void setUserPermission(UUID user, ZonePermission permission, boolean value) {
        userPermissions.putIfAbsent(user, new HashMap<>());
        userPermissions.get(user).put(permission, value);
    }

    public void setGlobalPermission(ZonePermission permission, boolean value) {
        globalPermissions.put(permission, value);
    }

    public Boolean getUserPermission(UUID user, ZonePermission permission) {
        if (userPermissions.containsKey(user) && userPermissions.get(user).containsKey(permission)) {
            return userPermissions.get(user).get(permission);
        }
        return globalPermissions.get(permission);
    }

    public boolean hasPermission(UUID user, ZonePermission permission) {
        if (zoneOwner.equals(user)) return true;

        Boolean userPerm = getUserPermission(user, permission);
        if (userPerm != null) return userPerm;

        return globalPermissions.getOrDefault(permission, false);
    }

    public Map<UUID, Map<ZonePermission, Boolean>> getUserPermissions() {
        return userPermissions;
    }

    public Map<ZonePermission, Boolean> getGlobalPermissions() {
        return globalPermissions;
    }

    public String getZoneIdentifier() {
        return subZoneNumber == null ? mainZoneNumber + "" : mainZoneNumber + "." + subZoneNumber;
    }
}
