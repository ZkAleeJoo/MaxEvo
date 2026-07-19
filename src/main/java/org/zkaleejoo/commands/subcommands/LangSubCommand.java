package org.zkaleejoo.commands.subcommands;

import java.util.Collections;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.zkaleejoo.MaxEvo;
import org.zkaleejoo.utils.MessageUtils;

public class LangSubCommand implements SubCommand {

    private final MaxEvo plugin;

    public LangSubCommand(MaxEvo plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "lang";
    }

    @Override
    public String getPermission() {
        return "maxevo.admin.lang";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMsgOnlyPlayers()));
            return true;
        }

        plugin.getLanguageMenuService().openMenu(player);
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}
