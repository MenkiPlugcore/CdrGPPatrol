package id.menkiplugcore.cdrgppatrol.command;

import id.menkiplugcore.cdrgppatrol.CdrGPPatrol;
import id.menkiplugcore.cdrgppatrol.gui.PatrolMenu;
import id.menkiplugcore.cdrgppatrol.model.ClaimSort;
import id.menkiplugcore.cdrgppatrol.model.ClaimType;
import id.menkiplugcore.cdrgppatrol.model.PatrolState;
import id.menkiplugcore.cdrgppatrol.service.ClaimService;
import id.menkiplugcore.cdrgppatrol.service.PatrolSessionService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public final class PatrolCommand implements CommandExecutor, TabCompleter {
    private static final List<String> ROOT_OPTIONS = List.of("search", "world", "type", "sort", "reset");

    private final CdrGPPatrol plugin;
    private final PatrolMenu patrolMenu;
    private final ClaimService claimService;
    private final PatrolSessionService sessionService;

    public PatrolCommand(CdrGPPatrol plugin, PatrolMenu patrolMenu,
                         ClaimService claimService, PatrolSessionService sessionService) {
        this.plugin = plugin;
        this.patrolMenu = patrolMenu;
        this.claimService = claimService;
        this.sessionService = sessionService;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("CdrGPPatrol: command ini hanya bisa digunakan player.");
            return true;
        }

        PatrolState state = sessionService.state(player);

        if (args.length == 0) {
            patrolMenu.open(player, 1);
            return true;
        }

        if (args.length == 1) {
            try {
                patrolMenu.open(player, Integer.parseInt(args[0]));
                return true;
            } catch (NumberFormatException ignored) {
                // Continue to subcommand parsing.
            }
        }

        String subcommand = args[0].toLowerCase(Locale.ROOT);
        switch (subcommand) {
            case "search" -> handleSearch(player, label, args, state);
            case "world" -> handleWorld(player, label, args, state);
            case "type" -> handleType(player, label, args, state);
            case "sort" -> handleSort(player, label, args, state);
            case "reset" -> {
                state.reset();
                player.sendMessage(plugin.prefix() + " §aSearch, filter, dan sorting direset.");
                patrolMenu.open(player, 1);
            }
            default -> sendUsage(player, label);
        }
        return true;
    }

    private void handleSearch(Player player, String label, String[] args, PatrolState state) {
        if (args.length < 2) {
            player.sendMessage(plugin.prefix() + " §7Gunakan: §f/" + label + " search <player|clear>");
            return;
        }

        String query = String.join(" ", Arrays.copyOfRange(args, 1, args.length)).trim();
        if (query.equalsIgnoreCase("clear") || query.equalsIgnoreCase("all")) {
            state.ownerQuery("");
            player.sendMessage(plugin.prefix() + " §aSearch owner dihapus.");
        } else {
            state.ownerQuery(query);
            player.sendMessage(plugin.prefix() + " §7Search owner: §f" + query);
        }
        patrolMenu.open(player, 1);
    }

    private void handleWorld(Player player, String label, String[] args, PatrolState state) {
        if (args.length < 2) {
            player.sendMessage(plugin.prefix() + " §7Gunakan: §f/" + label + " world <world|all>");
            return;
        }

        if (args[1].equalsIgnoreCase("all") || args[1].equalsIgnoreCase("clear")) {
            state.worldName("");
            player.sendMessage(plugin.prefix() + " §aWorld filter dihapus.");
            patrolMenu.open(player, 1);
            return;
        }

        String world = claimService.resolveWorld(args[1]);
        if (world == null) {
            player.sendMessage(plugin.prefix() + " §cWorld tersebut tidak ditemukan pada daftar claim.");
            return;
        }

        state.worldName(world);
        player.sendMessage(plugin.prefix() + " §7World filter: §f" + world);
        patrolMenu.open(player, 1);
    }

    private void handleType(Player player, String label, String[] args, PatrolState state) {
        if (args.length < 2) {
            player.sendMessage(plugin.prefix() + " §7Gunakan: §f/" + label + " type <all|player|admin>");
            return;
        }

        ClaimType type = ClaimType.fromInput(args[1]);
        if (type == null) {
            player.sendMessage(plugin.prefix() + " §cTipe tidak valid. Gunakan all, player, atau admin.");
            return;
        }

        state.claimType(type);
        player.sendMessage(plugin.prefix() + " §7Claim type: §f" + type.displayName());
        patrolMenu.open(player, 1);
    }

    private void handleSort(Player player, String label, String[] args, PatrolState state) {
        if (args.length < 2) {
            player.sendMessage(plugin.prefix() + " §7Gunakan: §f/" + label + " sort <world|owner|largest|smallest|id>");
            return;
        }

        ClaimSort sort = ClaimSort.fromInput(args[1]);
        if (sort == null) {
            player.sendMessage(plugin.prefix() + " §cSorting tidak valid. Gunakan world, owner, largest, smallest, atau id.");
            return;
        }

        state.sort(sort);
        player.sendMessage(plugin.prefix() + " §7Sorting: §f" + sort.displayName());
        patrolMenu.open(player, 1);
    }

    private void sendUsage(Player player, String label) {
        player.sendMessage(plugin.prefix() + " §fCdrGPPatrol v" + plugin.getPluginMeta().getVersion());
        player.sendMessage("§7/" + label + " [page] §8- §fBuka claim browser");
        player.sendMessage("§7/" + label + " search <player|clear> §8- §fCari owner");
        player.sendMessage("§7/" + label + " world <world|all> §8- §fFilter world");
        player.sendMessage("§7/" + label + " type <all|player|admin> §8- §fFilter tipe claim");
        player.sendMessage("§7/" + label + " sort <world|owner|largest|smallest|id> §8- §fSorting");
        player.sendMessage("§7/" + label + " reset §8- §fReset semua filter");
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                 @NotNull String alias, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            return List.of();
        }

        if (args.length == 1) {
            return matching(ROOT_OPTIONS, args[0]);
        }

        if (args.length == 2) {
            return switch (args[0].toLowerCase(Locale.ROOT)) {
                case "search" -> {
                    List<String> values = new ArrayList<>();
                    values.add("clear");
                    values.addAll(claimService.ownerNames());
                    yield matching(values, args[1]);
                }
                case "world" -> {
                    List<String> values = new ArrayList<>();
                    values.add("all");
                    values.addAll(claimService.worldNames());
                    yield matching(values, args[1]);
                }
                case "type" -> matching(List.of("all", "player", "admin"), args[1]);
                case "sort" -> matching(List.of("world", "owner", "largest", "smallest", "id"), args[1]);
                default -> List.of();
            };
        }

        return List.of();
    }

    private List<String> matching(Collection<String> values, String input) {
        String prefix = input.toLowerCase(Locale.ROOT);
        return values.stream()
                .filter(value -> value.toLowerCase(Locale.ROOT).startsWith(prefix))
                .toList();
    }
}
