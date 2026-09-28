package id.menkiplugcore.cdrgppatrol.gui;

import id.menkiplugcore.cdrgppatrol.CdrGPPatrol;
import id.menkiplugcore.cdrgppatrol.model.ClaimTrustSnapshot;
import id.menkiplugcore.cdrgppatrol.model.OwnerStatusSnapshot;
import id.menkiplugcore.cdrgppatrol.service.ClaimBorderVisualizer;
import id.menkiplugcore.cdrgppatrol.service.ClaimService;
import id.menkiplugcore.cdrgppatrol.util.Colors;
import id.menkiplugcore.cdrgppatrol.util.TimeUtil;
import me.ryanhamshire.GriefPrevention.Claim;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class ClaimInspectorMenu {
    public static final int OWNER_SLOT = 10;
    public static final int INFO_SLOT = 11;
    public static final int BOUNDS_SLOT = 12;
    public static final int TRUST_SUMMARY_SLOT = 13;
    public static final int VISUALIZE_SLOT = 14;
    public static final int TELEPORT_SLOT = 15;
    public static final int TRUST_VIEWER_SLOT = 16;
    public static final int BACK_SLOT = 22;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")
            .withZone(ZoneId.systemDefault());

    private final CdrGPPatrol plugin;
    private final ClaimService claimService;
    private final ClaimBorderVisualizer borderVisualizer;

    public ClaimInspectorMenu(CdrGPPatrol plugin, ClaimService claimService,
                              ClaimBorderVisualizer borderVisualizer) {
        this.plugin = plugin;
        this.claimService = claimService;
        this.borderVisualizer = borderVisualizer;
    }

    public void open(Player player, Claim claim, int sourcePage) {
        String baseTitle = plugin.getConfig().getString("inspector-title", "&8Claim Inspector");
        String claimId = claim.getID() == null ? "N/A" : String.valueOf(claim.getID());
        String title = Colors.color(baseTitle + " &7#" + claimId);

        ClaimInspectorHolder holder = new ClaimInspectorHolder(claim, sourcePage);
        Inventory inventory = Bukkit.createInventory(holder, 27, title);
        holder.inventory(inventory);

        ClaimTrustSnapshot trust = claimService.getTrustSnapshot(claim);

        inventory.setItem(OWNER_SLOT, ownerItem(claim));
        inventory.setItem(INFO_SLOT, infoItem(claim));
        inventory.setItem(BOUNDS_SLOT, boundsItem(claim));
        inventory.setItem(TRUST_SUMMARY_SLOT, trustSummaryItem(trust));
        inventory.setItem(VISUALIZE_SLOT, visualizerItem(player, claim));
        inventory.setItem(TELEPORT_SLOT, item(Material.ENDER_PEARL, "&bSafe Teleport", List.of(
                "&7Teleport ke lokasi aman di dalam claim.",
                "",
                "&eKlik untuk teleport"
        )));
        inventory.setItem(TRUST_VIEWER_SLOT, trust.totalEntries() > 0
                ? item(Material.WRITABLE_BOOK, "&dView Trust", List.of(
                        "&7Lihat Manager, Build, Container, dan Access trust.",
                        "&7Total entri: &f" + trust.totalEntries(),
                        "",
                        "&eKlik untuk membuka"
                ))
                : item(Material.GRAY_DYE, "&8View Trust", List.of("&7Claim ini tidak memiliki trust eksplisit.")));
        inventory.setItem(BACK_SLOT, item(Material.ARROW, "&eBack to Patrol", List.of(
                "&7Kembali ke halaman claim sebelumnya."
        )));

        player.openInventory(inventory);
    }

    private ItemStack visualizerItem(Player player, Claim claim) {
        if (borderVisualizer.isVisualizing(player, claim)) {
            return item(Material.REDSTONE, "&cStop Border Visualizer", List.of(
                    "&7Border claim sedang aktif untukmu.",
                    "&7Sisa waktu: &f" + borderVisualizer.remainingSeconds(player, claim) + " detik",
                    "",
                    "&cKlik untuk berhenti"
            ));
        }

        int duration = Math.max(3, plugin.getConfig().getInt("visualizer.duration-seconds", 15));
        return item(Material.BEACON, "&bVisualize Border", List.of(
                "&7Tampilkan perimeter claim dengan particle.",
                "&7Hanya kamu yang melihat visualisasi ini.",
                "&7Durasi: &f" + duration + " detik",
                "",
                "&eKlik untuk menampilkan"
        ));
    }

    private ItemStack ownerItem(Claim claim) {
        List<String> lore = new ArrayList<>();
        if (claim.isAdminClaim()) {
            lore.add("&7Type: &6Admin Claim");
            lore.add("&7Owner UUID: &8N/A");
            return item(Material.GOLD_BLOCK, "&6Admin Claim", lore);
        }

        String owner = claimService.ownerName(claim);
        OwnerStatusSnapshot status = claimService.ownerStatus(claim);

        lore.add("&7Owner: &f" + owner);
        lore.add("&7Owner UUID: &8" + claim.getOwnerID());

        if (status != null) {
            lore.add("");
            lore.add("&7Status: " + (status.online() ? "&aONLINE" : "&cOFFLINE"));
            if (!status.online()) {
                if (status.hasKnownLastSeen()) {
                    lore.add("&7Last Seen: &f" + TimeUtil.formatRelativePast(status.lastSeenMillis(), System.currentTimeMillis()));
                    lore.add("&7Last Seen At: &f" + TimeUtil.formatAbsolute(status.lastSeenMillis()));
                } else {
                    lore.add("&7Last Seen: &8Unknown");
                }
            }
            lore.add("&7Total Claims: &f" + status.claimCount());
            lore.add("&7Total Area: &f" + status.totalArea() + " blocks");
        }

        return item(Material.PLAYER_HEAD, "&a" + owner, lore);
    }

    private ItemStack infoItem(Claim claim) {
        World world = claim.getLesserBoundaryCorner().getWorld();
        List<String> lore = new ArrayList<>();
        lore.add("&7Claim ID: &f" + (claim.getID() == null ? "N/A" : claim.getID()));
        lore.add("&7World: &e" + world.getName());
        lore.add("&7Type: &f" + (claim.isAdminClaim() ? "Admin" : "Player"));
        lore.add("&7Subclaim: &f" + (claim.parent != null ? "Yes" : "No"));
        lore.add("&7Ukuran: &f" + claim.getWidth() + " x " + claim.getHeight());
        lore.add("&7Area: &f" + claim.getArea() + " blocks");
        lore.add("&7Child claims: &f" + (claim.children == null ? 0 : claim.children.size()));
        if (claim.modifiedDate != null) {
            lore.add("&7Modified: &f" + DATE_FORMAT.format(claim.modifiedDate.toInstant()));
        }
        return item(Material.BOOK, "&fClaim Information", lore);
    }

    private ItemStack boundsItem(Claim claim) {
        Location less = claim.getLesserBoundaryCorner();
        Location great = claim.getGreaterBoundaryCorner();
        int centerX = Math.floorDiv(less.getBlockX() + great.getBlockX(), 2);
        int centerZ = Math.floorDiv(less.getBlockZ() + great.getBlockZ(), 2);

        return item(Material.COMPASS, "&bClaim Bounds", List.of(
                "&7Lesser: &f" + less.getBlockX() + ", " + less.getBlockY() + ", " + less.getBlockZ(),
                "&7Greater: &f" + great.getBlockX() + ", " + great.getBlockY() + ", " + great.getBlockZ(),
                "&7Center X/Z: &b" + centerX + "&7, &b" + centerZ
        ));
    }

    private ItemStack trustSummaryItem(ClaimTrustSnapshot trust) {
        return item(Material.CHEST, "&6Trust Summary", List.of(
                "&7Manager: &f" + trust.managers().size(),
                "&7Build: &f" + trust.builders().size(),
                "&7Container: &f" + trust.containers().size(),
                "&7Access: &f" + trust.accessors().size(),
                "",
                "&7Total explicit entries: &f" + trust.totalEntries()
        ));
    }

    private ItemStack item(Material material, String name, List<String> lore) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(Colors.color(name));
        meta.setLore(lore.stream().map(Colors::color).toList());
        stack.setItemMeta(meta);
        return stack;
    }
}
