package org.zkaleejoo.evolution;

import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a single evolution level with its missions (requirements)
 * and rewards (enchantments + commands).
 */
public class EvolutionLevel {
    private final Map<String, Mission> missions = new HashMap<>();
    private final Map<String, Integer> enchantments = new HashMap<>();
    private final List<String> commands = new ArrayList<>();

    public EvolutionLevel(ConfigurationSection section) {
        // Parse missions
        ConfigurationSection reqSection = section.getConfigurationSection("requirements.missions");
        if (reqSection != null) {
            for (String missionKey : reqSection.getKeys(false)) {
                ConfigurationSection missionSec = reqSection.getConfigurationSection(missionKey);
                if (missionSec == null) continue;
                String type = missionSec.getString("type", "");
                String target = missionSec.getString("entity", missionSec.getString("block", null));
                int amount = missionSec.getInt("amount", 1);
                missions.put(missionKey, new Mission(type, target, amount));
            }
        }

        // Parse enchantment rewards
        ConfigurationSection rewardSection = section.getConfigurationSection("rewards.enchantments");
        if (rewardSection != null) {
            for (String enchKey : rewardSection.getKeys(false)) {
                enchantments.put(enchKey.toLowerCase(), rewardSection.getInt(enchKey));
            }
        }

        // Parse command rewards
        List<String> cmdList = section.getStringList("rewards.commands");
        commands.addAll(cmdList);
    }

    public Map<String, Mission> getMissions() {
        return missions;
    }

    public Map<String, Integer> getEnchantments() {
        return enchantments;
    }

    public List<String> getCommands() {
        return commands;
    }
}
