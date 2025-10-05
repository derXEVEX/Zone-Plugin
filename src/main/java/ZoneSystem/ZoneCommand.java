package ZoneSystem;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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

                if (!targetZone.isOwner(playerId) && !player.hasPermission("zone.reset.others")) {
                    player.sendMessage("§cDu kannst nur deine eigenen Zonen verwalten!");
                    return true;
                }

                if (args[0].equalsIgnoreCase("reset")) {
                    zoneManager.restoreZoneBackup(targetZone);
                    player.sendMessage("§aZone wurde zurückgesetzt.");
                } else {
                    zoneManager.removeZone(targetZone);
                    player.sendMessage("§eZone wurde gelöscht.");
                }
                return true;

            default:
                player.sendMessage("§cUnbekannter Befehl. Verwende: /zone create | confirm | reset | delete");
                return true;
        }
    }
}