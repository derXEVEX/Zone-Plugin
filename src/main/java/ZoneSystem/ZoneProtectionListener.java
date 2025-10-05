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
            event.setCancelled(true);
            player.sendActionBar("§cNo permissions for this zone!");
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Zone zone = zoneManager.getZoneAt(event.getBlock().getLocation());

        if (zone != null && !zone.isOwner(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendActionBar("§cNo permissions for this zone!");
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;

        Player player = event.getPlayer();
        Zone zone = zoneManager.getZoneAt(event.getClickedBlock().getLocation());

        if (zone != null && !zone.isOwner(player.getUniqueId())) {
            switch (event.getClickedBlock().getType()) {
                case CHEST, BARREL, FURNACE, CRAFTING_TABLE, ANVIL,
                     ENCHANTING_TABLE, BREWING_STAND, ENDER_CHEST,
                     LEVER, STONE_BUTTON, OAK_BUTTON, BIRCH_BUTTON,
                     SPRUCE_BUTTON, JUNGLE_BUTTON, ACACIA_BUTTON,
                     DARK_OAK_BUTTON, CRIMSON_BUTTON, WARPED_BUTTON,
                     OAK_DOOR, BIRCH_DOOR, SPRUCE_DOOR, JUNGLE_DOOR,
                     ACACIA_DOOR, DARK_OAK_DOOR, CRIMSON_DOOR, WARPED_DOOR,
                     OAK_TRAPDOOR, BIRCH_TRAPDOOR, SPRUCE_TRAPDOOR,
                     JUNGLE_TRAPDOOR, ACACIA_TRAPDOOR, DARK_OAK_TRAPDOOR,
                     CRIMSON_TRAPDOOR, WARPED_TRAPDOOR, OAK_FENCE_GATE,
                     BIRCH_FENCE_GATE, SPRUCE_FENCE_GATE, JUNGLE_FENCE_GATE,
                     ACACIA_FENCE_GATE, DARK_OAK_FENCE_GATE, CRIMSON_FENCE_GATE,
                     WARPED_FENCE_GATE -> {
                    event.setCancelled(true);
                    player.sendActionBar("§cNo permissions for this zone!");
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;

        Zone zone = zoneManager.getZoneAt(event.getEntity().getLocation());

        if (zone != null && !zone.isOwner(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendActionBar("§cNo permissions for this zone!");
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
