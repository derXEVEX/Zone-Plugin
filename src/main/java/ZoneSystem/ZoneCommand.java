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
            sender.sendMessage("§cDieser Befehl kann nur von einem Spieler ausgeführt werden.");
            return true;
        }

        UUID playerId = player.getUniqueId();
        ZonePlugin plugin = ZonePlugin.getInstance();
        ZoneManager zoneManager = plugin.getZoneManager();
        ZoneListener listener = plugin.getZoneListener();

        if (args.length == 0) {
            player.sendMessage("§cVerwendung: /zone create | confirm | reset <Spielername>#<Nummer> | delete <Spielername>#<Nummer>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create":
                player.getInventory().addItem(plugin.getZoneTool());
                player.sendMessage("§aDamit kannst du die Zone erstellen. Links-Klick setzt Position 1 | Rechts-Klick setzt Position 2");
                return true;

            case "confirm":
                ZoneSelection selection = listener.getSelection(playerId);
                if (selection == null || !selection.isComplete()) {
                    player.sendMessage("§cDu musst erst zwei Punkte setzen!");
                    return true;
                }

                if (!selection.isValidSize()) {
                    player.sendMessage("§cDie Zone muss mindestens 8x8 Blöcke groß sein!");
                    return true;
                }

                Zone zone = selection.toZone(playerId, player.getName());

                if (!zoneManager.canPlayerCreateZone(playerId, zone)) {
                    int currentZones = zoneManager.getZonesForPlayer(playerId).size();
                    int currentArea = zoneManager.getTotalAreaForPlayer(playerId);
                    int newArea = zone.getArea();

                    if (currentZones >= ZoneLimits.MAX_ZONES_PER_PLAYER) {
                        player.sendMessage("§cDu hast bereits die maximale Anzahl von " + ZoneLimits.MAX_ZONES_PER_PLAYER + " Zonen erreicht!");
                    } else {
                        player.sendMessage("§cDiese Zone würde dein Flächenlimit überschreiten!");
                        player.sendMessage("§eDeine Fläche: " + currentArea + "/" + ZoneLimits.MAX_TOTAL_AREA);
                        player.sendMessage("§eNeue Zone: " + newArea + " Blöcke");
                    }
                    return true;
                }

                if (!zoneManager.canCreateZone(zone)) {
                    player.sendMessage("§cDiese Zone überschneidet sich mit einer anderen!");
                    return true;
                }

                zoneManager.addZone(zone);
                Bukkit.getPluginManager().callEvent(new ZoneCreateEvent(zone));
                listener.clearSelection(playerId);
                plugin.getZoneVisualizer().stopVisualization(player);
                player.getInventory().removeItem(plugin.getZoneTool());
                player.sendMessage("§aZone erfolgreich erstellt! (" + zone.getArea() + " Blöcke)");
                return true;


            case "reset":
            case "delete":
                if (args.length != 2) {
                    player.sendMessage("§cVerwendung: /zone " + args[0] + " <Spielername>#<Nummer>");
                    return true;
                }

                String[] zoneParts = args[1].split("#");
                if (zoneParts.length != 2) {
                    player.sendMessage("§cUngültiges Format. Beispiel: Spielername#1");
                    return true;
                }

                String targetPlayer = zoneParts[0];
                int zoneNumber;
                try {
                    zoneNumber = Integer.parseInt(zoneParts[1]);
                } catch (NumberFormatException e) {
                    player.sendMessage("§cUngültige Zonennummer!");
                    return true;
                }

                Zone targetZone = zoneManager.getZoneByPlayerAndNumber(targetPlayer, zoneNumber);
                if (targetZone == null) {
                    player.sendMessage("§cZone nicht gefunden!");
                    return true;
                }

                boolean isOwner = targetZone.isOwner(playerId);

                if (args[0].equalsIgnoreCase("reset")) {
                    if (!isOwner && !player.hasPermission("zone.reset.others")) {
                        player.sendMessage("§cDu hast keine Berechtigung, fremde Zonen zurückzusetzen!");
                        return true;
                    }
                    if (isOwner && !player.hasPermission("zone.reset.own")) {
                        player.sendMessage("§cDu hast keine Berechtigung, eigene Zonen zurückzusetzen!");
                        return true;
                    }

                    zoneManager.restoreZoneBackup(targetZone);
                    player.sendMessage("§aZone wurde zurückgesetzt.");

                } else {
                    if (!isOwner && !player.hasPermission("zone.delete.others")) {
                        player.sendMessage("§cDu hast keine Berechtigung, fremde Zonen zu löschen!");
                        return true;
                    }
                    if (isOwner && !player.hasPermission("zone.delete.own")) {
                        player.sendMessage("§cDu hast keine Berechtigung, eigene Zonen zu löschen!");
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

                    player.sendMessage("§eZone wurde gelöscht.");
                    if (subZoneCount > 0) {
                        player.sendMessage("§6" + subZoneCount + " Subzone(n) wurden automatisch mitgelöscht.");
                    }
                    player.sendMessage("§aFreigegebene Fläche: " + freedArea + " Blöcke");
                    player.sendMessage("§7Deine Zonen: " + remainingZones + "/" + ZoneLimits.MAX_ZONES_PER_PLAYER);
                    player.sendMessage("§7Deine Fläche: " + remainingArea + "/" + ZoneLimits.MAX_TOTAL_AREA);
                }

                return true;


            case "info":
                Zone currentZone = zoneManager.getZoneAt(player.getLocation());
                SubZone currentSubZone = zoneManager.getSubZoneAt(player.getLocation());

                if (currentSubZone != null) {
                    player.sendMessage("§6§l=== SubZone Info ===");
                    player.sendMessage("§eOwner: §f" + currentSubZone.getOwnerName());
                    player.sendMessage("§eSubZone: §f#" + currentSubZone.getFullZoneName());
                    player.sendMessage("§eHauptzone: §f#" + currentSubZone.getMainZoneNumber());
                    player.sendMessage("§6§l===================");
                } else if (currentZone != null) {
                    int subZoneCount = zoneManager.getSubZoneCountForZone(
                            currentZone.getOwnerUUID(),
                            currentZone.getZoneNumber()
                    );

                    player.sendMessage("§6§l=== Zone Info ===");
                    player.sendMessage("§eOwner: §f" + currentZone.getOwnerName());
                    player.sendMessage("§eZone: §f#" + currentZone.getZoneNumber());
                    player.sendMessage("§eGröße: §f" + currentZone.getArea() + " Blöcke");
                    player.sendMessage("§eSubzonen: §f" + subZoneCount);
                    player.sendMessage("§6§l================");
                } else {
                    player.sendMessage("§7Du befindest dich auf keiner Zone.");
                }

                player.sendMessage("");
                player.sendMessage("§6§l=== Deine Zonen ===");

                List<Zone> playerZones = zoneManager.getZonesForPlayer(playerId);
                int totalUsedArea = zoneManager.getTotalAreaForPlayer(playerId);
                int freeArea = ZoneLimits.MAX_TOTAL_AREA - totalUsedArea;

                if (playerZones.isEmpty()) {
                    player.sendMessage("§7Du besitzt keine Zonen.");
                } else {
                    for (Zone z : playerZones) {
                        int szCount = zoneManager.getSubZoneCountForZone(playerId, z.getZoneNumber());
                        player.sendMessage("§e" + player.getName() + "#" + z.getZoneNumber() +
                                " §7- §f" + z.getArea() + " Blöcke §7(§6" + szCount + " Subzonen§7)");
                    }
                }

                player.sendMessage("");
                player.sendMessage("§eZonen: §f" + playerZones.size() + "/" + ZoneLimits.MAX_ZONES_PER_PLAYER);
                player.sendMessage("§eFläche: §f" + totalUsedArea + "/" + ZoneLimits.MAX_TOTAL_AREA + " Blöcke");
                player.sendMessage("§aFreie Fläche: §f" + freeArea + " Blöcke");
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
                player.sendMessage("§eZone-Erstellung abgebrochen.");
                return true;





            default:
                player.sendMessage("§cUnbekannter Befehl. Verwende: /zone create | confirm | reset | delete | info | permissions");
                return true;
        }
}}