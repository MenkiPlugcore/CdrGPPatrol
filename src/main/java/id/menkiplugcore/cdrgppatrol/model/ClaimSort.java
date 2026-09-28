package id.menkiplugcore.cdrgppatrol.model;

import java.util.Locale;

public enum ClaimSort {
    WORLD_ID("world", "World + Claim ID"),
    OWNER_ASC("owner", "Owner A-Z"),
    LARGEST("largest", "Area Terbesar"),
    SMALLEST("smallest", "Area Terkecil"),
    CLAIM_ID("id", "Claim ID");

    private final String key;
    private final String displayName;

    ClaimSort(String key, String displayName) {
        this.key = key;
        this.displayName = displayName;
    }

    public String key() {
        return key;
    }

    public String displayName() {
        return displayName;
    }

    public ClaimSort next() {
        ClaimSort[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public static ClaimSort fromInput(String input) {
        if (input == null) {
            return null;
        }

        String normalized = input.toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "world", "default" -> WORLD_ID;
            case "owner", "a-z", "az" -> OWNER_ASC;
            case "largest", "besar", "desc" -> LARGEST;
            case "smallest", "kecil", "asc" -> SMALLEST;
            case "id", "claimid", "claim-id" -> CLAIM_ID;
            default -> null;
        };
    }
}
