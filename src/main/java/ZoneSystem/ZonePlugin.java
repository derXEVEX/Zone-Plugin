package ZoneSystem;

import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;


public class ZonePlugin extends JavaPlugin {

    private static ZonePlugin instance;
    private ZoneManager zoneManager;
    private ZoneListener zoneListener;
    private SubZoneListener subZoneListener;
    private ZoneVisualizer zoneVisualizer;
    private SubZoneVisualizer subZoneVisualizer;
    private PlayerZoneVisualizer playerZoneVisualizer;

    @Override
    public void onEnable() {
        instance = this;
        this.zoneManager = new ZoneManager();
        this.zoneVisualizer = new ZoneVisualizer();
        this.subZoneVisualizer = new SubZoneVisualizer();
        zoneManager.loadZones();
        this.playerZoneVisualizer = new PlayerZoneVisualizer();

        getCommand("zone").setExecutor(new ZoneCommand());
        getCommand("zone").setTabCompleter(new ZoneTabCompleter());


        getCommand("subzone").setExecutor(new SubZoneCommand());
        getCommand("subzone").setTabCompleter(new SubZoneTabCompleter());

        this.zoneListener = new ZoneListener();
        this.subZoneListener = new SubZoneListener();


        getServer().getPluginManager().registerEvents(zoneListener, this);
        getServer().getPluginManager().registerEvents(subZoneListener, this);
        getServer().getPluginManager().registerEvents(new ZoneProtectionListener(zoneManager), this);

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

    public ZoneListener getZoneListener() {
        return zoneListener;
    }

    public SubZoneListener getSubZoneListener() {
        return subZoneListener;
    }

    public ItemStack getZoneTool() {
        return ZoneListener.getZoneTool();
    }

    public ItemStack getSubZoneTool() {
        return SubZoneListener.getSubZoneTool();
    }

    public ZoneVisualizer getZoneVisualizer() { return zoneVisualizer; }

    public SubZoneVisualizer getSubZoneVisualizer() { return subZoneVisualizer; }

    public PlayerZoneVisualizer getPlayerZoneVisualizer() {
        return playerZoneVisualizer;
    }


}
