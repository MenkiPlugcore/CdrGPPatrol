package id.menkiplugcore.cdrgppatrol.gui;

import id.menkiplugcore.cdrgppatrol.CdrGPPatrol;
import id.menkiplugcore.cdrgppatrol.model.AbandonedScanStats;
import id.menkiplugcore.cdrgppatrol.model.ClaimSort;
import id.menkiplugcore.cdrgppatrol.model.OwnerStatusSnapshot;
import id.menkiplugcore.cdrgppatrol.model.PatrolState;
import id.menkiplugcore.cdrgppatrol.service.ClaimService;
import id.menkiplugcore.cdrgppatrol.service.PatrolSessionService;
import id.menkiplugcore.cdrgppatrol.util.Colors;
import id.menkiplugcore.cdrgppatrol.util.TimeUtil;
import me.ryanhamshire.GriefPrevention.Claim;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class PatrolMenu {
    public static final int PAGE_SIZE = 45;
    public static final int PREVIOUS_SLOT = 45;
    public static final int SEARCH_SLOT = 46;
    public static final int WORLD_SLOT = 47;
    public static final int TYPE_SLOT = 48;
    public static final int REFRESH_SLOT = 49;
    public static final int SORT_SLOT = 50;
    public static final int RESET_SLOT = 51;
    public static final int SUMMARY_SLOT = 52;
    public static final int NEXT_SLOT = 53;

    private final CdrGPPatrol plugin;
    private final ClaimService claimService;
    private final PatrolSessionService sessionService;

    public PatrolMenu(CdrGPPatrol plugin, ClaimService claimService, PatrolSessionService sessionService) {
        this.plugin = plugin;
        this.claimService = claimService;
        this.sessionService = sessionService;
    }

    public void open(Player player, int requestedPage) {
        PatrolState state = sessionService.state(player);
        Map<UUID, OwnerStatusSnapshot> ownerStatuses = claimService.ownerStatuses();
        List<Claim> claims = claimService.getClaims(state, ownerStatuses);
        int totalPages = Math.max(1, (int) Math.ceil(claims.size() / (double) PAGE_SIZE));
        int page = Math.max(1, Math.min(requestedPage, totalPages));

        String baseTitle = plugin.getConfig().getString("menu-title", "&8GP Patrol");
        String filterMarker = state.hasFilters() ? " &e*" : "";
        String scannerMarker = state.hasAbandonedScanner() ? " &cA" + state.abandonedDays() + "d" : "";
        String title = Colors.color(baseTitle + " &7Page " + page + "/" + totalPages + filterMarker + scannerMarker);

        PatrolMenuHolder holder = new PatrolMenuHolder(page, totalPages);
        Inventory inventory = Bukkit.createInventory(holder, 54, title);
        holder.inventory(inventory);

        int start = (page - 1) * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, claims.size());
        int slot = 0;

        for (int index = start; index < end; index++) {
            Claim claim = claims.get(index);
            OwnerStatusSnapshot ownerStatus = claim.getOwnerID() == null ? null : ownerStatuses.get(claim.getOwnerID());
            inventory.setItem(slot, createClaimItem(claim, ownerStatus, state));
            holder.bind(slot, claim);
            slot++;
        }

        if (claims.isEmpty()) {
            List<String> emptyLore;
            if (state.hasAbandonedScanner()) {
                emptyLore = List.of(
                        "&7Tidak ada abandoned candidate untuk threshold &f" + state.abandonedDays() + " hari&7.",
                        "&7Owner online dan last-seen unknown selalu dikecualikan.",
                        "",
                        "&eKlik Scanner untuk ganti threshold."
                );
            } else if (state.hasFilters()) {
                emptyLore = List.of("&7Tidak ada claim yang cocok dengan filter aktif.", "", "&eKlik Reset Filters untuk menampilkan semua claim.");
            } else {
                emptyLore = List.of("&7Belum ada claim yang terbaca dari GriefPrevention.");
            }
            inventory.setItem(22, item(Material.BARRIER, "&cTidak ada claim", emptyLore));
        }

        inventory.setItem(PREVIOUS_SLOT, page > 1
                ? item(Material.ARROW, "&ePrevious Page", List.of("&7Klik untuk halaman sebelumnya"))
                : item(Material.GRAY_DYE, "&8Previous Page", List.of("&7Sudah di halaman pertama")));

        inventory.setItem(SEARCH_SLOT, searchItem(state));
        inventory.setItem(WORLD_SLOT, worldItem(state));
        inventory.setItem(TYPE_SLOT, typeItem(state));

        inventory.setItem(REFRESH_SLOT,
                item(Material.COMPASS, "&bRefresh", List.of("&7Refresh hasil dengan filter saat ini", "&7Hasil: &f" + claims.size())));

        inventory.setItem(SORT_SLOT, sortItem(state));
        inventory.setItem(RESET_SLOT, resetItem(state));
        inventory.setItem(SUMMARY_SLOT, summaryItem(state, claims));

        inventory.setItem(NEXT_SLOT, page < totalPages
                ? item(Material.ARROW, "&eNext Page", List.of("&7Klik untuk halaman berikutnya"))
                : item(Material.GRAY_DYE, "&8Next Page", List.of("&7Sudah di halaman terakhir")));

        player.openInventory(inventory);
    }

    private ItemStack searchItem(PatrolState state) {
        List<String> lore = new ArrayList<>();
        lore.add("&7Owner: " + (state.hasSearch() ? "&f" + state.ownerQuery() : "&8Semua owner"));
        lore.add("");
        lore.add("&eKlik kiri &7untuk petunjuk pencarian");
        if (state.hasSearch()) {
            lore.add("&cKlik kanan &7untuk hapus pencarian");
        }
        return item(Material.NAME_TAG, "&bSearch Owner", lore);
    }

    private ItemStack worldItem(PatrolState state) {
        String selected = state.hasWorldFilter() ? state.worldName() : "Semua World";
        return item(Material.MAP, "&aWorld Filter", List.of(
                "&7Aktif: &f" + selected,
                "",
                "&eKlik &7untuk ganti world"
        ));
    }

    private ItemStack typeItem(PatrolState state) {
        return item(Material.GOLDEN_SHOVEL, "&6Claim Type", List.of(
                "&7Aktif: &f" + state.claimType().displayName(),
                state.hasAbandonedScanner() ? "&8Scanner hanya menampilkan Player Claim." : "",
                "&eKlik &7untuk ganti tipe claim"
        ));
    }

    private ItemStack sortItem(PatrolState state) {
        if (state.hasAbandonedScanner()) {
            return item(Material.CLOCK, "&dSorting", List.of(
                    "&7Urutan: &fOldest Offline",
                    "&7Scanner mengunci urutan agar candidate",
                    "&7paling lama offline muncul lebih dulu."
            ));
        }
        return item(Material.HOPPER, "&dSorting", List.of(
                "&7Urutan: &f" + state.sort().displayName(),
                "",
                "&eKlik &7untuk ganti urutan"
        ));
    }

    private ItemStack resetItem(PatrolState state) {
        boolean changed = state.hasFilters() || state.sort() != ClaimSort.WORLD_ID;
        return changed
                ? item(Material.BARRIER, "&cReset Filters", List.of("&7Hapus search, filter, sorting, dan scanner."))
                : item(Material.GRAY_DYE, "&8Reset Filters", List.of("&7Filter masih default."));
    }

    private ItemStack summaryItem(PatrolState state, List<Claim> claims) {
        List<String> lore = new ArrayList<>();
        lore.add("&7Search: " + (state.hasSearch() ? "&f" + state.ownerQuery() : "&8Off"));
        lore.add("&7World: " + (state.hasWorldFilter() ? "&f" + state.worldName() : "&8Semua"));
        lore.add("&7Type: &f" + state.claimType().displayName());

        if (state.hasAbandonedScanner()) {
            AbandonedScanStats stats = claimService.abandonedStats(claims);
            lore.add("");
            lore.add("&cAbandoned Scanner: &f" + state.abandonedDays() + "d+");
            lore.add("&7Candidate claims: &f" + stats.claimCount());
            lore.add("&7Unique owners: &f" + stats.ownerCount());
            lore.add("&7Candidate area: &f" + stats.totalArea() + " blocks");
            lore.add("&7Order: &fOldest Offline First");
            lore.add("");
            lore.add("&eKlik kiri &7cycle 7d → 30d → 60d → 90d → off");
            lore.add("&cKlik kanan &7matikan scanner");
            lore.add("&8Custom: /gppatrol abandoned <days>d");
            return item(Material.RECOVERY_COMPASS, "&cAbandoned Scanner", lore);
        }

        lore.add("&7Sort: &f" + state.sort().displayName());
        lore.add("&7Hasil claim: &f" + claims.size());
        lore.add("");
        lore.add("&7Scanner: &8Off");
        lore.add("&eKlik &7untuk mulai scan 7 hari");
        lore.add("&8Command: /gppatrol abandoned 30d");
        return item(Material.CLOCK, "&fFilter Summary / Scanner", lore);
    }

    private ItemStack createClaimItem(Claim claim, OwnerStatusSnapshot ownerStatus, PatrolState state) {
        String owner = claimService.ownerName(claim);
        World world = claim.getLesserBoundaryCorner().getWorld();

        int x1 = claim.getLesserBoundaryCorner().getBlockX();
        int z1 = claim.getLesserBoundaryCorner().getBlockZ();
        int x2 = claim.getGreaterBoundaryCorner().getBlockX();
        int z2 = claim.getGreaterBoundaryCorner().getBlockZ();
        int centerX = Math.floorDiv(x1 + x2, 2);
        int centerZ = Math.floorDiv(z1 + z2, 2);

        List<String> lore = new ArrayList<>();
        if (state.hasAbandonedScanner()) {
            lore.add("&c⚠ Abandoned Candidate &7(" + state.abandonedDays() + "d+)");
        }
        lore.add("&7Claim ID: &f" + (claim.getID() == null ? "N/A" : claim.getID()));
        lore.add("&7World: &e" + world.getName());
        lore.add("&7Center: &b" + centerX + "&7, &b" + centerZ);
        lore.add("&7Ukuran: &f" + claim.getWidth() + " x " + claim.getHeight());
        lore.add("&7Area: &f" + claim.getArea() + " blocks");

        if (!claim.isAdminClaim() && ownerStatus != null) {
            lore.add("");
            lore.add("&7Owner Status: " + (ownerStatus.online() ? "&aONLINE" : "&cOFFLINE"));
            if (!ownerStatus.online()) {
                lore.add("&7Last Seen: &f" + TimeUtil.formatRelativePast(ownerStatus.lastSeenMillis(), System.currentTimeMillis()));
            }
            lore.add("&7Owner Claims: &f" + ownerStatus.claimCount());
            lore.add("&7Owner Total Area: &f" + ownerStatus.totalArea() + " blocks");
        }

        lore.add("");
        lore.add("&eKlik kiri &7untuk teleport");
        lore.add("&bKlik kanan &7untuk inspect claim");

        Material icon;
        if (claim.isAdminClaim()) {
            icon = Material.GOLD_BLOCK;
        } else if (state.hasAbandonedScanner()) {
            icon = Material.MOSSY_COBBLESTONE;
        } else {
            icon = Material.GRASS_BLOCK;
        }
        return item(icon, claim.isAdminClaim() ? "&6Admin Claim" : "&a" + owner, lore);
    }

    private ItemStack item(Material material, String name, List<String> lore) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(Colors.color(name));
        meta.setLore(lore.stream().filter(line -> line != null && !line.isEmpty()).map(Colors::color).toList());
        stack.setItemMeta(meta);
        return stack;
    }
}
