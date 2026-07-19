package org.zkaleejoo;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.zkaleejoo.scheduler.ScheduledTask;
import org.zkaleejoo.scheduler.SchedulerAdapter;
import org.zkaleejoo.scheduler.SchedulerAdapterFactory;
import org.bstats.bukkit.Metrics;
import org.zkaleejoo.commands.MainCommand;
import org.zkaleejoo.config.CustomConfig;
import org.zkaleejoo.config.MainConfigManager;
import org.zkaleejoo.listeners.PlayerJoinListener;
import org.zkaleejoo.utils.MessageUtils;
import org.zkaleejoo.utils.UpdateChecker;

public final class MaxEvo extends JavaPlugin {

    private static final int BSTATS_PLUGIN_ID = 32769;
    private static final long UPDATE_CHECK_INTERVAL_TICKS = 20L * 60L * 60L * 5L;

    private MainConfigManager mainConfigManager;
    private String latestVersion;
    private Metrics metrics;
    private ScheduledTask updateCheckTask;
    private SchedulerAdapter schedulerAdapter;

    // PLUGIN ENCIENDE
    @Override
    public void onEnable() {
        schedulerAdapter = SchedulerAdapterFactory.createAdapter(this);

        CustomConfig initialConfig = new CustomConfig("config.yml", null, this, false);
        initialConfig.registerConfig();

        mainConfigManager = new MainConfigManager(this);
        syncMetricsState();

        MainCommand mainCommand = new MainCommand(this);
        registerCommand("maxevo", mainCommand, mainCommand);

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);

        Bukkit.getConsoleSender().sendMessage(
                MessageUtils.getColoredMessage("&1&lMaxEvo &8» &1   _____                 ___________            "));
        Bukkit.getConsoleSender().sendMessage(
                MessageUtils.getColoredMessage("&1&lMaxEvo &8» &1  /     \\ _____  ___  ___\\_   _____/__  ______  "));
        Bukkit.getConsoleSender().sendMessage(MessageUtils
                .getColoredMessage("&1&lMaxEvo &8» &1 /  \\ /  \\\\__  \\ \\  \\/  / |    __)_\\  \\/ /  _ \\ "));
        Bukkit.getConsoleSender().sendMessage(MessageUtils
                .getColoredMessage("&1&lMaxEvo &8» &1/    Y    \\/ __ \\_>    <  |        \\\\   (  <_> )"));
        Bukkit.getConsoleSender().sendMessage(MessageUtils
                .getColoredMessage("&1&lMaxEvo &8» &1\\____|__  (____  /__/\\_ \\/_______  / \\_/ \\____/ "));
        Bukkit.getConsoleSender().sendMessage(MessageUtils
                .getColoredMessage("&1&lMaxEvo &8» &1        \\/     \\/      \\/        \\/             "));

        Bukkit.getConsoleSender().sendMessage(MessageUtils
                .getColoredMessage("&1&lMaxEvo &8» &1It was activated correctly in the version"));

        startUpdateChecks();
    }

    @Override
    public void onDisable() {
        if (updateCheckTask != null) {
            updateCheckTask.cancel();
            updateCheckTask = null;
        }

        if (metrics != null) {
            metrics.shutdown();
            metrics = null;
        }

        Bukkit.getConsoleSender().sendMessage(
                MessageUtils.getColoredMessage("&5&lMaxEvo &8» &fThe plugin has been disabled! Version: "));
    }

    @SuppressWarnings("null")
    private void registerCommand(String name, org.bukkit.command.CommandExecutor executor,
            org.bukkit.command.TabCompleter tabCompleter) {
        org.bukkit.command.PluginCommand command = getCommand(name);
        if (command == null) {
            getLogger().warning("Command \"" + name + "\" is missing in plugin.yml.");
            return;
        }
        command.setExecutor(executor);
        if (tabCompleter != null) {
            command.setTabCompleter(tabCompleter);
        }
    }

    public MainConfigManager getConfigManager() {
        return mainConfigManager;
    }

    public SchedulerAdapter getSchedulerAdapter() {
        return schedulerAdapter;
    }

    private void checkUpdates() {
        if (!getConfigManager().isUpdateCheckEnabled()) {
            return;
        }

        new UpdateChecker(this).getVersion(version -> {
            if (this.getPluginMeta().getVersion().equalsIgnoreCase(version)) {
                this.latestVersion = null;
                Bukkit.getConsoleSender().sendMessage(MessageUtils.getColoredMessage(
                        "&1&lMaxEvo &8» &aA check for updates was performed and nothing was found."));
            } else {
                this.latestVersion = version;

                Bukkit.getConsoleSender()
                        .sendMessage(MessageUtils
                                .getColoredMessage("&1&lMaxEvo &8» &f&lNEW VERSION: &7" + version));
                Bukkit.getConsoleSender().sendMessage(
                        MessageUtils
                                .getColoredMessage(
                                        "&1&lMaxEvo &8» &fDownload it now at the following link: &7https://modrinth.com/plugin/maxgraves"));
            }
        });
    }

    private void startUpdateChecks() {
        if (updateCheckTask != null) {
            updateCheckTask.cancel();
            updateCheckTask = null;
        }

        if (!getConfigManager().isUpdateCheckEnabled()) {
            latestVersion = null;
            return;
        }

        checkUpdates();
        updateCheckTask = schedulerAdapter.runTimer(this::checkUpdates,
                UPDATE_CHECK_INTERVAL_TICKS, UPDATE_CHECK_INTERVAL_TICKS);
    }

    private void syncMetricsState() {
        if (SchedulerAdapterFactory.isFolia()) {
            return;
        }

        if (getConfigManager().isBStatsEnabled()) {
            if (metrics == null) {
                metrics = new Metrics(this, BSTATS_PLUGIN_ID);
            }
            return;
        }

        if (metrics != null) {
            metrics.shutdown();
            metrics = null;
        }
    }

    public void reloadPluginState() {
        getConfigManager().reloadConfig();
        syncMetricsState();
        startUpdateChecks();
    }

    public String getLatestVersion() {
        return latestVersion;
    }

}
