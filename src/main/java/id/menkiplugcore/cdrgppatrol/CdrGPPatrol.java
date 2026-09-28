package id.menkiplugcore.cdrgppatrol;

import id.menkiplugcore.cdrgppatrol.command.PatrolCommand;
import id.menkiplugcore.cdrgppatrol.command.PatrolDebugCommand;
import id.menkiplugcore.cdrgppatrol.gui.ClaimInspectorMenu;
import id.menkiplugcore.cdrgppatrol.gui.PatrolMenu;
import id.menkiplugcore.cdrgppatrol.gui.TrustMenu;
import id.menkiplugcore.cdrgppatrol.listener.PatrolMenuListener;
import id.menkiplugcore.cdrgppatrol.service.ClaimBorderVisualizer;
import id.menkiplugcore.cdrgppatrol.service.ClaimService;
import id.menkiplugcore.cdrgppatrol.service.PatrolSessionService;
import id.menkiplugcore.cdrgppatrol.service.TeleportService;
import id.menkiplugcore.cdrgppatrol.util.Colors;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class CdrGPPatrol extends JavaPlugin {
    private ClaimService claimService;
    private PatrolSessionService sessionService;
    private TeleportService teleportService;
    private ClaimBorderVisualizer borderVisualizer;
    private PatrolMenu patrolMenu;
    private ClaimInspectorMenu inspectorMenu;
    private TrustMenu trustMenu;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        if (GriefPrevention.instance == null || GriefPrevention.instance.dataStore == null) {
            getLogger().severe("GriefPrevention is not ready. Disabling CdrGPPatrol.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.claimService = new ClaimService();
        this.sessionService = new PatrolSessionService();
        this.teleportService = new TeleportService(this, claimService);
        this.borderVisualizer = new ClaimBorderVisualizer(this);
        this.patrolMenu = new PatrolMenu(this, claimService, sessionService);
        this.inspectorMenu = new ClaimInspectorMenu(this, claimService, borderVisualizer);
        this.trustMenu = new TrustMenu(this, claimService);

        PluginCommand patrolCommand = getCommand("gppatrol");
        PluginCommand debugCommand = getCommand("gppatroldebug");
        if (patrolCommand == null || debugCommand == null) {
            getLogger().severe("Commands are missing from plugin.yml. Disabling CdrGPPatrol.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        PatrolCommand patrolExecutor = new PatrolCommand(this, patrolMenu, claimService, sessionService);
        patrolCommand.setExecutor(patrolExecutor);
        patrolCommand.setTabCompleter(patrolExecutor);
        debugCommand.setExecutor(new PatrolDebugCommand(this, claimService));

        getServer().getPluginManager().registerEvents(
                new PatrolMenuListener(this, patrolMenu, inspectorMenu, trustMenu,
                        teleportService, borderVisualizer, claimService, sessionService), this
        );

        getLogger().info("CdrGPPatrol v" + getPluginMeta().getVersion()
                + " enabled with GriefPrevention " + GriefPrevention.instance.getPluginMeta().getVersion() + ".");
    }

    @Override
    public void onDisable() {
        if (borderVisualizer != null) {
            borderVisualizer.stopAll();
        }
        if (sessionService != null) {
            sessionService.clear();
        }
    }

    public String prefix() {
        return Colors.color(getConfig().getString("prefix", "&8[&bGP Patrol&8]&r"));
    }
}
