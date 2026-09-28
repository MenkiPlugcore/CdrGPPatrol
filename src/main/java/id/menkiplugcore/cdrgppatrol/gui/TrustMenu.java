package id.menkiplugcore.cdrgppatrol.gui;

import id.menkiplugcore.cdrgppatrol.CdrGPPatrol;
import id.menkiplugcore.cdrgppatrol.model.ClaimTrustSnapshot;
import id.menkiplugcore.cdrgppatrol.model.TrustEntry;
import id.menkiplugcore.cdrgppatrol.service.ClaimService;
import id.menkiplugcore.cdrgppatrol.util.Colors;
import me.ryanhamshire.GriefPrevention.Claim;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public final class TrustMenu {
    public static final int PAGE_SIZE = 45;
    public static final int PREVIOUS_SLOT = 45;
    public static final int SUMMARY_SLOT = 47;
    public static final int BACK_SLOT = 49;
    public static final int NEXT_SLOT = 53;

    private final CdrGPPatrol plugin;
    private final ClaimService claimService;

    public TrustMenu(CdrGPPatrol plugin, ClaimService claimService) {
        this.plugin = plugin;
        this.claimService = claimService;
    }

    public void open(Player player, Claim claim, int sourcePage, int requestedPage) {
        ClaimTrustSnapshot snapshot = claimService.getTrustSnapshot(claim);
        List<TrustEntry> entries = snapshot.allEntries();
        int totalPages = Math.max(1, (int) Math.ceil(entries.size() / (double) PAGE_SIZE));
        int page = Math.max(1, Math.min(requestedPage, totalPages));

        String baseTitle = plugin.getConfig().getString("trust-title", "&8Claim Trust");
        String claimId = claim.getID() == null ? "N/A" : String.valueOf(claim.getID());
        String title = Colors.color(baseTitle + " &7#" + claimId + " " + page + "/" + totalPages);

        TrustMenuHolder holder = new TrustMenuHolder(claim, sourcePage, page, totalPages);
        Inventory inventory = Bukkit.createInventory(holder, 54, title);
        holder.inventory(inventory);

        int start = (page - 1) * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, entries.size());
        int slot = 0;
        for (int index = start; index < end; index++) {
            inventory.setItem(slot++, trustItem(entries.get(index)));
        }

        if (entries.isEmpty()) {
            inventory.setItem(22, item(Material.BARRIER, "&cNo Explicit Trust", List.of(
                    "&7Claim ini tidak memiliki Manager, Build,",
                    "&7Container, atau Access trust eksplisit."
            )));
        }

        inventory.setItem(PREVIOUS_SLOT, page > 1
                ? item(Material.ARROW, "&ePrevious Page", List.of("&7Halaman trust sebelumnya."))
                : item(Material.GRAY_DYE, "&8Previous Page", List.of("&7Sudah di halaman pertama.")));

        inventory.setItem(SUMMARY_SLOT, summaryItem(snapshot));
        inventory.setItem(BACK_SLOT, item(Material.ARROW, "&eBack to Inspector", List.of(
                "&7Kembali ke Claim Inspector."
        )));

        inventory.setItem(NEXT_SLOT, page < totalPages
                ? item(Material.ARROW, "&eNext Page", List.of("&7Halaman trust berikutnya."))
                : item(Material.GRAY_DYE, "&8Next Page", List.of("&7Sudah di halaman terakhir.")));

        player.openInventory(inventory);
    }

    private ItemStack trustItem(TrustEntry entry) {
        List<String> lore = new ArrayList<>();
        lore.add("&7Permission: " + entry.level().color() + entry.level().displayName());
        if (!entry.rawSubject().equals(entry.displaySubject())) {
            lore.add("&7Stored value: &8" + entry.rawSubject());
        }
        lore.add("");
        lore.add("&8Read-only inspector");

        return item(entry.level().material(), entry.level().color() + entry.displaySubject(), lore);
    }

    private ItemStack summaryItem(ClaimTrustSnapshot trust) {
        return item(Material.BOOK, "&fTrust Summary", List.of(
                "&7Manager: &f" + trust.managers().size(),
                "&7Build: &f" + trust.builders().size(),
                "&7Container: &f" + trust.containers().size(),
                "&7Access: &f" + trust.accessors().size(),
                "&7Total: &f" + trust.totalEntries()
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
