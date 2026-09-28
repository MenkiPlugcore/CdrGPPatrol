package id.menkiplugcore.cdrgppatrol.service;

import id.menkiplugcore.cdrgppatrol.model.AbandonedScanStats;
import id.menkiplugcore.cdrgppatrol.model.ClaimSort;
import id.menkiplugcore.cdrgppatrol.model.ClaimTrustSnapshot;
import id.menkiplugcore.cdrgppatrol.model.OwnerStatusSnapshot;
import id.menkiplugcore.cdrgppatrol.model.PatrolState;
import id.menkiplugcore.cdrgppatrol.model.TrustEntry;
import id.menkiplugcore.cdrgppatrol.model.TrustLevel;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class ClaimService {
    private static final long DAY_MILLIS = 86_400_000L;

    public List<Claim> getClaims() {
        return getClaims(new PatrolState());
    }

    public List<Claim> getClaims(PatrolState state) {
        Map<UUID, OwnerStatusSnapshot> statuses = state.hasAbandonedScanner()
                ? ownerStatuses()
                : Map.of();
        return getClaims(state, statuses);
    }

    public List<Claim> getClaims(PatrolState state, Map<UUID, OwnerStatusSnapshot> ownerStatuses) {
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

        if (state.hasAbandonedScanner()) {
            long now = System.currentTimeMillis();
            long thresholdMillis = Math.multiplyExact((long) state.abandonedDays(), DAY_MILLIS);
            claims.removeIf(claim -> !isAbandonedCandidate(claim, ownerStatuses, now, thresholdMillis));
            claims.sort(Comparator
                    .comparingLong((Claim claim) -> ownerStatuses.get(claim.getOwnerID()).lastSeenMillis())
                    .thenComparingLong(this::claimIdValue));
        } else {
            claims.sort(comparator(state.sort()));
        }
        return claims;
    }

    public AbandonedScanStats abandonedStats(List<Claim> claims) {
        if (claims == null || claims.isEmpty()) {
            return new AbandonedScanStats(0, 0, 0L);
        }

        Set<UUID> owners = new HashSet<>();
        long totalArea = 0L;
        for (Claim claim : claims) {
            if (claim.getOwnerID() != null) {
                owners.add(claim.getOwnerID());
            }
            totalArea += claim.getArea();
        }
        return new AbandonedScanStats(claims.size(), owners.size(), totalArea);
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

    public Map<UUID, OwnerStatusSnapshot> ownerStatuses() {
        List<Claim> claims = rawClaims();
        Map<UUID, Integer> claimCounts = new HashMap<>();
        Map<UUID, Long> totalAreas = new HashMap<>();

        for (Claim claim : claims) {
            if (claim.isAdminClaim() || claim.getOwnerID() == null) {
                continue;
            }

            UUID ownerId = claim.getOwnerID();
            claimCounts.merge(ownerId, 1, Integer::sum);
            totalAreas.merge(ownerId, (long) claim.getArea(), Long::sum);
        }

        Map<UUID, OwnerStatusSnapshot> result = new HashMap<>();
        long now = System.currentTimeMillis();

        for (Map.Entry<UUID, Integer> entry : claimCounts.entrySet()) {
            UUID ownerId = entry.getKey();
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(ownerId);
            String name = offlinePlayer.getName();
            boolean online = offlinePlayer.isOnline();
            long lastSeen = online ? now : offlinePlayer.getLastPlayed();
            boolean hasPlayedBefore = online || offlinePlayer.hasPlayedBefore() || lastSeen > 0L;

            result.put(ownerId, new OwnerStatusSnapshot(
                    ownerId,
                    name != null && !name.isBlank() ? name : ownerId.toString(),
                    online,
                    hasPlayedBefore,
                    lastSeen,
                    entry.getValue(),
                    totalAreas.getOrDefault(ownerId, 0L)
            ));
        }

        return result;
    }

    public OwnerStatusSnapshot ownerStatus(Claim claim) {
        if (claim == null || claim.isAdminClaim() || claim.getOwnerID() == null) {
            return null;
        }
        return ownerStatuses().get(claim.getOwnerID());
    }

    public ClaimTrustSnapshot getTrustSnapshot(Claim claim) {
        ArrayList<String> builders = new ArrayList<>();
        ArrayList<String> containers = new ArrayList<>();
        ArrayList<String> accessors = new ArrayList<>();
        ArrayList<String> managers = new ArrayList<>();

        claim.getPermissions(builders, containers, accessors, managers);

        return new ClaimTrustSnapshot(
                trustEntries(managers, TrustLevel.MANAGER),
                trustEntries(builders, TrustLevel.BUILD),
                trustEntries(containers, TrustLevel.CONTAINER),
                trustEntries(accessors, TrustLevel.ACCESS)
        );
    }

    public String trustSubjectName(String rawSubject) {
        if (rawSubject == null || rawSubject.isBlank()) {
            return "Unknown";
        }

        if (rawSubject.equalsIgnoreCase("public")) {
            return "Public / Everyone";
        }

        try {
            UUID uuid = UUID.fromString(rawSubject);
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            String name = offlinePlayer.getName();
            return name != null && !name.isBlank() ? name : uuid.toString();
        } catch (IllegalArgumentException ignored) {
            return rawSubject;
        }
    }

    private boolean isAbandonedCandidate(Claim claim, Map<UUID, OwnerStatusSnapshot> ownerStatuses,
                                         long nowMillis, long thresholdMillis) {
        if (claim.isAdminClaim() || claim.getOwnerID() == null) {
            return false;
        }

        OwnerStatusSnapshot status = ownerStatuses.get(claim.getOwnerID());
        if (status == null || status.online() || !status.hasKnownLastSeen()) {
            return false;
        }
        return status.offlineForMillis(nowMillis) >= thresholdMillis;
    }

    private List<TrustEntry> trustEntries(List<String> subjects, TrustLevel level) {
        return subjects.stream()
                .map(raw -> new TrustEntry(raw, trustSubjectName(raw), level))
                .sorted(Comparator.comparing(TrustEntry::displaySubject, String.CASE_INSENSITIVE_ORDER))
                .toList();
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
