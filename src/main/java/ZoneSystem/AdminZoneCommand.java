package ZoneSystem;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.boss.BarColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class AdminZoneCommand implements CommandExecutor {

    private final HashMap<UUID, AdminZoneCreation> adminCreations = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be executed by a player.");
            return true;
        }

        if (!player.hasPermission("zone.admin")) {
            player.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage("§cUsage: /zone admin create | confirm <Name> <Owner> [countTowardsLimit:true/false]");
            return true;
        }

        UUID adminId = player.getUniqueId();
        ZonePlugin plugin = ZonePlugin.getInstance();
        ZoneManager zoneManager = plugin.getZoneManager();
        ZoneListener listener = plugin.getZoneListener();

        if (args.length < 2) {
            player.sendMessage("§cUsage: /zone admin <create|confirm|cancel>");
            return true;
        }

        String subCommand = args[1].toLowerCase();

        switch (args[1].toLowerCase()) {
            case "create":
                player.getInventory().addItem(plugin.getZoneTool());
                adminCreations.put(adminId, new AdminZoneCreation());
                player.sendMessage("§6[Admin] §aSelect the zone area with the tool.");
                return true;

            case "confirm":
                if (args.length != 5) {
                    player.sendMessage("§cUsage: /zone admin confirm <CustomName> <OwnerName> <true|false>");
                    return true;
                }

                String customName = args[2];
                String ownerName = args[3];
                boolean countTowardsLimit;

                try {
                    countTowardsLimit = Boolean.parseBoolean(args[4]);
                } catch (Exception e) {
                    player.sendMessage("§cInvalid boolean value! Use 'true' or 'false'.");
                    return true;
                }

                Player targetPlayer = Bukkit.getPlayer(ownerName);
                UUID targetUUID;
                String targetName;

                if (targetPlayer != null) {
                    targetUUID = targetPlayer.getUniqueId();
                    targetName = targetPlayer.getName();
                } else {
                    OfflinePlayer offlinePlayer = Arrays.stream(Bukkit.getOfflinePlayers())
                            .filter(p -> p.getName() != null && p.getName().equalsIgnoreCase(ownerName))
                            .findFirst()
                            .orElse(null);

                    if (offlinePlayer == null || !offlinePlayer.hasPlayedBefore()) {
                        player.sendMessage("§cPlayer not found! Make sure the name is correct.");
                        return true;
                    }

                    targetUUID = offlinePlayer.getUniqueId();
                    targetName = offlinePlayer.getName();
                }

                ZoneSelection selection = listener.getSelection(adminId);
                if (selection == null || !selection.isComplete()) {
                    player.sendMessage("§cYou must set two points first!");
                    return true;
                }

                if (!selection.isValidSize()) {
                    player.sendMessage("§cThe zone must be at least 8x8 blocks in size!");
                    return true;
                }

                Zone zone = selection.toZone(targetUUID, targetName);
                zone.setCustomName(customName);

                if (!zoneManager.canCreateZone(zone)) {
                    player.sendMessage("§cThis zone overlaps with another zone!");
                    return true;
                }

                zoneManager.addAdminZone(zone, countTowardsLimit);
                Bukkit.getPluginManager().callEvent(new ZoneCreateEvent(zone));
                listener.clearSelection(adminId);
                plugin.getZoneVisualizer().stopVisualization(player);
                player.getInventory().removeItem(plugin.getZoneTool());

                player.sendMessage("§aAdmin zone successfully created!");
                player.sendMessage("§eOwner: §f" + targetName + (targetPlayer == null ? " §7(offline)" : ""));
                player.sendMessage("§eCustom Name: §f" + customName);
                player.sendMessage("§eSize: §f" + zone.getArea() + " blocks");
                player.sendMessage("§eCounts towards limit: §f" + countTowardsLimit);
                return true;



            case "cancel":
                listener.clearSelection(adminId);
                plugin.getZoneVisualizer().stopVisualization(player);
                player.getInventory().removeItem(plugin.getZoneTool());
                adminCreations.remove(adminId);
                player.sendMessage("§6[Admin] §eZone creation cancelled.");
                return true;

            default:
                player.sendMessage("§cUnknown admin command. Use: create | confirm | delete | cancel");
                return true;
        }
    }

    private static class AdminZoneCreation {
        private String zoneName;
        private boolean countTowardsLimit = true;


        public void setZoneName(String zoneName) {
            this.zoneName = zoneName;
        }

        public void setCountTowardsLimit(boolean countTowardsLimit) {
            this.countTowardsLimit = countTowardsLimit;
        }

    }
}
