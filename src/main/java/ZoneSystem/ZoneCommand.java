package ZoneSystem;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class ZoneCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cDieser Befehl kann nur von einem Spieler ausgeführt werden.");
            return true;
        }

        Player player = (Player) sender;
        UUID playerId = player.getUniqueId();
        ZonePlugin plugin = ZonePlugin.getInstance();
        ZoneManager zoneManager = plugin.getZoneManager();
        ZoneListener listener = plugin.getZoneListener();

        ZoneSelection selection = listener.getSelection(playerId);

        if (args.length == 0) {
            player.sendMessage("§cVerwendung: /zone create | /zone confirm");
            return true;
        }

        if (args[0].equalsIgnoreCase("create")) {
            player.getInventory().addItem(plugin.getZoneTool());
            player.sendMessage("§aDamit kannst du die Zone erstellen. Links-Klick setzt Position 1 | Rechts-Klick setzt Position 2");
            return true;
        }

        if (args[0].equalsIgnoreCase("confirm")) {
            if (selection == null || !selection.isComplete()) {
                player.sendMessage("§cDu musst erst zwei Punkte setzen!");
                return true;
            }

            Zone zone = selection.toZone(playerId);

            if (!zoneManager.canCreateZone(zone)) {
                player.sendMessage("§cDiese Zone überschneidet sich mit einer anderen!");
                return true;
            }

            zoneManager.addZone(zone);

            ItemStack zoneTool = plugin.getZoneTool();
            player.getInventory().removeItem(zoneTool);

            player.sendMessage("§aZone erfolgreich erstellt!");
            return true;
        }

        return false;
    }
}
