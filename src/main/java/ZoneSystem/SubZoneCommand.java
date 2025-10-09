package ZoneSystem;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class SubZoneCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be executed by a player.");
            return true;
        }

        UUID playerId = player.getUniqueId();
        ZonePlugin plugin = ZonePlugin.getInstance();
        ZoneManager zoneManager = plugin.getZoneManager();

        if (args.length == 0) {
            player.sendMessage("§cUsage: /subzone create <PlayerName>#<ZoneNumber>");
            return true;
        }

        if (args[0].equalsIgnoreCase("create")) {
            if (args.length != 2) {
                player.sendMessage("§cUsage: /subzone create <PlayerName>#<ZoneNumber>");
                return true;
            }

            Zone mainZone = zoneManager.getZoneByIdentifier(args[1]);

            if (mainZone == null) {
                player.sendMessage("§cZone not found! Use: /subzone create <CustomName|PlayerName#Number>");
                return true;
            }

            int mainZoneNumber = mainZone.getZoneNumber();

            if (mainZone == null) {
                player.sendMessage("§cYou don't have permission to create subzones!");
                return true;
            }

            if (!mainZone.isOwner(playerId)) {
                player.sendMessage("§cYou can only create subzones in your own zones!");
                return true;
            }

            zoneManager.setActiveSubZoneCreation(playerId, mainZone);
            player.getInventory().addItem(plugin.getSubZoneTool());
            player.sendMessage("§aSubzone tool received! Mark the corner points (with Y coordinates).");
            player.sendMessage("§7Left-Click: Position 1 | Right-Click: Position 2 and then /subzone confirm");
            player.sendMessage("§e/subzone cancel to cancel the creation.");
            return true;
        }

        if (args[0].equalsIgnoreCase("confirm")) {
            SubZoneSelection selection = plugin.getSubZoneListener().getSelection(playerId);
            Zone targetMainZone = zoneManager.getActiveSubZoneCreation(playerId);

            if (targetMainZone == null) {
                player.sendMessage("§cNo active subzone creation!");
                return true;
            }

            if (selection == null || !selection.isComplete()) {
                player.sendMessage("§cYou must set two points first!");
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
                player.sendMessage("§cThe subzone must be completely within the main zone " +
                        targetMainZone.getOwnerName() + "#" + targetMainZone.getZoneNumber() + "!");
                return true;
            }

            if (!zoneManager.canCreateSubZone(subZone)) {
                player.sendMessage("§cThis subzone overlaps with another subzone!");
                return true;
            }

            zoneManager.addSubZone(subZone);
            zoneManager.clearActiveSubZoneCreation(playerId);
            plugin.getSubZoneListener().clearSelection(playerId);
            plugin.getSubZoneVisualizer().stopVisualization(player);
            player.getInventory().removeItem(plugin.getSubZoneTool());
            player.sendMessage("§aSubzone " + targetMainZone.getOwnerName() + "#" + subZone.getFullZoneName() + " created!");
            return true;
        }

        if (args[0].equalsIgnoreCase("delete")) {
            if (args.length != 2) {
                player.sendMessage("§cUsage: /subzone delete <PlayerName>#<ZoneNumber>.<SubzoneNumber>");
                return true;
            }

            String[] mainParts = args[1].split("#");
            if (mainParts.length != 2) {
                player.sendMessage("§cInvalid format. Example: aip#1.1");
                return true;
            }

            String targetPlayer = mainParts[0];
            String[] zoneParts = mainParts[1].split("\\.");
            if (zoneParts.length != 2) {
                player.sendMessage("§cInvalid format. Example: aip#1.1");
                return true;
            }

            int mainZoneNumber;
            int subZoneNumber;
            try {
                mainZoneNumber = Integer.parseInt(zoneParts[0]);
                subZoneNumber = Integer.parseInt(zoneParts[1]);
            } catch (NumberFormatException e) {
                player.sendMessage("§cInvalid zone number!");
                return true;
            }

            String[] parts = args[1].split("#");
            if (parts.length != 2) {
                player.sendMessage("§cInvalid format! Use: PlayerName#ZoneNumber");
                return true;
            }

            Zone mainZone = zoneManager.getZoneByIdentifier(parts[0] + "#" + parts[1]);

            if (mainZone == null) {
                player.sendMessage("§cMain zone not found!");
                return true;
            }

            SubZone subZone = zoneManager.getSubZoneByNumbers(
                    Bukkit.getOfflinePlayer(mainZone.getOwnerUUID()).getName(),
                    mainZone.getZoneNumber(),
                    subZoneNumber
            );

            if (subZone == null) {
                player.sendMessage("§cSubzone not found!");
                return true;
            }

            if (!subZone.isOwner(playerId)) {
                player.sendMessage("§cYou can only delete your own subzones!");
                return true;
            }

            zoneManager.removeSubZone(subZone);
            player.sendMessage("§aSubzone " + targetPlayer + "#" + mainZoneNumber + "." + subZoneNumber + " deleted!");
            return true;
        }

        if (args[0].equalsIgnoreCase("cancel")) {
            zoneManager.clearActiveSubZoneCreation(playerId);
            plugin.getSubZoneListener().clearSelection(playerId);
            plugin.getSubZoneVisualizer().stopVisualization(player);
            player.getInventory().removeItem(plugin.getSubZoneTool());
            player.sendMessage("§eSubzone creation cancelled.");
            return true;
        }

        if (args[0].equalsIgnoreCase("permissions")) {
            SubZonePermissionCommand permCommand = new SubZonePermissionCommand();
            return permCommand.onCommand(sender, command, label,
                    java.util.Arrays.copyOfRange(args, 1, args.length));
        }


        return true;
    }
}
