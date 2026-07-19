package org.zkaleejoo.evolution;

import org.bukkit.Material;

/**
 * Classifies tool materials into evolution categories.
 * Each category has its own evolution file, milestones, and abilities.
 */
public enum ToolCategory {

    SWORD("sword"),
    PICKAXE("pickaxe"),
    AXE("axe"),
    SHOVEL("shovel"),
    FISHING_ROD("fishing_rod");

    private final String configFileName;

    ToolCategory(String configFileName) {
        this.configFileName = configFileName;
    }

    /**
     * Returns the YAML config file name for this category (without path).
     * Example: "sword.yml", "pickaxe.yml"
     */
    public String getConfigFileName() {
        return configFileName + ".yml";
    }

    /**
     * Returns the resource path inside the plugin JAR.
     * Example: "tools/sword.yml"
     */
    public String getResourcePath() {
        return "tools/" + getConfigFileName();
    }

    /**
     * Returns the subfolder name used for data storage.
     */
    public String getId() {
        return configFileName;
    }

    /**
     * Determines the ToolCategory for a given Material.
     *
     * @param material the item material
     * @return the matching ToolCategory, or null if not a tracked tool type
     */
    public static ToolCategory fromMaterial(Material material) {
        if (material == null) {
            return null;
        }

        String name = material.name();

        if (name.endsWith("_SWORD")) {
            return SWORD;
        }
        if (name.endsWith("_PICKAXE")) {
            return PICKAXE;
        }
        if (name.endsWith("_AXE") && !name.endsWith("_PICKAXE")) {
            return AXE;
        }
        if (name.endsWith("_SHOVEL")) {
            return SHOVEL;
        }
        if (material == Material.FISHING_ROD) {
            return FISHING_ROD;
        }

        return null;
    }

    /**
     * Checks if a given ProgressType is valid for this category.
     */
    public boolean supportsProgressType(ProgressType progressType) {
        if (progressType == null) {
            return false;
        }

        return switch (this) {
            case SWORD -> progressType == ProgressType.MOBS_KILLED
                    || progressType == ProgressType.PLAYERS_KILLED
                    || progressType == ProgressType.DAMAGE_DEALT;
            case PICKAXE, SHOVEL -> progressType == ProgressType.BLOCKS_BROKEN;
            case AXE -> progressType == ProgressType.BLOCKS_BROKEN
                    || progressType == ProgressType.MOBS_KILLED;
            case FISHING_ROD -> progressType == ProgressType.FISH_CAUGHT;
        };
    }
}
