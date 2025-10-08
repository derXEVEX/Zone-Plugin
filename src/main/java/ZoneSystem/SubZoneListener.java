package ZoneSystem;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.UUID;

public class SubZoneListener implements Listener {

    private final HashMap<UUID, SubZoneSelection> selections = new HashMap<>();

    @EventHandler
    public void onPlayerClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() != Material.WOODEN_HOE) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName() || !meta.getDisplayName().equals("§6SubZone-Tool")) return;

        event.setCancelled(true);

        if (event.getClickedBlock() == null) return;

        selections.putIfAbsent(playerId, new SubZoneSelection());
        SubZoneSelection selection = selections.get(playerId);

        int x = event.getClickedBlock().getX();
        int y = event.getClickedBlock().getY();
        int z = event.getClickedBlock().getZ();

        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            selection.setPosition1(x, y, z);
            player.sendMessage("§aFirst position Set: §f" + x + ", " + y + ", " + z);
            if (selection.isComplete()) {
                ZonePlugin.getInstance().getSubZoneVisualizer().startVisualization(player, selection);
            }
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            selection.setPosition2(x, y, z);
            player.sendMessage("§aSecond  position set§f" + x + ", " + y + ", " + z);
            if (selection.isComplete()) {
                ZonePlugin.getInstance().getSubZoneVisualizer().startVisualization(player, selection);
            }
        }
    }


    public void clearSelection(UUID playerId) {
        selections.remove(playerId);
    }

    public SubZoneSelection getSelection(UUID playerId) {
        return selections.get(playerId);
    }


    public static ItemStack getSubZoneTool() {
        ItemStack tool = new ItemStack(Material.WOODEN_HOE);
        ItemMeta meta = tool.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6SubZone-Tool");
            tool.setItemMeta(meta);
        }
        return tool;
    }
}
