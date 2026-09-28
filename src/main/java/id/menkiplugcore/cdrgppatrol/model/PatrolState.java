package id.menkiplugcore.cdrgppatrol.model;

public final class PatrolState {
    private String ownerQuery = "";
    private String worldName = "";
    private ClaimType claimType = ClaimType.ALL;
    private ClaimSort sort = ClaimSort.WORLD_ID;
    private int abandonedDays;

    public String ownerQuery() {
        return ownerQuery;
    }

    public void ownerQuery(String ownerQuery) {
        this.ownerQuery = ownerQuery == null ? "" : ownerQuery.trim();
    }

    public String worldName() {
        return worldName;
    }

    public void worldName(String worldName) {
        this.worldName = worldName == null ? "" : worldName.trim();
    }

    public ClaimType claimType() {
        return claimType;
    }

    public void claimType(ClaimType claimType) {
        this.claimType = claimType == null ? ClaimType.ALL : claimType;
    }

    public ClaimSort sort() {
        return sort;
    }

    public void sort(ClaimSort sort) {
        this.sort = sort == null ? ClaimSort.WORLD_ID : sort;
    }

    public int abandonedDays() {
        return abandonedDays;
    }

    public void abandonedDays(int abandonedDays) {
        this.abandonedDays = Math.max(0, abandonedDays);
        if (this.abandonedDays > 0 && claimType == ClaimType.ADMIN) {
            claimType = ClaimType.PLAYER;
        }
    }

    public boolean hasAbandonedScanner() {
        return abandonedDays > 0;
    }

    public void cycleAbandonedPreset() {
        abandonedDays = switch (abandonedDays) {
            case 0 -> 7;
            case 7 -> 30;
            case 30 -> 60;
            case 60 -> 90;
            default -> 0;
        };
        if (abandonedDays > 0) {
            claimType = ClaimType.PLAYER;
        }
    }

    public boolean hasSearch() {
        return !ownerQuery.isBlank();
    }

    public boolean hasWorldFilter() {
        return !worldName.isBlank();
    }

    public boolean hasFilters() {
        return hasSearch() || hasWorldFilter() || claimType != ClaimType.ALL || hasAbandonedScanner();
    }

    public void reset() {
        ownerQuery = "";
        worldName = "";
        claimType = ClaimType.ALL;
        sort = ClaimSort.WORLD_ID;
        abandonedDays = 0;
    }
}
