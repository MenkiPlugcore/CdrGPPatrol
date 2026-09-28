package id.menkiplugcore.cdrgppatrol.gui;

import me.ryanhamshire.GriefPrevention.Claim;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public final class TrustMenuHolder implements InventoryHolder {
    private final Claim claim;
    private final int sourcePage;
    private final int page;
    private final int totalPages;
    private Inventory inventory;

    public TrustMenuHolder(Claim claim, int sourcePage, int page, int totalPages) {
        this.claim = claim;
        this.sourcePage = sourcePage;
        this.page = page;
        this.totalPages = totalPages;
    }

    public Claim claim() {
        return claim;
    }

    public int sourcePage() {
        return sourcePage;
    }

    public int page() {
        return page;
    }

    public int totalPages() {
        return totalPages;
    }

    public void inventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public @NotNull Inventory getInventory() {
        if (inventory == null) {
            throw new IllegalStateException("Trust viewer inventory has not been initialized yet.");
        }
        return inventory;
    }
}
