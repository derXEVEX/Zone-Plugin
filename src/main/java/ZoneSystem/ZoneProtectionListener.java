package ZoneSystem;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.PlayerLeashEntityEvent;

import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.Cancellable;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;



public class ZoneProtectionListener implements Listener {

    private final ZoneManager zoneManager;

    public ZoneProtectionListener(ZoneManager zoneManager) {
        this.zoneManager = zoneManager;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Zone zone = zoneManager.getZoneAt(event.getBlock().getLocation());

        if (zone != null && !zone.isOwner(player.getUniqueId())) {
            if (!zoneManager.hasZonePermission(player.getUniqueId(), event.getBlock().getLocation(), ZonePermission.BUILD)) {
                event.setCancelled(true);
                player.sendActionBar("§cKeine Berechtigung zum Abbauen!");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Zone zone = zoneManager.getZoneAt(event.getBlock().getLocation());

        if (zone != null && !zone.isOwner(player.getUniqueId())) {
            if (!zoneManager.hasZonePermission(player.getUniqueId(), event.getBlock().getLocation(), ZonePermission.BUILD)) {
                event.setCancelled(true);
                player.sendActionBar("§cKeine Berechtigung zum Platzieren!");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if (block == null || event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Zone zone = zoneManager.getZoneAt(block.getLocation());
        if (zone == null || zone.isOwner(player.getUniqueId())) return;

        Material type = block.getType();

        // Container-Check
        if (isContainer(type)) {
            if (!zoneManager.hasZonePermission(player.getUniqueId(), block.getLocation(), ZonePermission.INTERACT_CONTAINERS)) {
                event.setCancelled(true);
                player.sendActionBar("§cKeine Berechtigung für Container!");
                return;
            }
        }

        // Redstone-Check
        if (isRedstone(type)) {
            if (!zoneManager.hasZonePermission(player.getUniqueId(), block.getLocation(), ZonePermission.USE_REDSTONE)) {
                event.setCancelled(true);
                player.sendActionBar("§cKeine Berechtigung für Redstone!");
                return;
            }
        }

        // Interactable Blocks (Türen, Knöpfe, etc.)
        if (isInteractable(type)) {
            if (!zoneManager.hasZonePermission(player.getUniqueId(), block.getLocation(), ZonePermission.INTERACT_BLOCKS)) {
                event.setCancelled(true);
                player.sendActionBar("§cKeine Berechtigung für Interaktion!");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();
        Zone zone = zoneManager.getZoneAt(entity.getLocation());

        if (zone == null || zone.isOwner(player.getUniqueId())) return;

        // Villager Trading
        if (entity instanceof Villager || entity instanceof WanderingTrader) {
            if (!zoneManager.hasZonePermission(player.getUniqueId(), entity.getLocation(), ZonePermission.VILLAGER_TRADE)) {
                event.setCancelled(true);
                player.sendActionBar("§cKeine Berechtigung zum Handeln!");
                return;
            }
        }

        // Animal Interactions
        if (entity instanceof Animals) {
            Material item = player.getInventory().getItemInMainHand().getType();

            // Füttern
            if (isFeedItem(item, entity)) {
                if (!zoneManager.hasZonePermission(player.getUniqueId(), entity.getLocation(), ZonePermission.FEED_ANIMALS)) {
                    event.setCancelled(true);
                    player.sendActionBar("§cKeine Berechtigung zum Füttern!");
                    return;
                }
            }

            // Scheren
            if (item == Material.SHEARS && entity instanceof Sheep) {
                if (!zoneManager.hasZonePermission(player.getUniqueId(), entity.getLocation(), ZonePermission.SHEAR_ANIMALS)) {
                    event.setCancelled(true);
                    player.sendActionBar("§cKeine Berechtigung zum Scheren!");
                    return;
                }
            }

            // Melken
            if (item == Material.BUCKET && (entity instanceof Cow || entity instanceof MushroomCow)) {
                if (!zoneManager.hasZonePermission(player.getUniqueId(), entity.getLocation(), ZonePermission.MILK_ANIMALS)) {
                    event.setCancelled(true);
                    player.sendActionBar("§cKeine Berechtigung zum Melken!");
                    return;
                }
            }
        }

        // Item Frames
        if (entity instanceof ItemFrame) {
            if (!zoneManager.hasZonePermission(player.getUniqueId(), entity.getLocation(), ZonePermission.INTERACT_ITEM_FRAMES)) {
                event.setCancelled(true);
                player.sendActionBar("§cKeine Berechtigung für Item Frames!");
            }
        }

        if (entity instanceof ArmorStand) {
            if (!zoneManager.hasZonePermission(player.getUniqueId(), entity.getLocation(), ZonePermission.INTERACT_ARMOR_STANDS)) {
                event.setCancelled(true);
                player.sendActionBar("§cKeine Berechtigung für Rüstungsständer!");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;

        Zone zone = zoneManager.getZoneAt(event.getEntity().getLocation());
        if (zone == null || zone.isOwner(player.getUniqueId())) return;

        Entity victim = event.getEntity();


        if (victim instanceof Animals) {
            if (!zoneManager.hasZonePermission(player.getUniqueId(), victim.getLocation(), ZonePermission.DAMAGE_ANIMALS)) {
                event.setCancelled(true);
                player.sendActionBar("§cKeine Berechtigung Tiere anzugreifen!");
                return;
            }
        }


        if (victim instanceof Villager || victim instanceof WanderingTrader) {
            if (!zoneManager.hasZonePermission(player.getUniqueId(), victim.getLocation(), ZonePermission.DAMAGE_VILLAGERS)) {
                event.setCancelled(true);
                player.sendActionBar("§cKeine Berechtigung Dorfbewohner anzugreifen!");
                return;
            }
        }


        if (victim instanceof Monster) {
            if (!zoneManager.hasZonePermission(player.getUniqueId(), victim.getLocation(), ZonePermission.DAMAGE_MONSTERS)) {
                event.setCancelled(true);
                player.sendActionBar("§cKeine Berechtigung Monster anzugreifen!");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerLeashEntity(PlayerLeashEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getEntity();
        Zone zone = zoneManager.getZoneAt(entity.getLocation());

        if (zone == null || zone.isOwner(player.getUniqueId())) return;

        if (!zoneManager.hasZonePermission(player.getUniqueId(), entity.getLocation(), ZonePermission.LEASH_ENTITIES)) {
            event.setCancelled(true);
            player.sendActionBar("§cKeine Berechtigung Tiere anzuleinen!");
        }
    }


    @EventHandler(priority = EventPriority.HIGH)
    public void onVehiclePlace(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Material item = event.getItem() != null ? event.getItem().getType() : Material.AIR;
        if (!isVehicleItem(item)) return;

        Player player = event.getPlayer();
        Zone zone = zoneManager.getZoneAt(event.getClickedBlock().getLocation());

        if (zone != null && !zone.isOwner(player.getUniqueId())) {
            if (!zoneManager.hasZonePermission(player.getUniqueId(), event.getClickedBlock().getLocation(), ZonePermission.PLACE_VEHICLES)) {
                event.setCancelled(true);
                player.sendActionBar("§cKeine Berechtigung Fahrzeuge zu platzieren!");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onVehicleDamage(VehicleDamageEvent event) {
        if (!(event.getAttacker() instanceof Player player)) return;

        Zone zone = zoneManager.getZoneAt(event.getVehicle().getLocation());
        if (zone != null && !zone.isOwner(player.getUniqueId())) {
            if (!zoneManager.hasZonePermission(player.getUniqueId(), event.getVehicle().getLocation(), ZonePermission.BREAK_VEHICLES)) {
                event.setCancelled(true);
                player.sendActionBar("§cKeine Berechtigung Fahrzeuge abzubauen!");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onVehicleEnter(VehicleEnterEvent event) {
        if (!(event.getEntered() instanceof Player player)) return;

        Zone zone = zoneManager.getZoneAt(event.getVehicle().getLocation());
        if (zone != null && !zone.isOwner(player.getUniqueId())) {
            if (!zoneManager.hasZonePermission(player.getUniqueId(), event.getVehicle().getLocation(), ZonePermission.RIDE_VEHICLES)) {
                event.setCancelled(true);
                player.sendActionBar("§cKeine Berechtigung Fahrzeuge zu benutzen!");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onHangingBreak(HangingBreakByEntityEvent event) {
        if (!(event.getRemover() instanceof Player player)) return;

        Zone zone = zoneManager.getZoneAt(event.getEntity().getLocation());
        if (zone != null && !zone.isOwner(player.getUniqueId())) {
            if (!zoneManager.hasZonePermission(player.getUniqueId(), event.getEntity().getLocation(), ZonePermission.INTERACT_ITEM_FRAMES)) {
                event.setCancelled(true);
                player.sendActionBar("§cKeine Berechtigung!");
            }
        }
    }


    private boolean isContainer(Material type) {
        return type == Material.CHEST || type == Material.TRAPPED_CHEST ||
                type == Material.BARREL || type == Material.FURNACE ||
                type == Material.BLAST_FURNACE || type == Material.SMOKER ||
                type == Material.HOPPER || type == Material.DROPPER ||
                type == Material.DISPENSER || type == Material.SHULKER_BOX ||
                type.name().contains("SHULKER_BOX");
    }

    private boolean isInteractable(Material type) {
        return type.name().contains("DOOR") || type.name().contains("GATE") ||
                type.name().contains("TRAPDOOR") || type == Material.LEVER ||
                type == Material.STONE_BUTTON || type == Material.BIRCH_BUTTON;
    }

    private boolean isRedstone(Material type) {
        return type == Material.LEVER || type.name().contains("BUTTON") ||
                type.name().contains("PRESSURE_PLATE") || type == Material.REPEATER ||
                type == Material.COMPARATOR || type == Material.REDSTONE_WIRE;
    }

    private boolean isVehicleItem(Material type) {
        return type.name().contains("MINECART") || type.name().contains("BOAT");
    }

    private boolean isFeedItem(Material item, Entity entity) {
        if (entity instanceof Sheep || entity instanceof Cow) {
            return item == Material.WHEAT;
        }
        if (entity instanceof Pig) {
            return item == Material.CARROT || item == Material.POTATO;
        }
        if (entity instanceof Chicken) {
            return item == Material.WHEAT_SEEDS;
        }
        return false;
    }
}
