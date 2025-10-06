package ZoneSystem;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

public class ZoneVisualizer {
    private final HashMap<UUID, BukkitRunnable> activeVisualizations = new HashMap<>();

    public void startVisualization(Player player, ZoneSelection selection) {
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

                drawZoneBorder(player, selection);
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

    private void drawZoneBorder(Player player, ZoneSelection selection) {
        World world = player.getWorld();

        int minX = Math.min(selection.getX1(), selection.getX2());
        int maxX = Math.max(selection.getX1(), selection.getX2());
        int minZ = Math.min(selection.getZ1(), selection.getZ2());
        int maxZ = Math.max(selection.getZ1(), selection.getZ2());

        int playerY = player.getLocation().getBlockY();

        for (int y = playerY - 10; y <= playerY + 10; y++) {
            for (int x = minX; x <= maxX; x += 2) {
                spawnParticle(world, x + 0.5, y, minZ + 0.5);
                spawnParticle(world, x + 0.5, y, maxZ + 0.5);
            }

            for (int z = minZ; z <= maxZ; z += 2) {
                spawnParticle(world, minX + 0.5, y, z + 0.5);
                spawnParticle(world, maxX + 0.5, y, z + 0.5);
            }
        }

        drawCornerPillars(world, minX, maxX, minZ, maxZ, playerY);
    }

    private void drawCornerPillars(World world, int minX, int maxX, int minZ, int maxZ, int centerY) {
        for (int y = centerY - 15; y <= centerY + 15; y++) {
            spawnHighlightParticle(world, minX + 0.5, y, minZ + 0.5);
            spawnHighlightParticle(world, minX + 0.5, y, maxZ + 0.5);
            spawnHighlightParticle(world, maxX + 0.5, y, minZ + 0.5);
            spawnHighlightParticle(world, maxX + 0.5, y, maxZ + 0.5);
        }
    }

    private void spawnParticle(World world, double x, double y, double z) {
        Location loc = new Location(world, x, y, z);
        world.spawnParticle(Particle.FLAME, loc, 1, 0, 0, 0, 0);
    }

    private void spawnHighlightParticle(World world, double x, double y, double z) {
        Location loc = new Location(world, x, y, z);
        world.spawnParticle(Particle.END_ROD, loc, 2, 0, 0, 0, 0.01);
    }
}
