package ZoneSystem;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class SubZoneCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cDieser Befehl kann nur von einem Spieler ausgeführt werden.");
            return true;
        }

        UUID playerId = player.getUniqueId();
        ZonePlugin plugin = ZonePlugin.getInstance();
        ZoneManager zoneManager = plugin.getZoneManager();

        if (args.length == 0) {
            player.sendMessage("§cVerwendung: /subzone create <Spielername>#<Zonennummer>");
            return true;
        }

        if (args[0].equalsIgnoreCase("create")) {
            if (args.length != 2) {
                player.sendMessage("§cVerwendung: /subzone create <Spielername>#<Zonennummer>");
                return true;
            }

            String[] parts = args[1].split("#");
            if (parts.length != 2) {
                player.sendMessage("§cUngültiges Format. Beispiel: aip#1");
                return true;
            }

            String targetPlayer = parts[0];
            int mainZoneNumber;
            try {
                mainZoneNumber = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                player.sendMessage("§cUngültige Zonennummer!");
                return true;
            }

            Zone mainZone = zoneManager.getZoneByPlayerAndNumber(targetPlayer, mainZoneNumber);
            if (mainZone == null) {
                player.sendMessage("§cHauptzone " + targetPlayer + "#" + mainZoneNumber + " nicht gefunden!");
                return true;
            }

            if (!mainZone.isOwner(playerId)) {
                player.sendMessage("§cDu kannst nur Subzonen in deinen eigenen Zonen erstellen!");
                return true;
            }

            zoneManager.setActiveSubZoneCreation(playerId, mainZone);
            player.getInventory().addItem(plugin.getSubZoneTool());
            player.sendMessage("§aSubzone-Tool erhalten! Markiere die Eckpunkte (mit Y-Koordinaten).");
            player.sendMessage("§7Links-Klick: Position 1 | Rechts-Klick: Position 2");
            return true;
        }

        if (args[0].equalsIgnoreCase("confirm")) {
            SubZoneSelection selection = plugin.getSubZoneListener().getSelection(playerId);
            Zone targetMainZone = zoneManager.getActiveSubZoneCreation(playerId);

            if (targetMainZone == null) {
                player.sendMessage("§cKeine aktive Subzone-Erstellung!");
                return true;
            }

            if (selection == null || !selection.isComplete()) {
                player.sendMessage("§cDu musst erst zwei Punkte setzen!");
                return true;
            }

            int nextSubNumber = zoneManager.getNextSubZoneNumber(
                    targetMainZone.getOwnerUUID(),
                    targetMainZone.getZoneNumber()
            );

            SubZone subZone = selection.toSubZone(
                    targetMainZone.getOwnerUUID(),
                    targetMainZone.getOwnerName(),
                    targetMainZone.getZoneNumber(),
                    nextSubNumber
            );

            if (!zoneManager.isSubZoneWithinMainZone(subZone, targetMainZone)) {
                player.sendMessage("§cDie Subzone muss vollständig in der Hauptzone " +
                        targetMainZone.getOwnerName() + "#" + targetMainZone.getZoneNumber() + " liegen!");
                return true;
            }

            if (!zoneManager.canCreateSubZone(subZone)) {
                player.sendMessage("§cDiese Subzone überschneidet sich mit einer anderen Subzone!");
                return true;
            }

            zoneManager.addSubZone(subZone);
            zoneManager.clearActiveSubZoneCreation(playerId);
            plugin.getSubZoneListener().clearSelection(playerId);
            plugin.getSubZoneVisualizer().stopVisualization(player);
            player.getInventory().removeItem(plugin.getSubZoneTool());
            player.sendMessage("§aSubzone " + targetMainZone.getOwnerName() + "#" + subZone.getFullZoneName() + " erstellt!");
            return true;
        }

        if (args[0].equalsIgnoreCase("delete")) {
            if (args.length != 2) {
                player.sendMessage("§cVerwendung: /subzone delete <Spielername>#<Zonennummer>.<Subzonennummer>");
                return true;
            }

            String[] mainParts = args[1].split("#");
            if (mainParts.length != 2) {
                player.sendMessage("§cUngültiges Format. Beispiel: aip#1.1");
                return true;
            }

            String targetPlayer = mainParts[0];
            String[] zoneParts = mainParts[1].split("\\.");
            if (zoneParts.length != 2) {
                player.sendMessage("§cUngültiges Format. Beispiel: aip#1.1");
                return true;
            }

            int mainZoneNumber;
            int subZoneNumber;
            try {
                mainZoneNumber = Integer.parseInt(zoneParts[0]);
                subZoneNumber = Integer.parseInt(zoneParts[1]);
            } catch (NumberFormatException e) {
                player.sendMessage("§cUngültige Zonennummer!");
                return true;
            }

            SubZone subZone = zoneManager.getSubZoneByNumbers(targetPlayer, mainZoneNumber, subZoneNumber);
            if (subZone == null) {
                player.sendMessage("§cSubzone nicht gefunden!");
                return true;
            }

            if (!subZone.isOwner(playerId)) {
                player.sendMessage("§cDu kannst nur deine eigenen Subzonen löschen!");
                return true;
            }

            zoneManager.removeSubZone(subZone);
            player.sendMessage("§aSubzone " + targetPlayer + "#" + mainZoneNumber + "." + subZoneNumber + " gelöscht!");
            return true;
        }

        if (args[0].equalsIgnoreCase("cancel")) {
            zoneManager.clearActiveSubZoneCreation(playerId);
            plugin.getSubZoneListener().clearSelection(playerId);
            plugin.getSubZoneVisualizer().stopVisualization(player);
            player.getInventory().removeItem(plugin.getSubZoneTool());
            player.sendMessage("§eSubzone-Erstellung abgebrochen.");
            return true;
        }



        return true;
    }
}
