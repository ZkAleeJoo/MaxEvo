package org.zkaleejoo.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.zkaleejoo.MaxEvo;
import org.zkaleejoo.config.MainConfigManager;
import org.zkaleejoo.utils.MessageUtils;
import org.zkaleejoo.utils.UpdateNotificationFormatter;

public class PlayerJoinListener implements Listener {

    private final MaxEvo plugin;

    public PlayerJoinListener(MaxEvo plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("maxevo.admin")) {
            return;
        }

        MainConfigManager config = plugin.getMainConfigManager();
        for (String line : UpdateNotificationFormatter.format(
                config.getPrefix(),
                config.getMsgUpdateAvailable(),
                config.getMsgUpdateCurrent(),
                config.getMsgUpdateDownload(),
                plugin.getPluginMeta().getVersion(),
                plugin.getLatestVersion(),
                MaxEvo.UPDATE_DOWNLOAD_URL)) {
            player.sendMessage(MessageUtils.getColoredMessage(line));
        }
    }
}
