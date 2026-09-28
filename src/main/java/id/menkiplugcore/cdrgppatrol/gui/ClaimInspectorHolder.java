package id.menkiplugcore.cdrgppatrol.gui;

import me.ryanhamshire.GriefPrevention.Claim;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public final class ClaimInspectorHolder implements InventoryHolder {
    private final Claim claim;
    private final int sourcePage;
    private Inventory inventory;

    public ClaimInspectorHolder(Claim claim, int sourcePage) {
        this.claim = claim;
        this.sourcePage = sourcePage;
    }

    public Claim claim() {
        return claim;
    }

    public int sourcePage() {
        return sourcePage;
    }

    public void inventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public @NotNull Inventory getInventory() {
        if (inventory == null) {
            throw new IllegalStateException("Claim inspector inventory has not been initialized yet.");
        }
        return inventory;
    }
}
