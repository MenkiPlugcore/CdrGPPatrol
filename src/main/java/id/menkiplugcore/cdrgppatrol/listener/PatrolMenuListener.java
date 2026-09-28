package id.menkiplugcore.cdrgppatrol.listener;

import id.menkiplugcore.cdrgppatrol.CdrGPPatrol;
import id.menkiplugcore.cdrgppatrol.gui.ClaimInspectorHolder;
import id.menkiplugcore.cdrgppatrol.gui.ClaimInspectorMenu;
import id.menkiplugcore.cdrgppatrol.gui.PatrolMenu;
import id.menkiplugcore.cdrgppatrol.gui.PatrolMenuHolder;
import id.menkiplugcore.cdrgppatrol.gui.TrustMenu;
import id.menkiplugcore.cdrgppatrol.gui.TrustMenuHolder;
import id.menkiplugcore.cdrgppatrol.model.ClaimType;
import id.menkiplugcore.cdrgppatrol.model.PatrolState;
import id.menkiplugcore.cdrgppatrol.service.ClaimBorderVisualizer;
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
import org.bukkit.inventory.InventoryHolder;

import java.util.List;

public final class PatrolMenuListener implements Listener {
    private final CdrGPPatrol plugin;
    private final PatrolMenu patrolMenu;
    private final ClaimInspectorMenu inspectorMenu;
    private final TrustMenu trustMenu;
    private final TeleportService teleportService;
    private final ClaimBorderVisualizer borderVisualizer;
    private final ClaimService claimService;
    private final PatrolSessionService sessionService;

    public PatrolMenuListener(CdrGPPatrol plugin, PatrolMenu patrolMenu, ClaimInspectorMenu inspectorMenu,
                              TrustMenu trustMenu, TeleportService teleportService,
                              ClaimBorderVisualizer borderVisualizer, ClaimService claimService,
                              PatrolSessionService sessionService) {
        this.plugin = plugin;
        this.patrolMenu = patrolMenu;
        this.inspectorMenu = inspectorMenu;
        this.trustMenu = trustMenu;
        this.teleportService = teleportService;
        this.borderVisualizer = borderVisualizer;
        this.claimService = claimService;
        this.sessionService = sessionService;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder topHolder = event.getView().getTopInventory().getHolder();
        if (!(topHolder instanceof PatrolMenuHolder)
                && !(topHolder instanceof ClaimInspectorHolder)
                && !(topHolder instanceof TrustMenuHolder)) {
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

        if (topHolder instanceof PatrolMenuHolder holder) {
            handlePatrolClick(event, player, holder, rawSlot);
            return;
        }

        if (topHolder instanceof ClaimInspectorHolder holder) {
            handleInspectorClick(player, holder, rawSlot);
            return;
        }

        if (topHolder instanceof TrustMenuHolder holder) {
            handleTrustClick(player, holder, rawSlot);
        }
    }

    private void handlePatrolClick(InventoryClickEvent event, Player player, PatrolMenuHolder holder, int rawSlot) {
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
            if (state.hasAbandonedScanner()) {
                state.claimType(ClaimType.PLAYER);
                player.sendMessage(plugin.prefix() + " §7Abandoned Scanner hanya memproses §fPlayer Claim§7.");
            } else {
                state.claimType(state.claimType().next());
            }
            patrolMenu.open(player, 1);
            return;
        }

        if (rawSlot == PatrolMenu.REFRESH_SLOT) {
            patrolMenu.open(player, holder.page());
            return;
        }

        if (rawSlot == PatrolMenu.SORT_SLOT) {
            if (state.hasAbandonedScanner()) {
                player.sendMessage(plugin.prefix() + " §7Scanner mengunci sorting ke §fOldest Offline First§7.");
            } else {
                state.sort(state.sort().next());
            }
            patrolMenu.open(player, 1);
            return;
        }

        if (rawSlot == PatrolMenu.RESET_SLOT) {
            state.reset();
            patrolMenu.open(player, 1);
            return;
        }

        if (rawSlot == PatrolMenu.SUMMARY_SLOT) {
            if (event.isRightClick()) {
                state.abandonedDays(0);
            } else {
                state.cycleAbandonedPreset();
            }
            patrolMenu.open(player, 1);
            return;
        }

        if (rawSlot == PatrolMenu.NEXT_SLOT && holder.page() < holder.totalPages()) {
            patrolMenu.open(player, holder.page() + 1);
            return;
        }

        Claim claim = holder.claimAt(rawSlot);
        if (claim == null) {
            return;
        }

        if (event.isRightClick()) {
            inspectorMenu.open(player, claim, holder.page());
        } else {
            player.closeInventory();
            teleportService.teleport(player, claim);
        }
    }

    private void handleInspectorClick(Player player, ClaimInspectorHolder holder, int rawSlot) {
        Claim claim = holder.claim();

        if (rawSlot == ClaimInspectorMenu.VISUALIZE_SLOT) {
            borderVisualizer.toggle(player, claim);
            inspectorMenu.open(player, claim, holder.sourcePage());
            return;
        }

        if (rawSlot == ClaimInspectorMenu.TELEPORT_SLOT) {
            player.closeInventory();
            teleportService.teleport(player, claim);
            return;
        }

        if (rawSlot == ClaimInspectorMenu.TRUST_VIEWER_SLOT) {
            if (claimService.getTrustSnapshot(claim).totalEntries() > 0) {
                trustMenu.open(player, claim, holder.sourcePage(), 1);
            }
            return;
        }

        if (rawSlot == ClaimInspectorMenu.BACK_SLOT) {
            patrolMenu.open(player, holder.sourcePage());
        }
    }

    private void handleTrustClick(Player player, TrustMenuHolder holder, int rawSlot) {
        if (rawSlot == TrustMenu.PREVIOUS_SLOT && holder.page() > 1) {
            trustMenu.open(player, holder.claim(), holder.sourcePage(), holder.page() - 1);
            return;
        }

        if (rawSlot == TrustMenu.BACK_SLOT) {
            inspectorMenu.open(player, holder.claim(), holder.sourcePage());
            return;
        }

        if (rawSlot == TrustMenu.NEXT_SLOT && holder.page() < holder.totalPages()) {
            trustMenu.open(player, holder.claim(), holder.sourcePage(), holder.page() + 1);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        InventoryHolder holder = event.getView().getTopInventory().getHolder();
        if (holder instanceof PatrolMenuHolder
                || holder instanceof ClaimInspectorHolder
                || holder instanceof TrustMenuHolder) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        borderVisualizer.stop(event.getPlayer().getUniqueId());
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
