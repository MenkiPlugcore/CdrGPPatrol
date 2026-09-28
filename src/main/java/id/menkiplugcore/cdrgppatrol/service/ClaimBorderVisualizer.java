package id.menkiplugcore.cdrgppatrol.service;

import id.menkiplugcore.cdrgppatrol.CdrGPPatrol;
import me.ryanhamshire.GriefPrevention.Claim;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ClaimBorderVisualizer {
    private final CdrGPPatrol plugin;
    private final Map<UUID, VisualizationSession> sessions = new HashMap<>();

    public ClaimBorderVisualizer(CdrGPPatrol plugin) {
        this.plugin = plugin;
    }

    public boolean toggle(Player player, Claim claim) {
        if (isVisualizing(player, claim)) {
            stop(player, true);
            return false;
        }

        return start(player, claim);
    }

    public boolean start(Player player, Claim claim) {
        if (claim == null) {
            return false;
        }

        Location lesser = claim.getLesserBoundaryCorner();
        Location greater = claim.getGreaterBoundaryCorner();
        World world = lesser.getWorld();

        if (!player.getWorld().equals(world)) {
            player.sendMessage(plugin.prefix() + " §cKamu harus berada di world claim untuk melihat bordernya.");
            return false;
        }

        double centerX = (lesser.getBlockX() + greater.getBlockX()) / 2.0 + 0.5;
        double centerZ = (lesser.getBlockZ() + greater.getBlockZ()) / 2.0 + 0.5;
        double dx = player.getLocation().getX() - centerX;
        double dz = player.getLocation().getZ() - centerZ;
        int maxDistance = Math.max(16, plugin.getConfig().getInt("visualizer.max-distance", 96));

        if ((dx * dx) + (dz * dz) > (double) maxDistance * maxDistance) {
            player.sendMessage(plugin.prefix() + " §cTerlalu jauh dari claim. Teleport ke claim dulu lalu aktifkan visualizer.");
            return false;
        }

        stop(player, false);

        int durationSeconds = Math.max(3, plugin.getConfig().getInt("visualizer.duration-seconds", 15));
        int intervalTicks = Math.max(2, plugin.getConfig().getInt("visualizer.refresh-ticks", 10));
        long endAt = System.currentTimeMillis() + (durationSeconds * 1000L);
        UUID playerId = player.getUniqueId();

        BukkitTask task = plugin.getServer().getScheduler().runTaskTimer(
                plugin,
                () -> renderSession(playerId),
                0L,
                intervalTicks
        );

        sessions.put(playerId, new VisualizationSession(claim, task, endAt));
        player.sendMessage(plugin.prefix() + " §aBorder claim ditampilkan selama §f" + durationSeconds + " detik§a. Klik lagi untuk berhenti.");
        return true;
    }

    public boolean isVisualizing(Player player, Claim claim) {
        VisualizationSession session = sessions.get(player.getUniqueId());
        return session != null && sameClaim(session.claim(), claim);
    }

    public long remainingSeconds(Player player, Claim claim) {
        VisualizationSession session = sessions.get(player.getUniqueId());
        if (session == null || !sameClaim(session.claim(), claim)) {
            return 0L;
        }
        return Math.max(0L, (session.endAtMillis() - System.currentTimeMillis() + 999L) / 1000L);
    }

    public void stop(Player player, boolean notify) {
        VisualizationSession session = sessions.remove(player.getUniqueId());
        if (session == null) {
            return;
        }

        session.task().cancel();
        if (notify) {
            player.sendMessage(plugin.prefix() + " §7Claim border visualizer dihentikan.");
        }
    }

    public void stop(UUID playerId) {
        VisualizationSession session = sessions.remove(playerId);
        if (session != null) {
            session.task().cancel();
        }
    }

    public void stopAll() {
        for (VisualizationSession session : sessions.values()) {
            session.task().cancel();
        }
        sessions.clear();
    }

    private void renderSession(UUID playerId) {
        VisualizationSession session = sessions.get(playerId);
        if (session == null) {
            return;
        }

        Player player = plugin.getServer().getPlayer(playerId);
        if (player == null || !player.isOnline()) {
            stop(playerId);
            return;
        }

        if (System.currentTimeMillis() >= session.endAtMillis()) {
            stop(player, false);
            player.sendMessage(plugin.prefix() + " §7Claim border visualizer selesai.");
            return;
        }

        Claim claim = session.claim();
        World world = claim.getLesserBoundaryCorner().getWorld();
        if (!player.getWorld().equals(world)) {
            stop(player, false);
            player.sendMessage(plugin.prefix() + " §7Visualizer dihentikan karena kamu berpindah world.");
            return;
        }

        renderBorder(player, claim);
    }

    private void renderBorder(Player player, Claim claim) {
        Location lesser = claim.getLesserBoundaryCorner();
        Location greater = claim.getGreaterBoundaryCorner();
        World world = lesser.getWorld();

        int minX = Math.min(lesser.getBlockX(), greater.getBlockX());
        int maxX = Math.max(lesser.getBlockX(), greater.getBlockX());
        int minZ = Math.min(lesser.getBlockZ(), greater.getBlockZ());
        int maxZ = Math.max(lesser.getBlockZ(), greater.getBlockZ());

        int width = Math.max(1, maxX - minX + 1);
        int depth = Math.max(1, maxZ - minZ + 1);
        int perimeter = Math.max(1, 2 * (width + depth));

        int baseSpacing = Math.max(1, plugin.getConfig().getInt("visualizer.spacing", 2));
        int maxPoints = Math.max(32, plugin.getConfig().getInt("visualizer.max-points-per-pass", 320));
        int adaptiveSpacing = Math.max(baseSpacing, (int) Math.ceil(perimeter / (double) maxPoints));

        Particle.DustOptions borderDust = new Particle.DustOptions(readColor(), readParticleSize());
        Particle.DustOptions cornerDust = new Particle.DustOptions(readColor(), Math.min(4.0f, readParticleSize() + 0.45f));

        drawXEdge(player, world, minX, maxX, minZ, adaptiveSpacing, borderDust);
        if (maxZ != minZ) {
            drawXEdge(player, world, minX, maxX, maxZ, adaptiveSpacing, borderDust);
        }
        drawZEdge(player, world, minZ, maxZ, minX, adaptiveSpacing, borderDust);
        if (maxX != minX) {
            drawZEdge(player, world, minZ, maxZ, maxX, adaptiveSpacing, borderDust);
        }

        drawCornerPillar(player, world, minX, minZ, cornerDust);
        drawCornerPillar(player, world, minX, maxZ, cornerDust);
        drawCornerPillar(player, world, maxX, minZ, cornerDust);
        drawCornerPillar(player, world, maxX, maxZ, cornerDust);
    }

    private void drawXEdge(Player player, World world, int minX, int maxX, int z, int spacing,
                           Particle.DustOptions dust) {
        for (int x = minX; x <= maxX; x += spacing) {
            spawnSurfaceParticle(player, world, x, z, dust);
        }
        if ((maxX - minX) % spacing != 0) {
            spawnSurfaceParticle(player, world, maxX, z, dust);
        }
    }

    private void drawZEdge(Player player, World world, int minZ, int maxZ, int x, int spacing,
                           Particle.DustOptions dust) {
        for (int z = minZ; z <= maxZ; z += spacing) {
            spawnSurfaceParticle(player, world, x, z, dust);
        }
        if ((maxZ - minZ) % spacing != 0) {
            spawnSurfaceParticle(player, world, x, maxZ, dust);
        }
    }

    private void spawnSurfaceParticle(Player player, World world, int x, int z, Particle.DustOptions dust) {
        if (!world.isChunkLoaded(x >> 4, z >> 4)) {
            return;
        }

        double y = world.getHighestBlockYAt(x, z) + 1.15;
        player.spawnParticle(Particle.DUST, new Location(world, x + 0.5, y, z + 0.5), 1, dust);
    }

    private void drawCornerPillar(Player player, World world, int x, int z, Particle.DustOptions dust) {
        if (!world.isChunkLoaded(x >> 4, z >> 4)) {
            return;
        }

        double baseY = world.getHighestBlockYAt(x, z) + 1.15;
        double height = Math.max(1.0, plugin.getConfig().getDouble("visualizer.corner-pillar-height", 4.0));
        for (double offset = 0.0; offset <= height; offset += 0.5) {
            player.spawnParticle(Particle.DUST, new Location(world, x + 0.5, baseY + offset, z + 0.5), 1, dust);
        }
    }

    private Color readColor() {
        int red = clampColor(plugin.getConfig().getInt("visualizer.color.red", 40));
        int green = clampColor(plugin.getConfig().getInt("visualizer.color.green", 210));
        int blue = clampColor(plugin.getConfig().getInt("visualizer.color.blue", 255));
        return Color.fromRGB(red, green, blue);
    }

    private float readParticleSize() {
        double configured = plugin.getConfig().getDouble("visualizer.particle-size", 1.0);
        return (float) Math.max(0.25, Math.min(4.0, configured));
    }

    private int clampColor(int value) {
        return Math.max(0, Math.min(255, value));
    }

    private boolean sameClaim(Claim first, Claim second) {
        if (first == second) {
            return true;
        }
        if (first == null || second == null) {
            return false;
        }
        if (first.getID() != null && second.getID() != null) {
            return first.getID().equals(second.getID());
        }
        return first.getLesserBoundaryCorner().equals(second.getLesserBoundaryCorner())
                && first.getGreaterBoundaryCorner().equals(second.getGreaterBoundaryCorner());
    }

    private record VisualizationSession(Claim claim, BukkitTask task, long endAtMillis) {
    }
}
