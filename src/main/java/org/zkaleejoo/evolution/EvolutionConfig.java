package org.zkaleejoo.evolution;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.HashMap;
import java.util.Map;

public class EvolutionConfig {
    private final String itemType;
    private final Map<Integer, EvolutionLevel> levels = new HashMap<>();

    public EvolutionConfig(YamlConfiguration yaml) {
        this.itemType = yaml.getString("item-type");
        ConfigurationSection evolSection = yaml.getConfigurationSection("evolutions");
        if (evolSection == null)
            return;
        for (String key : evolSection.getKeys(false)) {
            try {
                int level = Integer.parseInt(key);
                ConfigurationSection levelSection = evolSection.getConfigurationSection(key);
                if (levelSection != null) {
                    levels.put(level, new EvolutionLevel(levelSection));
                }
            } catch (NumberFormatException ignored) {
            }
        }
    }

    public String getItemType() {
        return itemType;
    }

    public EvolutionLevel getLevel(int level) {
        return levels.get(level);
    }

    public int getMaxLevel() {
        return levels.isEmpty() ? 0 : java.util.Collections.max(levels.keySet());
    }
}
