package org.zkaleejoo.evolution;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EvolutionManager {
    private final JavaPlugin plugin;
    private final Map<String, EvolutionConfig> configs = new HashMap<>();
    private final Map<UUID, PlayerProgress> progressMap = new HashMap<>();

    public EvolutionManager(JavaPlugin plugin) {
        this.plugin = plugin;
        saveDefaultEvolutionFiles();
        loadAllConfigs();
    }

    @SuppressWarnings("null")
    private void saveDefaultEvolutionFiles() {
        String[] defaults = { "evolution/sword.yml", "evolution/pickaxe.yml", "evolution/axe.yml" };
        for (String path : defaults) {
            File file = new File(plugin.getDataFolder(), path);
            if (!file.exists()) {
                plugin.saveResource(path, false);
            }
        }
    }

    private void loadAllConfigs() {
        configs.clear();
        File evolutionFolder = new File(plugin.getDataFolder(), "evolution");
        if (!evolutionFolder.exists()) {
            plugin.getLogger().warning("Evolution folder not found!");
            return;
        }
        File[] files = evolutionFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null)
            return;
        for (File file : files) {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
            String itemType = yaml.getString("item-type");
            if (itemType == null)
                continue;
            configs.put(itemType.toUpperCase(), new EvolutionConfig(yaml));
            plugin.getLogger().info("Loaded evolution config: " + itemType);
        }
    }

    public String resolveToolType(String materialName) {
        if (materialName == null)
            return null;
        String upper = materialName.toUpperCase();
        if (upper.endsWith("_SWORD"))
            return "SWORD";
        if (upper.endsWith("_PICKAXE"))
            return "PICKAXE";
        if (upper.endsWith("_AXE") && !upper.endsWith("_PICKAXE"))
            return "AXE";
        if (upper.endsWith("_SHOVEL"))
            return "SHOVEL";
        if (upper.endsWith("_HOE"))
            return "HOE";
        return null;
    }

    public EvolutionConfig getConfig(String toolType) {
        return configs.get(toolType.toUpperCase());
    }

    public PlayerProgress getProgress(Player player) {
        return progressMap.computeIfAbsent(player.getUniqueId(), uuid -> new PlayerProgress());
    }

    public void processMissionProgress(Player player, String toolType, String missionType, String target, int amount) {
        EvolutionConfig cfg = getConfig(toolType);
        if (cfg == null)
            return;

        PlayerProgress progress = getProgress(player);
        int currentLevel = progress.getCurrentLevel(toolType);
        EvolutionLevel lvl = cfg.getLevel(currentLevel);
        if (lvl == null)
            return;

        for (Map.Entry<String, Mission> entry : lvl.getMissions().entrySet()) {
            Mission mission = entry.getValue();
            if (!mission.getType().equalsIgnoreCase(missionType))
                continue;
            if (mission.getTarget() != null && !mission.getTarget().equalsIgnoreCase(target))
                continue;

            progress.addProgress(toolType, entry.getKey(), amount);
        }

        boolean allDone = lvl.getMissions().entrySet().stream().allMatch(entry -> {
            Mission mission = entry.getValue();
            int current = progress.getMissionProgress(toolType, entry.getKey());
            return current >= mission.getAmount();
        });

        if (allDone) {
            applyRewards(player, toolType, currentLevel);
            progress.levelUp(toolType);
            plugin.getLogger()
                    .info(player.getName() + " evolved " + toolType + " to level " + (currentLevel + 1) + "!");
        }
    }

    private void applyRewards(Player player, String toolType, int level) {
        EvolutionConfig cfg = getConfig(toolType);
        if (cfg == null)
            return;
        EvolutionLevel lvl = cfg.getLevel(level);
        if (lvl == null)
            return;

        ItemStack item = player.getInventory().getItemInMainHand();
        ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return;

        Registry<Enchantment> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
        for (Map.Entry<String, Integer> entry : lvl.getEnchantments().entrySet()) {
            NamespacedKey key = NamespacedKey.minecraft(entry.getKey());
            Enchantment enchant = registry.get(java.util.Objects.requireNonNull(key));
            if (enchant != null) {
                meta.addEnchant(enchant, entry.getValue(), true);
            } else {
                plugin.getLogger().warning("Unknown enchantment: " + entry.getKey());
            }
        }

        item.setItemMeta(meta);

        for (String cmd : lvl.getCommands()) {
            String finalCmd = cmd.replace("%player%", player.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCmd);
        }
    }

    public void reload() {
        loadAllConfigs();
    }
}
