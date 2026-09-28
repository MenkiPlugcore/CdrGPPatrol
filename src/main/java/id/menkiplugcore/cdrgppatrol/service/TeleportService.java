package id.menkiplugcore.cdrgppatrol.service;

import id.menkiplugcore.cdrgppatrol.CdrGPPatrol;
import id.menkiplugcore.cdrgppatrol.util.Colors;
import me.ryanhamshire.GriefPrevention.Claim;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

public final class TeleportService {
    private static final Set<Material> HAZARDS = EnumSet.of(
            Material.LAVA,
            Material.FIRE,
            Material.SOUL_FIRE,
            Material.CAMPFIRE,
            Material.SOUL_CAMPFIRE,
            Material.MAGMA_BLOCK,
            Material.CACTUS,
            Material.SWEET_BERRY_BUSH,
            Material.POWDER_SNOW
    );

    private final CdrGPPatrol plugin;
    private final ClaimService claimService;

    public TeleportService(CdrGPPatrol plugin, ClaimService claimService) {
        this.plugin = plugin;
        this.claimService = claimService;
    }

    public void teleport(Player player, Claim claim) {
        Optional<Location> safeLocation = findSafeLocation(claim);
        if (safeLocation.isEmpty()) {
            player.sendMessage(plugin.prefix() + " " + Colors.color("&cTidak menemukan lokasi teleport yang aman di claim ini."));
            return;
        }

        Location destination = safeLocation.get();
        destination.setYaw(player.getLocation().getYaw());
        destination.setPitch(player.getLocation().getPitch());
        player.teleport(destination);

        String owner = claimService.ownerName(claim);
        player.sendMessage(plugin.prefix() + " " + Colors.color("&aTeleport ke claim milik &f" + owner
                + " &7di &e" + destination.getWorld().getName()
                + " &7(&b" + destination.getBlockX() + "&7, &b" + destination.getBlockY()
                + "&7, &b" + destination.getBlockZ() + "&7)."));
    }

    private Optional<Location> findSafeLocation(Claim claim) {
        Location lesser = claim.getLesserBoundaryCorner();
        Location greater = claim.getGreaterBoundaryCorner();
        World world = lesser.getWorld();

        int minX = Math.min(lesser.getBlockX(), greater.getBlockX());
        int maxX = Math.max(lesser.getBlockX(), greater.getBlockX());
        int minZ = Math.min(lesser.getBlockZ(), greater.getBlockZ());
        int maxZ = Math.max(lesser.getBlockZ(), greater.getBlockZ());
        int centerX = Math.floorDiv(minX + maxX, 2);
        int centerZ = Math.floorDiv(minZ + maxZ, 2);

        int configuredRadius = Math.max(0, plugin.getConfig().getInt("teleport.search-radius", 12));
        int maxRadius = Math.min(configuredRadius,
                Math.max(Math.max(centerX - minX, maxX - centerX), Math.max(centerZ - minZ, maxZ - centerZ)));

        for (int radius = 0; radius <= maxRadius; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (radius > 0 && Math.abs(dx) != radius && Math.abs(dz) != radius) {
                        continue;
                    }

                    int x = centerX + dx;
                    int z = centerZ + dz;
                    if (x < minX || x > maxX || z < minZ || z > maxZ) {
                        continue;
                    }

                    Optional<Location> candidate = safeColumnLocation(world, x, z);
                    if (candidate.isPresent()) {
                        return candidate;
                    }
                }
            }
        }

        return Optional.empty();
    }

    private Optional<Location> safeColumnLocation(World world, int x, int z) {
        int groundY = world.getHighestBlockYAt(x, z, HeightMap.MOTION_BLOCKING_NO_LEAVES);
        if (groundY < world.getMinHeight() || groundY + 2 >= world.getMaxHeight()) {
            return Optional.empty();
        }

        Block ground = world.getBlockAt(x, groundY, z);
        Block feet = world.getBlockAt(x, groundY + 1, z);
        Block head = world.getBlockAt(x, groundY + 2, z);

        if (!ground.getType().isSolid() || !feet.isPassable() || !head.isPassable()) {
            return Optional.empty();
        }

        if (feet.isLiquid() || head.isLiquid()) {
            return Optional.empty();
        }

        if (plugin.getConfig().getBoolean("teleport.reject-hazards", true)
                && (HAZARDS.contains(ground.getType()) || HAZARDS.contains(feet.getType()) || HAZARDS.contains(head.getType()))) {
            return Optional.empty();
        }

        return Optional.of(new Location(world, x + 0.5, groundY + 1.0, z + 0.5));
    }
}
