package ZoneSystem;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ZonePermissionCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage("§cUsage: /zone permissions <Zone#X> <list|set>");
            return true;
        }

        ZoneManager zoneManager = ZonePlugin.getInstance().getZoneManager();
        String[] zoneParts = args[0].split("#");
        if (zoneParts.length != 2) {
            player.sendMessage("§cInvalid format. Example: Player#1 or Player#1.1");
            return true;
        }

        String ownerName = zoneParts[0];
        String[] numberParts = zoneParts[1].split("\\.");
        int mainZoneNumber;
        Integer subZoneNumber = null;

        try {
            mainZoneNumber = Integer.parseInt(numberParts[0]);
            if (numberParts.length == 2) {
                subZoneNumber = Integer.parseInt(numberParts[1]);
            }
        } catch (NumberFormatException e) {
            player.sendMessage("§cInvalid zone number!");
            return true;
        }

        Zone zone = zoneManager.getZoneByPlayerAndNumber(ownerName, mainZoneNumber);
        if (zone == null) {
            player.sendMessage("§cZone not found!");
            return true;
        }

        if (!zone.isOwner(player.getUniqueId())) {
            player.sendMessage("§cYou can only manage permissions for your own zones!");
            return true;
        }

        if (args[1].equalsIgnoreCase("list")) {
            ZonePermissionEntry entry = zoneManager.getPermissionEntry(zone.getOwnerUUID(), mainZoneNumber, subZoneNumber);

            player.sendMessage("§6§l=== Permissions for " + ownerName + "#" + (subZoneNumber == null ? mainZoneNumber : mainZoneNumber + "." + subZoneNumber) + " ===");

            if (entry == null) {
                player.sendMessage("§7No permissions set.");
                return true;
            }

            player.sendMessage("§e§lGlobal Permissions:");
            if (entry.getGlobalPermissions().isEmpty()) {
                player.sendMessage("§7  None");
            } else {
                entry.getGlobalPermissions().forEach((perm, value) ->
                        player.sendMessage("§7  " + perm.getKey() + ": §f" + (value ? "§aALLOW" : "§cDENY"))
                );
            }

            player.sendMessage("§e§lUser Permissions:");
            if (entry.getUserPermissions().isEmpty()) {
                player.sendMessage("§7  None");
            } else {
                entry.getUserPermissions().forEach((uuid, perms) -> {
                    String userName = Bukkit.getOfflinePlayer(uuid).getName();
                    player.sendMessage("§b  " + userName + ":");
                    perms.forEach((perm, value) ->
                            player.sendMessage("§7    " + perm.getKey() + ": §f" + (value ? "§aALLOW" : "§cDENY"))
                    );
                });
            }
            return true;
        }

        if (args[1].equalsIgnoreCase("set")) {
            if (args.length != 5) {
                player.sendMessage("§cUsage: /zone permissions <Zone#X> set <user|*> <permission|*> <true|false>");
                return true;
            }

            String targetUser = args[2];
            String permissionKey = args[3];
            boolean value;

            try {
                value = Boolean.parseBoolean(args[4]);
            } catch (Exception e) {
                player.sendMessage("§cUse 'true' or 'false'!");
                return true;
            }

            UUID targetUUID = null;
            if (!targetUser.equals("*")) {
                Player target = Bukkit.getPlayer(targetUser);
                if (target == null) {
                    player.sendMessage("§cPlayer not found!");
                    return true;
                }
                targetUUID = target.getUniqueId();
            }

            if (permissionKey.equals("*")) {
                for (ZonePermission perm : ZonePermission.values()) {
                    zoneManager.setZonePermission(zone.getOwnerUUID(), mainZoneNumber, subZoneNumber, targetUUID, perm, value);
                }
                player.sendMessage("§aAll permissions set to §f" + value + " §afor " + (targetUser.equals("*") ? "everyone" : targetUser));
            } else {
                ZonePermission permission = ZonePermission.fromString(permissionKey);
                if (permission == null) {
                    player.sendMessage("§cInvalid permission! Available:");
                    for (ZonePermission perm : ZonePermission.values()) {
                        player.sendMessage("§7  - " + perm.getKey() + " §f(" + perm.getDescription() + ")");
                    }
                    return true;
                }

                zoneManager.setZonePermission(zone.getOwnerUUID(), mainZoneNumber, subZoneNumber, targetUUID, permission, value);
                player.sendMessage("§aPermission §e" + permission.getKey() + " §aset to §f" + value + " §afor " + (targetUser.equals("*") ? "everyone" : targetUser));
            }
            return true;
        }

        return true;
    }
}
