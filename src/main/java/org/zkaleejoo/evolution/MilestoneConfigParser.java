package org.zkaleejoo.evolution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public final class MilestoneConfigParser {

    private MilestoneConfigParser() {
    }

    /**
     * Parses milestones from a per-category tool config file.
     * Each milestone must have an 'action' (blocks-broken, mobs-killed, etc.) and a 'count'.
     */
    public static List<EvolutionMilestone> parse(FileConfiguration config, String path, Consumer<String> warningSink) {
        ConfigurationSection section = config.getConfigurationSection(path);
        if (section == null) {
            return Collections.emptyList();
        }

        List<EvolutionMilestone> parsed = new ArrayList<>();

        for (String key : section.getKeys(false)) {
            String milestonePath = path + "." + key;

            // The new format uses 'count', but we still support legacy 'blocks' for migration
            boolean hasCount = isExplicitlySet(config, milestonePath + ".count");
            boolean hasLegacyBlocks = isExplicitlySet(config, milestonePath + ".blocks");

            if (!hasCount && !hasLegacyBlocks) {
                continue;
            }

            int count = hasCount
                    ? config.getInt(milestonePath + ".count", -1)
                    : config.getInt(milestonePath + ".blocks", -1);

            if (count <= 0) {
                warningSink.accept("Invalid milestone count value in key " + milestonePath);
                continue;
            }

            // Parse the action type; default to BLOCKS_BROKEN for backward compatibility
            String actionStr = getExplicitString(config, milestonePath + ".action", "blocks-broken");
            ProgressType progressType = ProgressType.fromConfigKey(actionStr);
            if (progressType == null) {
                warningSink.accept("Unknown action type '" + actionStr + "' in milestone " + milestonePath
                        + ". Defaulting to blocks-broken.");
                progressType = ProgressType.BLOCKS_BROKEN;
            }

            String enchantment = getExplicitString(config, milestonePath + ".enchantment", "");
            int level = Math.max(1, getExplicitInt(config, milestonePath + ".level", 1));
            List<String> unlockAbilities = getExplicitStringList(config, milestonePath + ".unlock-abilities");

            if (unlockAbilities.isEmpty() && getExplicitBoolean(config, milestonePath + ".unlock-special", false)) {
                unlockAbilities = List.of("self-repair");
            }

            parsed.add(new EvolutionMilestone(count, progressType, enchantment, level,
                    normalizeAbilityIds(unlockAbilities)));
        }

        parsed.sort(Comparator.comparingInt(m -> m.requiredCount()));
        return parsed;
    }

    private static boolean isExplicitlySet(FileConfiguration config, String path) {
        return config.contains(path, true);
    }

    private static String getExplicitString(FileConfiguration config, String path, String fallback) {
        return isExplicitlySet(config, path) ? config.getString(path, fallback) : fallback;
    }

    private static int getExplicitInt(FileConfiguration config, String path, int fallback) {
        return isExplicitlySet(config, path) ? config.getInt(path, fallback) : fallback;
    }

    private static boolean getExplicitBoolean(FileConfiguration config, String path, boolean fallback) {
        return isExplicitlySet(config, path) ? config.getBoolean(path, fallback) : fallback;
    }

    private static List<String> getExplicitStringList(FileConfiguration config, String path) {
        return isExplicitlySet(config, path) ? config.getStringList(path) : Collections.emptyList();
    }

    private static List<String> normalizeAbilityIds(List<String> ids) {
        return ids.stream()
                .filter(Objects::nonNull)
                .map(id -> id.trim().toLowerCase(Locale.ROOT))
                .filter(id -> !id.isBlank())
                .distinct()
                .collect(Collectors.toList());
    }
}
