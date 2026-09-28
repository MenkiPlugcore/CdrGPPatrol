package id.menkiplugcore.cdrgppatrol.command;

import id.menkiplugcore.cdrgppatrol.gui.PatrolMenu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class PatrolCommand implements CommandExecutor {
    private final PatrolMenu patrolMenu;

    public PatrolCommand(PatrolMenu patrolMenu) {
        this.patrolMenu = patrolMenu;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("CdrGPPatrol: command ini hanya bisa digunakan player.");
            return true;
        }

        int page = 1;
        if (args.length >= 1) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
                player.sendMessage("§cGunakan: /" + label + " [page]");
                return true;
            }
        }

        patrolMenu.open(player, page);
        return true;
    }
}
