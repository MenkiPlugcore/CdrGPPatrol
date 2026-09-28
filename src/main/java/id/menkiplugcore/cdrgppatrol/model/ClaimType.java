package id.menkiplugcore.cdrgppatrol.model;

import me.ryanhamshire.GriefPrevention.Claim;

import java.util.Locale;

public enum ClaimType {
    ALL("all", "Semua Claim"),
    PLAYER("player", "Player Claim"),
    ADMIN("admin", "Admin Claim");

    private final String key;
    private final String displayName;

    ClaimType(String key, String displayName) {
        this.key = key;
        this.displayName = displayName;
    }

    public String key() {
        return key;
    }

    public String displayName() {
        return displayName;
    }

    public boolean matches(Claim claim) {
        return switch (this) {
            case ALL -> true;
            case PLAYER -> !claim.isAdminClaim();
            case ADMIN -> claim.isAdminClaim();
        };
    }

    public ClaimType next() {
        ClaimType[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public static ClaimType fromInput(String input) {
        if (input == null) {
            return null;
        }

        String normalized = input.toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "all", "semua" -> ALL;
            case "player", "players" -> PLAYER;
            case "admin", "adminclaim", "admin-claim" -> ADMIN;
            default -> null;
        };
    }
}
