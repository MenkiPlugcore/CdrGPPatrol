package id.menkiplugcore.cdrgppatrol;

import id.menkiplugcore.cdrgppatrol.command.PatrolCommand;
import id.menkiplugcore.cdrgppatrol.command.PatrolDebugCommand;
import id.menkiplugcore.cdrgppatrol.gui.PatrolMenu;
import id.menkiplugcore.cdrgppatrol.listener.PatrolMenuListener;
import id.menkiplugcore.cdrgppatrol.service.ClaimService;
import id.menkiplugcore.cdrgppatrol.service.TeleportService;
import id.menkiplugcore.cdrgppatrol.util.Colors;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class CdrGPPatrol extends JavaPlugin {
    private ClaimService claimService;
    private TeleportService teleportService;
    private PatrolMenu patrolMenu;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        if (GriefPrevention.instance == null || GriefPrevention.instance.dataStore == null) {
            getLogger().severe("GriefPrevention is not ready. Disabling CdrGPPatrol.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.claimService = new ClaimService();
        this.teleportService = new TeleportService(this, claimService);
        this.patrolMenu = new PatrolMenu(this, claimService);

        PluginCommand patrolCommand = getCommand("gppatrol");
        PluginCommand debugCommand = getCommand("gppatroldebug");
        if (patrolCommand == null || debugCommand == null) {
            getLogger().severe("Commands are missing from plugin.yml. Disabling CdrGPPatrol.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        patrolCommand.setExecutor(new PatrolCommand(patrolMenu));
        debugCommand.setExecutor(new PatrolDebugCommand(this, claimService));
        getServer().getPluginManager().registerEvents(
                new PatrolMenuListener(patrolMenu, teleportService), this
        );

        getLogger().info("CdrGPPatrol v" + getPluginMeta().getVersion()
                + " enabled with GriefPrevention " + GriefPrevention.instance.getPluginMeta().getVersion() + ".");
    }

    public String prefix() {
        return Colors.color(getConfig().getString("prefix", "&8[&bGP Patrol&8]&r"));
    }
}
