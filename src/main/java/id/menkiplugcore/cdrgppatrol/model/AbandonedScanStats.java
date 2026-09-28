package id.menkiplugcore.cdrgppatrol.model;

public record AbandonedScanStats(
        int claimCount,
        int ownerCount,
        long totalArea
) {
}
