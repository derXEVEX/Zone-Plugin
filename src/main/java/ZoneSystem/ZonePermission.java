package ZoneSystem;

public enum ZonePermission {
    // Basis-Berechtigungen
    BUILD("build", "Blöcke platzieren und abbauen"),
    INTERACT_BLOCKS("interact_blocks", "Türen, Knöpfe, Hebel, Druckplatten benutzen"),
    INTERACT_CONTAINERS("interact_containers", "Truhen, Öfen, Fässer öffnen"),

    // Entity-Interaktionen
    INTERACT_ENTITIES("interact_entities", "Generelle Entity-Interaktion"),
    DAMAGE_ANIMALS("damage_animals", "Tiere angreifen"),
    DAMAGE_MONSTERS("damage_monsters", "Monster angreifen"),
    DAMAGE_VILLAGERS("damage_villagers", "Dorfbewohner angreifen"),
    LEASH_ENTITIES("leash_entities", "Allows leashing animals"),

    // Spezifische Tier-Interaktionen
    FEED_ANIMALS("feed_animals", "Tiere füttern"),
    BREED_ANIMALS("breed_animals", "Tiere züchten"),
    SHEAR_ANIMALS("shear_animals", "Schafe scheren"),
    MILK_ANIMALS("milk_animals", "Kühe/Mooshrooms melken"),

    // Villager & Trading
    VILLAGER_TRADE("villager_trade", "Mit Dorfbewohnern handeln"),

    // Fahrzeuge
    PLACE_VEHICLES("place_vehicles", "Minecarts, Boote platzieren"),
    BREAK_VEHICLES("break_vehicles", "Minecarts, Boote abbauen"),
    RIDE_VEHICLES("ride_vehicles", "Minecarts, Boote fahren"),

    // Item-Frames & Rüstungsständer
    INTERACT_ITEM_FRAMES("interact_item_frames", "Item Frames benutzen"),
    INTERACT_ARMOR_STANDS("interact_armor_stands", "Rüstungsständer bearbeiten"),

    // Redstone
    USE_REDSTONE("use_redstone", "Redstone-Komponenten benutzen"),

    // Admin
    ADMIN("admin", "Subzonen erstellen und Permissions verwalten");

    private final String key;
    private final String description;

    ZonePermission(String key, String description) {
        this.key = key;
        this.description = description;
    }

    public String getKey() { return key; }
    public String getDescription() { return description; }

    public static ZonePermission fromString(String key) {
        for (ZonePermission perm : values()) {
            if (perm.key.equalsIgnoreCase(key)) {
                return perm;
            }
        }
        return null;
    }

    // Kategorien für bessere Übersicht
    public static ZonePermission[] getBuildPermissions() {
        return new ZonePermission[]{BUILD, INTERACT_BLOCKS, INTERACT_CONTAINERS};
    }

    public static ZonePermission[] getAnimalPermissions() {
        return new ZonePermission[]{DAMAGE_ANIMALS, FEED_ANIMALS, BREED_ANIMALS, SHEAR_ANIMALS, MILK_ANIMALS};
    }

    public static ZonePermission[] getVehiclePermissions() {
        return new ZonePermission[]{PLACE_VEHICLES, BREAK_VEHICLES, RIDE_VEHICLES};
    }
}
