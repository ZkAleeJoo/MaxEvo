package org.zkaleejoo.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.zkaleejoo.MaxEvo;
import org.zkaleejoo.config.MainConfigManager;
import org.zkaleejoo.utils.MessageUtils;

public class PlayerJoinListener implements Listener {

    private static final String UPDATE_NOTIFY_PERMISSION = "maxevo.admin";
    private static final String DOWNLOAD_URL = "https://builtbybit.com/resources/maxevo.107277/";

    private final MaxEvo plugin;

    public PlayerJoinListener(MaxEvo plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        MainConfigManager config = plugin.getConfigManager();

        if (!player.hasPermission(UPDATE_NOTIFY_PERMISSION)) {
            return;
        }

        String latest = plugin.getLatestVersion();
        if (latest == null || plugin.getPluginMeta().getVersion().equalsIgnoreCase(latest)) {
            return;
        }

        player.sendMessage(" ");
        player.sendMessage(MessageUtils.getColoredMessage(
                config.getPrefix() + config.getMsgUpdateAvailable().replace("{version}", latest)));
        player.sendMessage(MessageUtils.getColoredMessage(
                config.getMsgUpdateCurrent().replace("{version}", plugin.getPluginMeta().getVersion())));
        player.sendMessage(MessageUtils.getColoredMessage(config.getMsgUpdateDownload()));
        player.sendMessage(MessageUtils.getColoredMessage("&7" + DOWNLOAD_URL));
        player.sendMessage(" ");
    }
}
