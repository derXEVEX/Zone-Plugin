package ZoneSystem;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.List;
import java.util.UUID;

public class ZoneCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be executed by a player.");
            return true;
        }

        UUID playerId = player.getUniqueId();
        ZonePlugin plugin = ZonePlugin.getInstance();
        ZoneManager zoneManager = plugin.getZoneManager();
        ZoneListener listener = plugin.getZoneListener();

        if (args.length == 0) {
            player.sendMessage("§cUsage: /zone create | confirm | reset <PlayerName>#<Number> | delete <PlayerName>#<Number>");
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("admin")) {
            return new AdminZoneCommand().onCommand(sender, command, label, args);
        }





        switch (args[0].toLowerCase()) {
            case "create":
                player.getInventory().addItem(plugin.getZoneTool());
                player.sendMessage("§aYou can use this to create the zone. Left-Click sets Position 1 | Right-Click sets Position 2");
                return true;

            case "confirm":
                ZoneSelection selection = listener.getSelection(playerId);
                if (selection == null || !selection.isComplete()) {
                    player.sendMessage("§cYou must set two points first!");
                    return true;
                }

                if (!selection.isValidSize()) {
                    player.sendMessage("§cThe zone must be at least 8x8 blocks in size!");
                    return true;
                }

                Zone zone = selection.toZone(playerId, player.getName());

                if (!zoneManager.canPlayerCreateZone(playerId, zone)) {
                    int currentZones = zoneManager.getZonesForPlayer(playerId).size();
                    int currentArea = zoneManager.getTotalAreaForPlayer(playerId);
                    int newArea = zone.getArea();

                    if (currentZones >= ZoneLimits.MAX_ZONES_PER_PLAYER) {
                        player.sendMessage("§cYou have already reached the maximum number of " + ZoneLimits.MAX_ZONES_PER_PLAYER + " zones!");
                    } else {
                        player.sendMessage("§cThis zone would exceed your area limit!");
                        player.sendMessage("§eYour area: " + currentArea + "/" + ZoneLimits.MAX_TOTAL_AREA);
                        player.sendMessage("§eNew zone: " + newArea + " blocks");
                    }
                    return true;
                }

                if (!zoneManager.canCreateZone(zone)) {
                    player.sendMessage("§cThis zone overlaps with another zone!");
                    return true;
                }

                zoneManager.addZone(zone);
                Bukkit.getPluginManager().callEvent(new ZoneCreateEvent(zone));
                listener.clearSelection(playerId);
                plugin.getZoneVisualizer().stopVisualization(player);
                player.getInventory().removeItem(plugin.getZoneTool());
                player.sendMessage("§aZone successfully created! (" + zone.getArea() + " blocks)");
                return true;


            case "reset":
            case "delete":
                if (args.length != 2) {
                    player.sendMessage("§cUsage: /zone " + args[0] + " <PlayerName>#<Number>");
                    return true;
                }

                String[] zoneParts = args[1].split("#");
                if (zoneParts.length != 2) {
                    player.sendMessage("§cInvalid format. Example: PlayerName#1");
                    return true;
                }

                String targetPlayer = zoneParts[0];
                int zoneNumber;
                try {
                    zoneNumber = Integer.parseInt(zoneParts[1]);
                } catch (NumberFormatException e) {
                    player.sendMessage("§cInvalid zone number!");
                    return true;
                }

                Zone targetZone = zoneManager.getZoneByPlayerAndNumber(targetPlayer, zoneNumber);
                if (targetZone == null) {
                    player.sendMessage("§cZone not found!");
                    return true;
                }

                boolean isOwner = targetZone.isOwner(playerId);

                if (args[0].equalsIgnoreCase("reset")) {
                    if (!isOwner && !player.hasPermission("zone.reset.others")) {
                        player.sendMessage("§cYou don't have permission to reset other players' zones!");
                        return true;
                    }
                    if (isOwner && !player.hasPermission("zone.reset.own")) {
                        player.sendMessage("§cYou don't have permission to reset your own zones!");
                        return true;
                    }

                    zoneManager.restoreZoneBackup(targetZone);
                    player.sendMessage("§aZone has been reset.");

                } else {
                    if (!isOwner && !player.hasPermission("zone.delete.others")) {
                        player.sendMessage("§cYou don't have permission to delete other players' zones!");
                        return true;
                    }
                    if (isOwner && !player.hasPermission("zone.delete.own")) {
                        player.sendMessage("§cYou don't have permission to delete your own zones!");
                        return true;
                    }

                    int freedArea = targetZone.getArea();
                    int subZoneCount = zoneManager.getSubZoneCountForZone(
                            targetZone.getOwnerUUID(),
                            targetZone.getZoneNumber()
                    );

                    zoneManager.removeZoneWithSubZones(targetZone);

                    int remainingZones = zoneManager.getZonesForPlayer(targetZone.getOwnerUUID()).size();
                    int remainingArea = zoneManager.getTotalAreaForPlayer(targetZone.getOwnerUUID());

                    player.sendMessage("§eZone has been deleted.");
                    if (subZoneCount > 0) {
                        player.sendMessage("§6" + subZoneCount + " subzone(s) were automatically deleted as well.");
                    }
                    player.sendMessage("§aFreed area: " + freedArea + " blocks");
                    player.sendMessage("§7Your zones: " + remainingZones + "/" + ZoneLimits.MAX_ZONES_PER_PLAYER);
                    player.sendMessage("§7Your area: " + remainingArea + "/" + ZoneLimits.MAX_TOTAL_AREA);
                }

                return true;


            case "info":
                Zone currentZone = zoneManager.getZoneAt(player.getLocation());
                SubZone currentSubZone = zoneManager.getSubZoneAt(player.getLocation());

                if (currentSubZone != null) {
                    player.sendMessage("§6§l=== SubZone Info ===");
                    player.sendMessage("§eOwner: §f" + currentSubZone.getOwnerName());
                    player.sendMessage("§eSubZone: §f#" + currentSubZone.getFullZoneName());
                    player.sendMessage("§eMain zone: §f#" + currentSubZone.getMainZoneNumber());
                    player.sendMessage("§6§l===================");
                } else if (currentZone != null) {
                    int subZoneCount = zoneManager.getSubZoneCountForZone(
                            currentZone.getOwnerUUID(),
                            currentZone.getZoneNumber()
                    );

                    player.sendMessage("§6§l=== Zone Info ===");
                    player.sendMessage("§eOwner: §f" + currentZone.getOwnerName());
                    player.sendMessage("§eZone: §f#" + currentZone.getZoneNumber());
                    player.sendMessage("§eSize: §f" + currentZone.getArea() + " blocks");
                    player.sendMessage("§eSubzones: §f" + subZoneCount);
                    player.sendMessage("§6§l================");
                } else {
                    player.sendMessage("§7You are not in any zone.");
                }

                player.sendMessage("");
                player.sendMessage("§6§l=== Your Zones ===");

                List<Zone> playerZones = zoneManager.getZonesForPlayer(playerId);
                int totalUsedArea = zoneManager.getTotalAreaForPlayer(playerId);
                int freeArea = ZoneLimits.MAX_TOTAL_AREA - totalUsedArea;

                if (playerZones.isEmpty()) {
                    player.sendMessage("§7You don't own any zones.");
                } else {
                    for (Zone z : playerZones) {
                        int szCount = zoneManager.getSubZoneCountForZone(playerId, z.getZoneNumber());
                        player.sendMessage("§e" + player.getName() + "#" + z.getZoneNumber() +
                                " §7- §f" + z.getArea() + " blocks §7(§6" + szCount + " subzones§7)");
                    }
                }

                player.sendMessage("");
                player.sendMessage("§eZones: §f" + playerZones.size() + "/" + ZoneLimits.MAX_ZONES_PER_PLAYER);
                player.sendMessage("§eArea: §f" + totalUsedArea + "/" + ZoneLimits.MAX_TOTAL_AREA + " blocks");
                player.sendMessage("§aFree area: §f" + freeArea + " blocks");
                player.sendMessage("§6§l==================");

                return true;

            case "permissions":
                ZonePermissionCommand permCommand = new ZonePermissionCommand();
                return permCommand.onCommand(sender, command, label,
                        java.util.Arrays.copyOfRange(args, 1, args.length));

            case "cancel":
                listener.clearSelection(playerId);
                plugin.getZoneVisualizer().stopVisualization(player);
                player.getInventory().removeItem(plugin.getZoneTool());
                player.sendMessage("§eZone creation cancelled.");
                return true;
            case "show":
                if (args.length != 2) {
                    player.sendMessage("§cUsage: /zone show <all|my>");
                    return true;
                }

                PlayerZoneVisualizer playerVis = plugin.getPlayerZoneVisualizer();

                if (args[1].equalsIgnoreCase("all")) {
                    playerVis.showZones(player, false);
                    player.sendMessage("§aAll zones are now visible. Use §e/zone hide§a to hide them.");
                } else if (args[1].equalsIgnoreCase("my")) {
                    playerVis.showZones(player, true);
                    player.sendMessage("§aYour zones are now visible. Use §e/zone hide§a to hide them.");
                } else {
                    player.sendMessage("§cUsage: /zone show <all|my>");
                }
                return true;

            case "hide":
                plugin.getPlayerZoneVisualizer().stopVisualization(player);
                player.sendMessage("§eZone borders hidden.");
                return true;





            default:
                player.sendMessage("§cUnknown command. Use: /zone create | confirm | reset | delete | info | permissions");
                return true;
        }
    }
}
