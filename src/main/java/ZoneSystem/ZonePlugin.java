package ZoneSystem;

import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class ZonePlugin extends JavaPlugin {

    private static ZonePlugin instance;
    private ZoneManager zoneManager;
    private ZoneListener zoneListener;

    @Override
    public void onEnable() {
        instance = this;
        this.zoneManager = new ZoneManager();
        zoneManager.loadZones();

        getCommand("zone").setExecutor(new ZoneCommand());

        this.zoneListener = new ZoneListener();
        getServer().getPluginManager().registerEvents(zoneListener, this);
    }


    @Override
    public void onDisable() {
        zoneManager.saveZones();
    }

    public static ZonePlugin getInstance() {
        return instance;
    }

    public ZoneManager getZoneManager() {
        return zoneManager;
    }

    public ItemStack getZoneTool() {
        return ZoneListener.getZoneTool();
    }

    public ZoneListener getZoneListener() {
        return this.zoneListener;
    }


}
