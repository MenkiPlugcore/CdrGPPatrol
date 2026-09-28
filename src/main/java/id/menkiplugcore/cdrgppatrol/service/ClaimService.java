package id.menkiplugcore.cdrgppatrol.service;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public final class ClaimService {
    public List<Claim> getClaims() {
        Collection<Claim> claims = GriefPrevention.instance.dataStore.getClaims();
        if (claims == null || claims.isEmpty()) {
            return List.of();
        }

        List<Claim> result = new ArrayList<>(claims);
        result.sort(Comparator
                .comparing((Claim claim) -> claim.getLesserBoundaryCorner().getWorld().getName(), String.CASE_INSENSITIVE_ORDER)
                .thenComparingLong(claim -> claim.getID() == null ? Long.MAX_VALUE : claim.getID()));
        return result;
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
}
