package ZoneSystem;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.UUID;

public class ZoneListener implements Listener {
    private final HashMap<UUID, ZoneSelection> selections = new HashMap<>();
    private final HashMap<UUID, BossBar> activeBossBars = new HashMap<>();

    @EventHandler
    public void onPlayerClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (player.getInventory().getItemInMainHand().getType() != Material.STICK) return;

        selections.putIfAbsent(playerId, new ZoneSelection());
        ZoneSelection selection = selections.get(playerId);

        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            selection.setPosition1(event.getClickedBlock().getX(), event.getClickedBlock().getZ());
            player.sendMessage("§aErste Position gesetzt!");
        }

        else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            selection.setPosition2(event.getClickedBlock().getX(), event.getClickedBlock().getZ());
            player.sendMessage("§aZweite Position gesetzt!");
        }
    }

    public ZoneSelection getSelection(UUID playerId) {
        return selections.get(playerId);
    }

    public static ItemStack getZoneTool() {
        ItemStack tool = new ItemStack(Material.STICK);
        ItemMeta meta = tool.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6Zone-Tool");
            tool.setItemMeta(meta);
        }
        return tool;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        ZoneManager zoneManager = ZonePlugin.getInstance().getZoneManager();

        Zone playerZone = zoneManager.getPlayerZone(playerId);
        boolean isInZone = playerZone != null;

        if (isInZone) {
            if (!activeBossBars.containsKey(playerId)) {
                String zoneName = "Zone: " + playerZone.getOwner() + " #" + zoneManager.getZoneCountForPlayer(playerId);

                BossBar bossBar = Bukkit.createBossBar(zoneName, BarColor.GREEN, BarStyle.SOLID);
                bossBar.addPlayer(player);
                activeBossBars.put(playerId, bossBar);
            }
        } else {
            if (activeBossBars.containsKey(playerId)) {
                BossBar bossBar = activeBossBars.remove(playerId);
                bossBar.removePlayer(player);
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        ZoneManager zoneManager = ZonePlugin.getInstance().getZoneManager();

        Zone playerZone = zoneManager.getPlayerZone(playerId);

        if (playerZone != null && !activeBossBars.containsKey(playerId)) {
            String zoneName = "Zone: " + playerZone.getOwner() + " #" + zoneManager.getZoneCountForPlayer(playerId);

            BossBar bossBar = Bukkit.createBossBar(zoneName, BarColor.GREEN, BarStyle.SOLID);
            bossBar.addPlayer(player);
            activeBossBars.put(playerId, bossBar);
        }
    }


}

