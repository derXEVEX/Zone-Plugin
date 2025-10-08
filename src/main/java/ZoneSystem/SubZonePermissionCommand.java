package ZoneSystem;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class SubZonePermissionCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be executed by a player.");
            return true;
        }

        ZoneManager zoneManager = ZonePlugin.getInstance().getZoneManager();

        if (args.length < 2) {
            player.sendMessage("§cUsage: /subzone permissions <PlayerName>#<Zone>.<SubZone> <list|set>");
            return true;
        }

        String[] parts = args[0].split("#");
        if (parts.length != 2) {
            player.sendMessage("§cInvalid format. Example: Player#1.1");
            return true;
        }

        String targetPlayer = parts[0];
        String[] zoneParts = parts[1].split("\\.");
        if (zoneParts.length != 2) {
            player.sendMessage("§cInvalid format. Example: Player#1.1");
            return true;
        }

        int mainZoneNumber, subZoneNumber;
        try {
            mainZoneNumber = Integer.parseInt(zoneParts[0]);
            subZoneNumber = Integer.parseInt(zoneParts[1]);
        } catch (NumberFormatException e) {
            player.sendMessage("§cInvalid zone number!");
            return true;
        }

        SubZone subZone = zoneManager.getSubZoneByNumbers(targetPlayer, mainZoneNumber, subZoneNumber);
        if (subZone == null) {
            player.sendMessage("§cSubzone not found!");
            return true;
        }

        if (!subZone.isOwner(player.getUniqueId())) {
            player.sendMessage("§cYou can only manage permissions for your own subzones!");
            return true;
        }

        String action = args[1].toLowerCase();

        if (action.equals("list")) {
            ZonePermissionEntry entry = zoneManager.getPermissionEntry(
                    subZone.getOwnerUUID(),
                    mainZoneNumber,
                    subZoneNumber
            );

            player.sendMessage("§6§l=== Permissions for SubZone " + subZone.getFullZoneName() + " ===");

            if (entry == null) {
                player.sendMessage("§7No custom permissions set.");
            } else {
                player.sendMessage("§eGlobal Permissions:");
                for (ZonePermission perm : ZonePermission.values()) {
                    Boolean value = entry.getGlobalPermission(perm);
                    if (value != null) {
                        player.sendMessage("  §7- " + perm.getKey() + ": " + (value ? "§a✓" : "§c✗"));
                    }
                }

                player.sendMessage("§eUser Permissions:");
                entry.getUserPermissions().forEach((uuid, perms) -> {
                    String userName = Bukkit.getOfflinePlayer(uuid).getName();
                    player.sendMessage("  §b" + userName + ":");
                    perms.forEach((perm, value) ->
                            player.sendMessage("    §7- " + perm.getKey() + ": " + (value ? "§a✓" : "§c✗"))
                    );
                });
            }
            return true;
        }

        if (action.equals("set")) {
            if (args.length != 5) {
                player.sendMessage("§cUsage: /subzone permissions <SubZone> set <Player|*> <Permission|*> <true|false>");
                return true;
            }

            String targetUser = args[2];
            String permissionName = args[3];
            boolean value = Boolean.parseBoolean(args[4]);

            UUID targetUUID = null;
            if (!targetUser.equals("*")) {
                Player target = Bukkit.getPlayer(targetUser);
                if (target == null) {
                    player.sendMessage("§cPlayer not found!");
                    return true;
                }
                targetUUID = target.getUniqueId();
            }

            if (permissionName.equals("*")) {
                for (ZonePermission perm : ZonePermission.values()) {
                    zoneManager.setZonePermission(
                            subZone.getOwnerUUID(),
                            mainZoneNumber,
                            subZoneNumber,
                            targetUUID,
                            perm,
                            value
                    );
                }
                player.sendMessage("§aAll permissions for " + (targetUser.equals("*") ? "everyone" : targetUser) +
                        " set to §f" + value + "§a!");
            } else {
                ZonePermission permission = ZonePermission.fromString(permissionName);
                if (permission == null) {
                    player.sendMessage("§cInvalid permission! Available: " +
                            String.join(", ", java.util.Arrays.stream(ZonePermission.values())
                                    .map(ZonePermission::getKey).toArray(String[]::new)));
                    return true;
                }

                zoneManager.setZonePermission(
                        subZone.getOwnerUUID(),
                        mainZoneNumber,
                        subZoneNumber,
                        targetUUID,
                        permission,
                        value
                );

                player.sendMessage("§aPermission §f" + permissionName + " §afor " +
                        (targetUser.equals("*") ? "everyone" : targetUser) + " set to §f" + value + "§a!");
            }
            return true;
        }

        player.sendMessage("§cUnknown command. Use: list | set");
        return true;
    }
}
