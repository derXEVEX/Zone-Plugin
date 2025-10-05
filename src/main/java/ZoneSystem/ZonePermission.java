package ZoneSystem;

public enum ZonePermission {
    BUILD("build", "Place and break blocks"),
    INTERACT_BLOCKS("interact_blocks", "Use chests, doors, buttons, etc."),
    INTERACT_ENTITIES("interact_entities", "Interact with all entities"),
    ANIMALS_RIGHT_CLICK("animals_right_click", "Right-click animals (feed, shear, etc.)"),
    ANIMALS_LEFT_CLICK("animals_left_click", "Attack animals"),
    VILLAGER_TRADE("villager_trade", "Trade with villagers"),
    MINECART_RIDE("minecart_ride", "Ride minecarts"),
    ADMIN("admin", "Create subzones and manage permissions");

    private final String key;
    private final String description;

    ZonePermission(String key, String description) {
        this.key = key;
        this.description = description;
    }

    public String getKey() {
        return key;
    }

    public String getDescription() {
        return description;
    }

    public static ZonePermission fromString(String key) {
        for (ZonePermission perm : values()) {
            if (perm.key.equalsIgnoreCase(key)) {
                return perm;
            }
        }
        return null;
    }
}
