package ZoneSystem;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ZoneCreateEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Zone zone;

    public ZoneCreateEvent(Zone zone) {
        this.zone = zone;
    }

    public Zone getZone() {
        return zone;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}