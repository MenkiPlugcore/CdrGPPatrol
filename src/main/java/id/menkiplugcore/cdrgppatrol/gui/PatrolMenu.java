package id.menkiplugcore.cdrgppatrol.gui;

import id.menkiplugcore.cdrgppatrol.CdrGPPatrol;
import id.menkiplugcore.cdrgppatrol.service.ClaimService;
import id.menkiplugcore.cdrgppatrol.util.Colors;
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

public final class PatrolMenu {
    public static final int PAGE_SIZE = 45;
    public static final int PREVIOUS_SLOT = 45;
    public static final int REFRESH_SLOT = 49;
    public static final int NEXT_SLOT = 53;

    private final CdrGPPatrol plugin;
    private final ClaimService claimService;

    public PatrolMenu(CdrGPPatrol plugin, ClaimService claimService) {
        this.plugin = plugin;
        this.claimService = claimService;
    }

    public void open(Player player, int requestedPage) {
        List<Claim> claims = claimService.getClaims();
        int totalPages = Math.max(1, (int) Math.ceil(claims.size() / (double) PAGE_SIZE));
        int page = Math.max(1, Math.min(requestedPage, totalPages));

        String baseTitle = plugin.getConfig().getString("menu-title", "&8GP Patrol");
        String title = Colors.color(baseTitle + " &7Page " + page + "/" + totalPages);

        PatrolMenuHolder holder = new PatrolMenuHolder(page, totalPages);
        Inventory inventory = Bukkit.createInventory(holder, 54, title);
        holder.inventory(inventory);

        int start = (page - 1) * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, claims.size());
        int slot = 0;

        for (int index = start; index < end; index++) {
            Claim claim = claims.get(index);
            inventory.setItem(slot, createClaimItem(claim));
            holder.bind(slot, claim);
            slot++;
        }

        if (claims.isEmpty()) {
            inventory.setItem(22, item(Material.BARRIER, "&cTidak ada claim",
                    List.of("&7Belum ada claim yang terbaca dari GriefPrevention.")));
        }

        inventory.setItem(PREVIOUS_SLOT, page > 1
                ? item(Material.ARROW, "&ePrevious Page", List.of("&7Klik untuk halaman sebelumnya"))
                : item(Material.GRAY_DYE, "&8Previous Page", List.of("&7Sudah di halaman pertama")));

        inventory.setItem(REFRESH_SLOT,
                item(Material.COMPASS, "&bRefresh", List.of("&7Refresh daftar claim", "&7Total: &f" + claims.size())));

        inventory.setItem(NEXT_SLOT, page < totalPages
                ? item(Material.ARROW, "&eNext Page", List.of("&7Klik untuk halaman berikutnya"))
                : item(Material.GRAY_DYE, "&8Next Page", List.of("&7Sudah di halaman terakhir")));

        player.openInventory(inventory);
    }

    private ItemStack createClaimItem(Claim claim) {
        String owner = claimService.ownerName(claim);
        World world = claim.getLesserBoundaryCorner().getWorld();

        int x1 = claim.getLesserBoundaryCorner().getBlockX();
        int z1 = claim.getLesserBoundaryCorner().getBlockZ();
        int x2 = claim.getGreaterBoundaryCorner().getBlockX();
        int z2 = claim.getGreaterBoundaryCorner().getBlockZ();
        int centerX = Math.floorDiv(x1 + x2, 2);
        int centerZ = Math.floorDiv(z1 + z2, 2);

        List<String> lore = new ArrayList<>();
        lore.add("&7Claim ID: &f" + (claim.getID() == null ? "N/A" : claim.getID()));
        lore.add("&7World: &e" + world.getName());
        lore.add("&7Center: &b" + centerX + "&7, &b" + centerZ);
        lore.add("&7Ukuran: &f" + claim.getWidth() + " x " + claim.getHeight());
        lore.add("&7Area: &f" + claim.getArea() + " blocks");
        lore.add("");
        lore.add("&eKlik untuk teleport");

        Material icon = claim.isAdminClaim() ? Material.GOLD_BLOCK : Material.GRASS_BLOCK;
        return item(icon, claim.isAdminClaim() ? "&6Admin Claim" : "&a" + owner, lore);
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
