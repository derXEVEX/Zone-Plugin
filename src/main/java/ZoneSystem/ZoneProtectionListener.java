package ZoneSystem;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Villager;
import org.bukkit.entity.WanderingTrader;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;


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
                player.sendActionBar("§cNo permissions for this zone!");
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
                player.sendActionBar("§cNo permissions for this zone!");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();
        Zone zone = zoneManager.getZoneAt(entity.getLocation());

        if (zone != null && !zone.isOwner(player.getUniqueId())) {
            if (entity instanceof Villager || entity instanceof WanderingTrader) {
                if (!zoneManager.hasZonePermission(player.getUniqueId(), entity.getLocation(), ZonePermission.VILLAGER_TRADE)) {
                    event.setCancelled(true);
                    player.sendActionBar("§cNo permission to trade with villagers!");
                    return;
                }
            }

            if (entity instanceof Animals) {
                if (!zoneManager.hasZonePermission(player.getUniqueId(), entity.getLocation(), ZonePermission.ANIMALS_RIGHT_CLICK)) {
                    event.setCancelled(true);
                    player.sendActionBar("§cNo permission to interact with animals!");
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        Zone zone = zoneManager.getZoneAt(event.getEntity().getLocation());

        if (zone != null && !zone.isOwner(player.getUniqueId())) {
            if (event.getEntity() instanceof Animals) {
                if (!zoneManager.hasZonePermission(player.getUniqueId(), event.getEntity().getLocation(), ZonePermission.ANIMALS_LEFT_CLICK)) {
                    event.setCancelled(true);
                    player.sendActionBar("§cNo permission to attack animals!");
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onVehicleEnter(VehicleEnterEvent event) {
        if (!(event.getEntered() instanceof Player player)) return;

        Vehicle vehicle = event.getVehicle();
        if (vehicle.getType().name().contains("MINECART")) {
            Zone zone = zoneManager.getZoneAt(vehicle.getLocation());
            if (zone != null && !zone.isOwner(player.getUniqueId())) {
                if (!zoneManager.hasZonePermission(player.getUniqueId(), vehicle.getLocation(), ZonePermission.MINECART_RIDE)) {
                    event.setCancelled(true);
                    player.sendActionBar("§cNo permission to ride minecarts!");
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        Zone zone = zoneManager.getZoneAt(event.getBlock().getLocation());

        if (zone != null && !zone.isOwner(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendActionBar("§cNo permissions for this zone!");
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBucketFill(PlayerBucketFillEvent event) {
        Player player = event.getPlayer();
        Zone zone = zoneManager.getZoneAt(event.getBlock().getLocation());

        if (zone != null && !zone.isOwner(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendActionBar("§cNo permissions for this zone!");
        }
    }
}
