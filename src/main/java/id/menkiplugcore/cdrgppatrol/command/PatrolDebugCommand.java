package id.menkiplugcore.cdrgppatrol.command;

import id.menkiplugcore.cdrgppatrol.CdrGPPatrol;
import id.menkiplugcore.cdrgppatrol.service.ClaimService;
import id.menkiplugcore.cdrgppatrol.util.Colors;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class PatrolDebugCommand implements CommandExecutor {
    private final CdrGPPatrol plugin;
    private final ClaimService claimService;

    public PatrolDebugCommand(CdrGPPatrol plugin, ClaimService claimService) {
        this.plugin = plugin;
        this.claimService = claimService;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        List<Claim> claims = claimService.getClaims();

        sender.sendMessage(Colors.color("&8&m--------------------------"));
        sender.sendMessage(Colors.color("&bCdrGPPatrol Debug"));
        sender.sendMessage(Colors.color("&7Plugin: &f" + plugin.getPluginMeta().getVersion()));
        sender.sendMessage(Colors.color("&7GriefPrevention: &f" + GriefPrevention.instance.getPluginMeta().getVersion()));
        sender.sendMessage(Colors.color("&7Total claim: &f" + claims.size()));

        int limit = Math.min(10, claims.size());
        for (int i = 0; i < limit; i++) {
            Claim claim = claims.get(i);
            Location lesser = claim.getLesserBoundaryCorner();
            Location greater = claim.getGreaterBoundaryCorner();
            sender.sendMessage(Colors.color("&e" + (i + 1) + ". &a" + claimService.ownerName(claim)
                    + " &7| World: &f" + lesser.getWorld().getName()
                    + " &7| &b" + lesser.getBlockX() + "," + lesser.getBlockZ()
                    + " &7-> &b" + greater.getBlockX() + "," + greater.getBlockZ()));
        }

        if (claims.size() > 10) {
            sender.sendMessage(Colors.color("&7Hanya menampilkan 10 claim pertama."));
        }
        sender.sendMessage(Colors.color("&8&m--------------------------"));
        return true;
    }
}
