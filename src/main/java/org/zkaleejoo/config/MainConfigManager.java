package org.zkaleejoo.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.zkaleejoo.MaxGraves;

public class MainConfigManager {

    private CustomConfig configFile;
    private CustomConfig langFile;
    private MaxGraves plugin;

    //VARIABLES CONFIG
    private String selectedLanguage;
    private String prefix;
    private int graveDespawnTime;

    //VARIABLES MENSAJES
    private String msgNoPermission;
    private String msgPluginReload;
    private String msgGraveCreated;
    private String msgGraveClaimed;
    private String msgMapReceived;
    private String msgUsageCommand;

    public MainConfigManager(MaxGraves plugin){
        this.plugin = plugin;
        configFile = new CustomConfig("config.yml", null, plugin, false);
        configFile.registerConfig();
        loadConfig();
    }

    public void loadConfig(){
        FileConfiguration config = configFile.getConfig();

        selectedLanguage = config.getString("general.language", "en");

        String langPath = "messages_" + selectedLanguage + ".yml";
        langFile = new CustomConfig(langPath, "lang", plugin, false);
        langFile.registerConfig();
        FileConfiguration lang = langFile.getConfig();

        //CONFIG
        prefix = config.getString("general.prefix", "&#8A2BE2&lMaxGraves &8» ");
        graveDespawnTime = config.getInt("grave.despawn-time", 3600);

        //MENSAJES
        msgNoPermission = lang.getString("messages.no-permission", "&cYou do not have permission.");
        msgPluginReload = lang.getString("messages.plugin-reload", "&aConfiguration successfully reloaded.");
        msgGraveCreated = lang.getString("messages.grave-created", "&eYour tomb has been created. You have been given a map.");
        msgGraveClaimed = lang.getString("messages.grave-claimed", "&aYou have recovered your items and XP.");
        msgMapReceived = lang.getString("messages.map-received", "&eTomb map received.");
        msgUsageCommand = lang.getString("messages.usage-command", "&cUse: /maxgraves <reload>");
    }

    public void reloadConfig(){
        configFile.reloadConfig();
        if(langFile != null) langFile.reloadConfig();
        loadConfig();
    }

    //GETTERS
    public String getPrefix() { return prefix; }
    public int getGraveDespawnTime() { return graveDespawnTime; }

    public String getMsgNoPermission() { return msgNoPermission; }
    public String getMsgPluginReload() { return msgPluginReload; }
    public String getMsgGraveCreated() { return msgGraveCreated; }
    public String getMsgGraveClaimed() { return msgGraveClaimed; }
    public String getMsgMapReceived() { return msgMapReceived; }
    public String getMsgUsageCommand() { return msgUsageCommand; }
}