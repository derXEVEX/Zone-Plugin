package ZoneSystem;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

public class SubZoneVisualizer {
    private final HashMap<UUID, BukkitRunnable> activeVisualizations = new HashMap<>();

    public void startVisualization(Player player, SubZoneSelection selection) {
        stopVisualization(player);

        if (!selection.isComplete()) {
            return;
        }

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || !selection.isComplete()) {
                    cancel();
                    activeVisualizations.remove(player.getUniqueId());
                    return;
                }
                drawSubZoneBorder(player, selection);
            }
        };

        task.runTaskTimer(ZonePlugin.getInstance(), 0L, 10L);
        activeVisualizations.put(player.getUniqueId(), task);
    }

    public void stopVisualization(Player player) {
        UUID playerId = player.getUniqueId();
        if (activeVisualizations.containsKey(playerId)) {
            activeVisualizations.get(playerId).cancel();
            activeVisualizations.remove(playerId);
        }
    }

    private void drawSubZoneBorder(Player player, SubZoneSelection selection) {
        World world = player.getWorld();

        int minX = Math.min(selection.getX1(), selection.getX2());
        int maxX = Math.max(selection.getX1(), selection.getX2());
        int minY = Math.min(selection.getY1(), selection.getY2());
        int maxY = Math.max(selection.getY1(), selection.getY2());
        int minZ = Math.min(selection.getZ1(), selection.getZ2());
        int maxZ = Math.max(selection.getZ1(), selection.getZ2());

        for (int y = minY; y <= maxY; y += 2) {
            for (int x = minX; x <= maxX; x += 2) {
                spawnParticle(world, x + 0.5, y, minZ + 0.5);
                spawnParticle(world, x + 0.5, y, maxZ + 0.5);
            }
            for (int z = minZ; z <= maxZ; z += 2) {
                spawnParticle(world, minX + 0.5, y, z + 0.5);
                spawnParticle(world, maxX + 0.5, y, z + 0.5);
            }
        }

        for (int y = minY; y <= maxY; y++) {
            spawnHighlightParticle(world, minX + 0.5, y, minZ + 0.5);
            spawnHighlightParticle(world, minX + 0.5, y, maxZ + 0.5);
            spawnHighlightParticle(world, maxX + 0.5, y, minZ + 0.5);
            spawnHighlightParticle(world, maxX + 0.5, y, maxZ + 0.5);
        }
    }

    private void spawnParticle(World world, double x, double y, double z) {
        world.spawnParticle(Particle.SOUL_FIRE_FLAME, new Location(world, x, y, z), 1, 0, 0, 0, 0);
    }

    private void spawnHighlightParticle(World world, double x, double y, double z) {
        world.spawnParticle(Particle.SOUL, new Location(world, x, y, z), 2, 0, 0, 0, 0.01);
    }
}
