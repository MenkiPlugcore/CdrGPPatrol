package id.menkiplugcore.cdrgppatrol.listener;

import id.menkiplugcore.cdrgppatrol.gui.PatrolMenu;
import id.menkiplugcore.cdrgppatrol.gui.PatrolMenuHolder;
import id.menkiplugcore.cdrgppatrol.service.TeleportService;
import me.ryanhamshire.GriefPrevention.Claim;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public final class PatrolMenuListener implements Listener {
    private final PatrolMenu patrolMenu;
    private final TeleportService teleportService;

    public PatrolMenuListener(PatrolMenu patrolMenu, TeleportService teleportService) {
        this.patrolMenu = patrolMenu;
        this.teleportService = teleportService;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof PatrolMenuHolder holder)) {
            return;
        }

        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        int rawSlot = event.getRawSlot();
        if (rawSlot < 0 || rawSlot >= event.getView().getTopInventory().getSize()) {
            return;
        }

        if (rawSlot == PatrolMenu.PREVIOUS_SLOT && holder.page() > 1) {
            patrolMenu.open(player, holder.page() - 1);
            return;
        }

        if (rawSlot == PatrolMenu.REFRESH_SLOT) {
            patrolMenu.open(player, holder.page());
            return;
        }

        if (rawSlot == PatrolMenu.NEXT_SLOT && holder.page() < holder.totalPages()) {
            patrolMenu.open(player, holder.page() + 1);
            return;
        }

        Claim claim = holder.claimAt(rawSlot);
        if (claim != null) {
            player.closeInventory();
            teleportService.teleport(player, claim);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof PatrolMenuHolder) {
            event.setCancelled(true);
        }
    }
}
