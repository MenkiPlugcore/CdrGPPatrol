package id.menkiplugcore.cdrgppatrol.gui;

import me.ryanhamshire.GriefPrevention.Claim;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public final class PatrolMenuHolder implements InventoryHolder {
    private final int page;
    private final int totalPages;
    private final Map<Integer, Claim> claimsBySlot = new HashMap<>();
    private Inventory inventory;

    public PatrolMenuHolder(int page, int totalPages) {
        this.page = page;
        this.totalPages = totalPages;
    }

    public int page() {
        return page;
    }

    public int totalPages() {
        return totalPages;
    }

    public void bind(int slot, Claim claim) {
        claimsBySlot.put(slot, claim);
    }

    public Claim claimAt(int slot) {
        return claimsBySlot.get(slot);
    }

    public void inventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public @NotNull Inventory getInventory() {
        if (inventory == null) {
            throw new IllegalStateException("Patrol inventory has not been initialized yet.");
        }
        return inventory;
    }
}
