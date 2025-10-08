package ZoneSystem;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class PlayerZoneVisualizer {
    private final Map<UUID, BukkitRunnable> activeTasks = new HashMap<>();

    public void showZones(Player player, boolean onlyOwn) {
        stopVisualization(player);

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    stopVisualization(player);
                    return;
                }

                ZoneManager zoneManager = ZonePlugin.getInstance().getZoneManager();
                for (Zone zone : zoneManager.getAllZones()) {
                    if (onlyOwn && !zone.isOwner(player.getUniqueId())) continue;
                    drawZoneBorder(player, zone);
                }
            }
        };

        task.runTaskTimer(ZonePlugin.getInstance(), 0L, 5L);
        activeTasks.put(player.getUniqueId(), task);
    }

    private void drawZoneBorder(Player player, Zone zone) {
        int playerY = player.getLocation().getBlockY();
        int step = 2; // Dichtere Partikel

        // Zeichne auf mehreren Höhen (unterhalb und oberhalb des Spielers)
        for (int yOffset = -3; yOffset <= 6; yOffset += 2) {
            int y = playerY + yOffset;

            // Horizontale Linien
            for (int x = zone.getMinX(); x <= zone.getMaxX(); x += step) {
                spawnParticle(player, x + 0.5, y, zone.getMinZ() + 0.5);
                spawnParticle(player, x + 0.5, y, zone.getMaxZ() + 0.5);
            }

            for (int z = zone.getMinZ(); z <= zone.getMaxZ(); z += step) {
                spawnParticle(player, zone.getMinX() + 0.5, y, z + 0.5);
                spawnParticle(player, zone.getMaxX() + 0.5, y, z + 0.5);
            }
        }

        // Vertikale Ecken-Säulen (deutlich sichtbar)
        for (int y = playerY - 3; y <= playerY + 6; y++) {
            spawnHighlightParticle(player, zone.getMinX() + 0.5, y, zone.getMinZ() + 0.5);
            spawnHighlightParticle(player, zone.getMinX() + 0.5, y, zone.getMaxZ() + 0.5);
            spawnHighlightParticle(player, zone.getMaxX() + 0.5, y, zone.getMinZ() + 0.5);
            spawnHighlightParticle(player, zone.getMaxX() + 0.5, y, zone.getMaxZ() + 0.5);
        }
    }

    private void spawnParticle(Player player, double x, double y, double z) {
        player.spawnParticle(Particle.FLAME, x, y, z, 2, 0, 0, 0, 0);
    }

    private void spawnHighlightParticle(Player player, double x, double y, double z) {
        player.spawnParticle(Particle.END_ROD, x, y, z, 5, 0.1, 0.1, 0.1, 0.02);
    }

    public void stopVisualization(Player player) {
        UUID playerId = player.getUniqueId();
        if (activeTasks.containsKey(playerId)) {
            activeTasks.get(playerId).cancel();
            activeTasks.remove(playerId);
        }
    }
}
