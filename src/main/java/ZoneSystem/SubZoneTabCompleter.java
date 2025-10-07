package ZoneSystem;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SubZoneTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player)) return null;

        ZoneManager zoneManager = ZonePlugin.getInstance().getZoneManager();
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            return Arrays.asList("create", "confirm", "delete", "permissions", "cancel")
                    .stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }


        if (args.length == 2 && args[0].equalsIgnoreCase("create")) {
            return zoneManager.getZonesForPlayer(player.getUniqueId())
                    .stream()
                    .map(z -> player.getName() + "#" + z.getZoneNumber())
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("delete")) {
            List<String> subZones = new ArrayList<>();
            zoneManager.getZonesForPlayer(player.getUniqueId()).forEach(zone -> {
                int subCount = zoneManager.getSubZoneCountForZone(player.getUniqueId(), zone.getZoneNumber());
                for (int i = 1; i <= subCount; i++) {
                    subZones.add(player.getName() + "#" + zone.getZoneNumber() + "." + i);
                }
            });
            return subZones.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args[0].equalsIgnoreCase("permissions")) {
            if (args.length == 2) {
                List<String> zones = new ArrayList<>();
                zoneManager.getZonesForPlayer(player.getUniqueId()).forEach(zone -> {
                    int subCount = zoneManager.getSubZoneCountForZone(player.getUniqueId(), zone.getZoneNumber());
                    for (int i = 1; i <= subCount; i++) {
                        zones.add(player.getName() + "#" + zone.getZoneNumber() + "." + i);
                    }
                });
                return zones.stream()
                        .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }

            if (args.length == 3) {
                return Arrays.asList("list", "set")
                        .stream()
                        .filter(s -> s.toLowerCase().startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
            }

            if (args[2].equalsIgnoreCase("set")) {
                if (args.length == 4) {
                    suggestions.add("*");
                    Bukkit.getOnlinePlayers().forEach(p -> suggestions.add(p.getName()));
                    return suggestions.stream()
                            .filter(s -> s.toLowerCase().startsWith(args[3].toLowerCase()))
                            .collect(Collectors.toList());
                }

                if (args.length == 5) {
                    suggestions.add("*");
                    Arrays.stream(ZonePermission.values())
                            .forEach(perm -> suggestions.add(perm.getKey()));
                    return suggestions.stream()
                            .filter(s -> s.toLowerCase().startsWith(args[4].toLowerCase()))
                            .collect(Collectors.toList());
                }

                if (args.length == 6) {
                    return Arrays.asList("true", "false")
                            .stream()
                            .filter(s -> s.toLowerCase().startsWith(args[5].toLowerCase()))
                            .collect(Collectors.toList());
                }
            }
        }

        return suggestions;
    }
}
