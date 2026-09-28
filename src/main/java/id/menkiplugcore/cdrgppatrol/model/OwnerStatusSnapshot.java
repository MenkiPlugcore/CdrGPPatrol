package id.menkiplugcore.cdrgppatrol.model;

import java.util.UUID;

public record OwnerStatusSnapshot(
        UUID ownerId,
        String ownerName,
        boolean online,
        boolean hasPlayedBefore,
        long lastSeenMillis,
        int claimCount,
        long totalArea
) {
    public boolean hasKnownLastSeen() {
        return lastSeenMillis > 0L;
    }

    public long offlineForMillis(long nowMillis) {
        if (online || !hasKnownLastSeen()) {
            return 0L;
        }
        return Math.max(0L, nowMillis - lastSeenMillis);
    }
}
