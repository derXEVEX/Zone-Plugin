package ZoneSystem;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class ZonePermissionCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be executed by a player.");
            return true;
        }

        ZoneManager zoneManager = ZonePlugin.getInstance().getZoneManager();

        if (args.length < 2) {
            player.sendMessage("§cUsage: /zone permissions <PlayerName>#<Number> list | set <Player|*> <Permission|*> <true|false>");
            return true;
        }

        Zone targetZone = zoneManager.getZoneByIdentifier(args[0]);
        if (targetZone == null) {
            player.sendMessage("§cZone not found! Use: CustomName or PlayerName#Number");
            return true;
        }

        if (!targetZone.isOwner(player.getUniqueId())) {
            player.sendMessage("§cYou can only manage your own zones!");
            return true;
        }

        String subCommand = args[1].toLowerCase();

        if (subCommand.equals("list")) {
            displayPermissions(player, targetZone, zoneManager);
            return true;
        }

        if (subCommand.equals("set")) {
            if (args.length < 5) {
                player.sendMessage("§cUsage: /zone permissions <Zone> set <Player|*> <Permission|*> <true|false>");
                return true;
            }

            String targetUserName = args[2];
            String permissionKey = args[3];
            boolean value;

            try {
                value = Boolean.parseBoolean(args[4]);
            } catch (Exception e) {
                player.sendMessage("§cInvalid value! Use true or false.");
                return true;
            }

            UUID targetUser = null;
            if (!targetUserName.equals("*")) {
                Player target = Bukkit.getPlayer(targetUserName);
                if (target == null) {
                    player.sendMessage("§cPlayer not found!");
                    return true;
                }
                targetUser = target.getUniqueId();
            }

            if (permissionKey.equals("*")) {
                for (ZonePermission perm : ZonePermission.values()) {
                    zoneManager.setZonePermission(targetZone.getOwnerUUID(), targetZone.getZoneNumber(), null, targetUser, perm, value);
                }
                String scope = targetUser == null ? "everyone" : targetUserName;
                player.sendMessage("§aSet all permissions to " + value + " for " + scope + ".");
            } else {
                ZonePermission permission = ZonePermission.fromString(permissionKey);
                if (permission == null) {
                    player.sendMessage("§cUnknown permission! Available permissions:");
                    for (ZonePermission perm : ZonePermission.values()) {
                        player.sendMessage("§e- " + perm.getKey() + " §7(" + perm.getDescription() + ")");
                    }
                    return true;
                }

                zoneManager.setZonePermission(targetZone.getOwnerUUID(), targetZone.getZoneNumber(), null, targetUser, permission, value);
                String scope = targetUser == null ? "everyone" : targetUserName;
                player.sendMessage("§aSet " + permission.getKey() + " to " + value + " for " + scope + ".");
            }

            return true;
        }

        player.sendMessage("§cUnknown subcommand. Use: list | set");
        return true;
    }

    private void displayPermissions(Player player, Zone zone, ZoneManager zoneManager) {
        ZonePermissionEntry mainEntry = zoneManager.getPermissionEntry(zone.getOwnerUUID(), zone.getZoneNumber(), null);

        player.sendMessage("§6§l=== Zone Permissions ===");
        player.sendMessage("§eMain Zone: §f" + zone.getOwnerName() + "#" + zone.getZoneNumber());

        player.sendMessage("");
        player.sendMessage("§6§lMain Zone Permissions:");
        if (mainEntry == null) {
            player.sendMessage("§7No permissions set yet.");
        } else {
            displayEntryPermissions(player, mainEntry);
        }

        // Subzonen anzeigen
        int subZoneCount = zoneManager.getSubZoneCountForZone(zone.getOwnerUUID(), zone.getZoneNumber());
        if (subZoneCount > 0) {
            player.sendMessage("");
            player.sendMessage("§6§l=== SubZone Permissions ===");

            for (int i = 1; i <= subZoneCount; i++) {
                ZonePermissionEntry subEntry = zoneManager.getPermissionEntry(
                        zone.getOwnerUUID(),
                        zone.getZoneNumber(),
                        i
                );

                player.sendMessage("");
                player.sendMessage("§eSubZone §f#" + zone.getZoneNumber() + "." + i + ":");

                if (subEntry == null) {
                    player.sendMessage("§7No specific permissions set. Inheriting from main zone.");
                } else {
                    displayEntryPermissions(player, subEntry);
                }
            }
        }

        player.sendMessage("§6§l========================");
    }

    private void displayEntryPermissions(Player player, ZonePermissionEntry entry) {
        player.sendMessage("§6Global Permissions:");
        Map<ZonePermission, Boolean> globalPerms = entry.getGlobalPermissions();
        if (globalPerms.isEmpty()) {
            player.sendMessage("§7None");
        } else {
            for (Map.Entry<ZonePermission, Boolean> perm : globalPerms.entrySet()) {
                String status = perm.getValue() ? "§aEnabled" : "§cDisabled";
                player.sendMessage("§e- " + perm.getKey().getKey() + ": " + status);
            }
        }

        player.sendMessage("§6User-Specific Permissions:");
        Map<UUID, Map<ZonePermission, Boolean>> userPerms = entry.getUserPermissions();
        if (userPerms.isEmpty()) {
            player.sendMessage("§7None");
        } else {
            for (Map.Entry<UUID, Map<ZonePermission, Boolean>> userEntry : userPerms.entrySet()) {
                Player target = Bukkit.getPlayer(userEntry.getKey());
                String userName = target != null ? target.getName() : userEntry.getKey().toString();
                player.sendMessage("§e" + userName + ":");

                for (Map.Entry<ZonePermission, Boolean> perm : userEntry.getValue().entrySet()) {
                    String status = perm.getValue() ? "§aEnabled" : "§cDisabled";
                    player.sendMessage("  §7- " + perm.getKey().getKey() + ": " + status);
                }
            }
        }
    }

}
