package id.menkiplugcore.cdrgppatrol.listener;

import id.menkiplugcore.cdrgppatrol.CdrGPPatrol;
import id.menkiplugcore.cdrgppatrol.gui.PatrolMenu;
import id.menkiplugcore.cdrgppatrol.gui.PatrolMenuHolder;
import id.menkiplugcore.cdrgppatrol.model.PatrolState;
import id.menkiplugcore.cdrgppatrol.service.ClaimService;
import id.menkiplugcore.cdrgppatrol.service.PatrolSessionService;
import id.menkiplugcore.cdrgppatrol.service.TeleportService;
import me.ryanhamshire.GriefPrevention.Claim;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;

public final class PatrolMenuListener implements Listener {
    private final CdrGPPatrol plugin;
    private final PatrolMenu patrolMenu;
    private final TeleportService teleportService;
    private final ClaimService claimService;
    private final PatrolSessionService sessionService;

    public PatrolMenuListener(CdrGPPatrol plugin, PatrolMenu patrolMenu, TeleportService teleportService,
                              ClaimService claimService, PatrolSessionService sessionService) {
        this.plugin = plugin;
        this.patrolMenu = patrolMenu;
        this.teleportService = teleportService;
        this.claimService = claimService;
        this.sessionService = sessionService;
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

        PatrolState state = sessionService.state(player);

        if (rawSlot == PatrolMenu.PREVIOUS_SLOT && holder.page() > 1) {
            patrolMenu.open(player, holder.page() - 1);
            return;
        }

        if (rawSlot == PatrolMenu.SEARCH_SLOT) {
            if (event.isRightClick() && state.hasSearch()) {
                state.ownerQuery("");
                patrolMenu.open(player, 1);
            } else {
                player.closeInventory();
                player.sendMessage(plugin.prefix() + " §7Cari owner dengan §f/gppatrol search <player>§7.");
                player.sendMessage(plugin.prefix() + " §7Hapus pencarian dengan §f/gppatrol search clear§7.");
            }
            return;
        }

        if (rawSlot == PatrolMenu.WORLD_SLOT) {
            cycleWorld(state);
            patrolMenu.open(player, 1);
            return;
        }

        if (rawSlot == PatrolMenu.TYPE_SLOT) {
            state.claimType(state.claimType().next());
            patrolMenu.open(player, 1);
            return;
        }

        if (rawSlot == PatrolMenu.REFRESH_SLOT) {
            patrolMenu.open(player, holder.page());
            return;
        }

        if (rawSlot == PatrolMenu.SORT_SLOT) {
            state.sort(state.sort().next());
            patrolMenu.open(player, 1);
            return;
        }

        if (rawSlot == PatrolMenu.RESET_SLOT) {
            state.reset();
            patrolMenu.open(player, 1);
            return;
        }

        if (rawSlot == PatrolMenu.SUMMARY_SLOT) {
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

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        sessionService.remove(event.getPlayer());
    }

    private void cycleWorld(PatrolState state) {
        List<String> worlds = claimService.worldNames();
        if (worlds.isEmpty()) {
            state.worldName("");
            return;
        }

        if (!state.hasWorldFilter()) {
            state.worldName(worlds.getFirst());
            return;
        }

        int currentIndex = -1;
        for (int index = 0; index < worlds.size(); index++) {
            if (worlds.get(index).equalsIgnoreCase(state.worldName())) {
                currentIndex = index;
                break;
            }
        }

        if (currentIndex < 0 || currentIndex + 1 >= worlds.size()) {
            state.worldName("");
        } else {
            state.worldName(worlds.get(currentIndex + 1));
        }
    }
}
