package org.zkaleejoo.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

public class MessageUtils {

    private static final LegacyComponentSerializer AMPERSAND_SERIALIZER = LegacyComponentSerializer.builder()
            .character('&')
            .hexCharacter('#')
            .hexColors()
            .build();
            
    private static final LegacyComponentSerializer SECTION_SERIALIZER = LegacyComponentSerializer.legacySection();

    public static String getColoredMessage(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }

        Component component = AMPERSAND_SERIALIZER.deserialize(message);
        return "§r" + SECTION_SERIALIZER.serialize(component);
    }

    public static Component getColoredComponent(String message) {
        return AMPERSAND_SERIALIZER.deserialize(message)
                .decoration(TextDecoration.ITALIC, false);
    }

    public static void broadcastToPlayersOnly(String message) {
        if (message == null || message.isEmpty())
            return;
        Component coloredMessage = getColoredComponent(message);
        for (Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
            if (player != null) {
                player.sendMessage(coloredMessage);
            }
        }
    }

}
