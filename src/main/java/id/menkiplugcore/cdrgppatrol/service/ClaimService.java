package id.menkiplugcore.cdrgppatrol.service;

import id.menkiplugcore.cdrgppatrol.model.ClaimSort;
import id.menkiplugcore.cdrgppatrol.model.PatrolState;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public final class ClaimService {
    public List<Claim> getClaims() {
        return getClaims(new PatrolState());
    }

    public List<Claim> getClaims(PatrolState state) {
        List<Claim> claims = rawClaims();
        if (claims.isEmpty()) {
            return claims;
        }

        String ownerQuery = state.ownerQuery().toLowerCase(Locale.ROOT);
        String worldName = state.worldName();

        claims.removeIf(claim -> !state.claimType().matches(claim));

        if (!worldName.isBlank()) {
            claims.removeIf(claim -> !claim.getLesserBoundaryCorner().getWorld().getName().equalsIgnoreCase(worldName));
        }

        if (!ownerQuery.isBlank()) {
            claims.removeIf(claim -> !ownerName(claim).toLowerCase(Locale.ROOT).contains(ownerQuery));
        }

        claims.sort(comparator(state.sort()));
        return claims;
    }

    public List<String> worldNames() {
        return rawClaims().stream()
                .map(claim -> claim.getLesserBoundaryCorner().getWorld().getName())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    public List<String> ownerNames() {
        return rawClaims().stream()
                .filter(claim -> !claim.isAdminClaim() && claim.getOwnerID() != null)
                .map(this::ownerName)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    public String resolveWorld(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }

        for (String world : worldNames()) {
            if (world.equalsIgnoreCase(input)) {
                return world;
            }
        }
        return null;
    }

    public String ownerName(Claim claim) {
        if (claim == null || claim.isAdminClaim() || claim.getOwnerID() == null) {
            return "Admin Claim";
        }

        UUID ownerId = claim.getOwnerID();
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(ownerId);
        String name = offlinePlayer.getName();
        return name != null && !name.isBlank() ? name : ownerId.toString();
    }

    private List<Claim> rawClaims() {
        Collection<Claim> claims = GriefPrevention.instance.dataStore.getClaims();
        if (claims == null || claims.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(claims);
    }

    private Comparator<Claim> comparator(ClaimSort sort) {
        Comparator<Claim> byId = Comparator.comparingLong(this::claimIdValue);

        return switch (sort) {
            case OWNER_ASC -> Comparator
                    .comparing(this::ownerName, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(byId);
            case LARGEST -> Comparator
                    .comparingLong((Claim claim) -> claim.getArea())
                    .reversed()
                    .thenComparing(byId);
            case SMALLEST -> Comparator
                    .comparingLong((Claim claim) -> claim.getArea())
                    .thenComparing(byId);
            case CLAIM_ID -> byId;
            case WORLD_ID -> Comparator
                    .comparing((Claim claim) -> claim.getLesserBoundaryCorner().getWorld().getName(), String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(byId);
        };
    }

    private long claimIdValue(Claim claim) {
        return claim.getID() == null ? Long.MAX_VALUE : claim.getID();
    }
}
