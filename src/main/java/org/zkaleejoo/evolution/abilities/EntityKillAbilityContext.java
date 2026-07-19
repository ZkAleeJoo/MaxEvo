package org.zkaleejoo.evolution.abilities;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.zkaleejoo.MaxEvo;
import org.zkaleejoo.evolution.ToolEvolutionManager;

/**
 * Context object for ability handlers that trigger on entity kills.
 * Analogous to {@link BlockBreakAbilityContext} but for entity death events.
 */
public class EntityKillAbilityContext {

    private final MaxEvo plugin;
    private final ToolEvolutionManager evolutionManager;
    private final Player player;
    private final LivingEntity killedEntity;
    private final ItemStack tool;
    private final ItemMeta meta;

    public EntityKillAbilityContext(MaxEvo plugin, ToolEvolutionManager evolutionManager,
            Player player, LivingEntity killedEntity, ItemStack tool, ItemMeta meta) {
        this.plugin = plugin;
        this.evolutionManager = evolutionManager;
        this.player = player;
        this.killedEntity = killedEntity;
        this.tool = tool;
        this.meta = meta;
    }

    public MaxEvo plugin() {
        return plugin;
    }

    public ToolEvolutionManager evolutionManager() {
        return evolutionManager;
    }

    public Player player() {
        return player;
    }

    public LivingEntity killedEntity() {
        return killedEntity;
    }

    public ItemStack tool() {
        return tool;
    }

    public ItemMeta meta() {
        return meta;
    }

    public boolean isPlayerKill() {
        return killedEntity instanceof Player;
    }
}
