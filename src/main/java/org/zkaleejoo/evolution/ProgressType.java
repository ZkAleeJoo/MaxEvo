package org.zkaleejoo.evolution;

import java.util.Locale;

/**
 * Defines the different types of progress that can be tracked for tool evolution.
 * Each tool category supports a subset of these progress types.
 */
public enum ProgressType {

    /** Progress by breaking blocks (pickaxe, axe, shovel). */
    BLOCKS_BROKEN("blocks-broken"),

    /** Progress by killing mobs/entities — excludes players (sword, axe). */
    MOBS_KILLED("mobs-killed"),

    /** Progress by killing players (sword). */
    PLAYERS_KILLED("players-killed"),

    /** Progress by dealing damage to entities (sword). */
    DAMAGE_DEALT("damage-dealt"),

    /** Progress by catching fish or entities with a fishing rod. */
    FISH_CAUGHT("fish-caught");

    private final String configKey;

    ProgressType(String configKey) {
        this.configKey = configKey;
    }

    /**
     * Returns the key used in YAML config files.
     * Example: "blocks-broken", "mobs-killed"
     */
    public String getConfigKey() {
        return configKey;
    }

    /**
     * Parses a config key string into a ProgressType.
     *
     * @param key the config key (e.g. "blocks-broken", "mobs-killed")
     * @return the matching ProgressType, or null if not recognized
     */
    public static ProgressType fromConfigKey(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }

        String normalized = key.trim().toLowerCase(Locale.ROOT);
        for (ProgressType type : values()) {
            if (type.configKey.equals(normalized)) {
                return type;
            }
        }
        return null;
    }
}
