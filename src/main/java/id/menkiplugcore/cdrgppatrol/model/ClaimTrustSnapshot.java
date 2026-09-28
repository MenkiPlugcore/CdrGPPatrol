package id.menkiplugcore.cdrgppatrol.model;

import java.util.ArrayList;
import java.util.List;

public record ClaimTrustSnapshot(
        List<TrustEntry> managers,
        List<TrustEntry> builders,
        List<TrustEntry> containers,
        List<TrustEntry> accessors
) {
    public int totalEntries() {
        return managers.size() + builders.size() + containers.size() + accessors.size();
    }

    public List<TrustEntry> allEntries() {
        List<TrustEntry> entries = new ArrayList<>(totalEntries());
        entries.addAll(managers);
        entries.addAll(builders);
        entries.addAll(containers);
        entries.addAll(accessors);
        return List.copyOf(entries);
    }
}
