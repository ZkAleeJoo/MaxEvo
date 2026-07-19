package org.zkaleejoo.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.zkaleejoo.MaxEvo;

public class MainConfigManager {

    private CustomConfig configFile;
    private CustomConfig langFile;
    private final MaxEvo plugin;

    // VARIABLES CONFIG
    private String selectedLanguage;
    private String prefix;
    private boolean updateCheckEnabled;
    private boolean bStatsEnabled;

    // VARIABLES MENSAJES
    private String msgNoPermission;
    private String msgPluginReload;
    private String msgUsageCommand;
    private String msgUpdateAvailable;
    private String msgUpdateCurrent;
    private String msgUpdateDownload;

    public MainConfigManager(MaxEvo plugin) {
        this.plugin = plugin;
        configFile = new CustomConfig("config.yml", null, plugin, false);
        configFile.registerConfig();
        loadConfig();
    }

    public void loadConfig() {
        FileConfiguration config = configFile.getConfig();

        selectedLanguage = config.getString("general.language", "en");

        String langPath = "messages_" + selectedLanguage + ".yml";
        langFile = new CustomConfig(langPath, "lang", plugin, false);
        langFile.registerConfig();
        FileConfiguration lang = langFile.getConfig();

        // CONFIG
        prefix = config.getString("general.prefix", "&#1621F5&lMaxEvo &8» ");
        updateCheckEnabled = config.getBoolean("general.update-check", true);
        bStatsEnabled = config.getBoolean("general.bstats", true);

        // MENSAJES
        msgNoPermission = lang.getString("messages.no-permission", "&cYou do not have permission.");
        msgPluginReload = lang.getString("messages.plugin-reload", "&aConfiguration successfully reloaded.");
        msgUsageCommand = lang.getString("messages.usage-command", "&cUse: /maxevo <reload>");
        msgUpdateAvailable = lang.getString("messages.update-available",
                "&f&lNEW VERSION: &7{version}");
        msgUpdateCurrent = lang.getString("messages.update-current", "&7Your current version: &c{version}");
        msgUpdateDownload = lang.getString("messages.update-download",
                "&eDownload it to get improvements and fixes.");
    }

    public void reloadConfig() {
        configFile.reloadConfig();
        if (langFile != null)
            langFile.reloadConfig();
        loadConfig();
    }

    // GETTERS
    public String getPrefix() {
        return prefix;
    }

    public boolean isUpdateCheckEnabled() {
        return updateCheckEnabled;
    }

    public boolean isBStatsEnabled() {
        return bStatsEnabled;
    }

    public String getMsgNoPermission() {
        return msgNoPermission;
    }

    public String getMsgPluginReload() {
        return msgPluginReload;
    }

    public String getMsgUsageCommand() {
        return msgUsageCommand;
    }

    public String getMsgUpdateAvailable() {
        return msgUpdateAvailable;
    }

    public String getMsgUpdateCurrent() {
        return msgUpdateCurrent;
    }

    public String getMsgUpdateDownload() {
        return msgUpdateDownload;
    }
}
