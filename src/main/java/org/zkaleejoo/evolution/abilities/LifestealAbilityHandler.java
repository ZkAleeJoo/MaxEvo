package org.zkaleejoo.evolution.abilities;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.zkaleejoo.evolution.AbilityTrigger;
import org.zkaleejoo.evolution.AbilityType;
import org.zkaleejoo.evolution.SpecialAbilityConfig;
import org.zkaleejoo.evolution.ToolEvolutionManager;

/**
 * Lifesteal ability handler for swords.
 * Restores health to the player when they kill an entity.
 * The 'amount' config value is the number of half-hearts restored per proc.
 */
public class LifestealAbilityHandler implements AbilityHandler {

    @Override
    public boolean canTrigger(ItemMeta meta, SpecialAbilityConfig config, ToolEvolutionManager manager) {
        if (config == null || !config.enabled() || config.type() != AbilityType.LIFESTEAL) {
            return false;
        }

        if (config.trigger() != AbilityTrigger.ENTITY_KILL) {
            return false;
        }

        return manager.isOffCooldown(meta, config.id());
    }

    @Override
    public void onBlockBreak(BlockBreakAbilityContext context, SpecialAbilityConfig config) {
        // Lifesteal does not trigger on block break
    }

    /**
     * Called when an entity is killed while wielding a tool with Lifesteal unlocked.
     */
    public void onEntityKill(EntityKillAbilityContext context, SpecialAbilityConfig config) {
        Player player = context.player();
        ItemMeta meta = context.meta();

        double healAmount = config.amount();
        AttributeInstance maxHealthAttr = player.getAttribute(Attribute.MAX_HEALTH);
        double maxHealth = maxHealthAttr != null ? maxHealthAttr.getValue() : 20.0D;
        double currentHealth = player.getHealth();

        if (currentHealth < maxHealth) {
            double newHealth = Math.min(maxHealth, currentHealth + healAmount);
            player.setHealth(newHealth);
        }

        context.evolutionManager().applyCooldown(meta, config);
        context.evolutionManager().incrementAbilityActivation(meta, config.id());
    }
}
