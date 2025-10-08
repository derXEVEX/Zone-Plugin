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
    private final HashMap<UUID, Zone> currentPlayerZones = new HashMap<>();


    @EventHandler
    public void onPlayerClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (player.getInventory().getItemInMainHand().getType() != Material.STICK) return;

        event.setCancelled(true);

        selections.putIfAbsent(playerId, new ZoneSelection());
        ZoneSelection selection = selections.get(playerId);

        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            selection.setPosition1(event.getClickedBlock().getX(), event.getClickedBlock().getZ());
            player.sendMessage("§aSet first position");

            if (selection.isComplete()) {
                ZonePlugin.getInstance().getZoneVisualizer().startVisualization(player, selection);
                player.sendMessage("§e/zone confirm to create the zone");
            }
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            selection.setPosition2(event.getClickedBlock().getX(), event.getClickedBlock().getZ());
            player.sendMessage("§aSecond position set");

            if (selection.isComplete()) {
                ZonePlugin.getInstance().getZoneVisualizer().startVisualization(player, selection);
                player.sendMessage("§e/zone confirm to create the zone");
            }
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

        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        SubZone oldSubZone = zoneManager.getSubZoneAt(event.getFrom());
        SubZone newSubZone = zoneManager.getSubZoneAt(event.getTo());
        Zone newZone = zoneManager.getZoneAt(event.getTo());
        Zone oldZone = currentPlayerZones.get(playerId);
        BossBar bossBar = activeBossBars.get(playerId);

        if (newSubZone != null) {
            currentPlayerZones.put(playerId, newZone);
            String zoneName = "SubZone: " + newSubZone.getOwnerName() + "#" + newSubZone.getFullZoneName();

            if (bossBar == null) {
                bossBar = Bukkit.createBossBar(zoneName, BarColor.YELLOW, BarStyle.SOLID);
                activeBossBars.put(playerId, bossBar);
                bossBar.addPlayer(player);
            } else {
                bossBar.setTitle(zoneName);
                bossBar.setColor(BarColor.YELLOW);
            }
            return;
        }

        if (oldSubZone != null && newZone != null) {
            currentPlayerZones.put(playerId, newZone);
            String zoneName = "Zone: " + newZone.getOwnerName() + "#" + newZone.getZoneNumber();

            if (bossBar == null) {
                bossBar = Bukkit.createBossBar(zoneName, BarColor.GREEN, BarStyle.SOLID);
                activeBossBars.put(playerId, bossBar);
                bossBar.addPlayer(player);
            } else {
                bossBar.setTitle(zoneName);
                bossBar.setColor(BarColor.GREEN);
            }
            return;
        }

        if (newZone != null) {
            boolean zoneChanged = !newZone.equals(oldZone);

            if (zoneChanged) {
                currentPlayerZones.put(playerId, newZone);
                String zoneName = "Zone: " + newZone.getOwnerName() + "#" + newZone.getZoneNumber();

                if (bossBar == null) {
                    bossBar = Bukkit.createBossBar(zoneName, BarColor.GREEN, BarStyle.SOLID);
                    activeBossBars.put(playerId, bossBar);
                    bossBar.addPlayer(player);
                } else {
                    bossBar.setTitle(zoneName);
                    bossBar.setColor(BarColor.GREEN);
                }
            }
            return;
        }

        if (oldZone != null) {
            currentPlayerZones.remove(playerId);
            BossBar oldBar = activeBossBars.remove(playerId);
            if (oldBar != null) {
                oldBar.removePlayer(player);
                oldBar.removeAll();
            }
        }
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        ZoneManager zoneManager = ZonePlugin.getInstance().getZoneManager();

        Zone playerZone = zoneManager.getZoneAt(player.getLocation());


        if (playerZone != null) {
            currentPlayerZones.put(playerId, playerZone);
            String zoneName = "Zone: " + playerZone.getOwnerName() + "#" + playerZone.getZoneNumber();

            BossBar bossBar = Bukkit.createBossBar(zoneName, BarColor.GREEN, BarStyle.SOLID);
            bossBar.addPlayer(player);
            activeBossBars.put(playerId, bossBar);
        }
    }

    @EventHandler
    public void onZoneCreate(ZoneCreateEvent event) {
        Zone newZone = event.getZone();
        if (newZone != null) {
            ZonePlugin plugin = ZonePlugin.getInstance();
            if (plugin != null && plugin.getZoneManager() != null) {
                plugin.getZoneManager().saveZoneBackup(newZone);
            }
        }
    }

    public void clearSelection(UUID playerId) {
        selections.remove(playerId);
    }



}

