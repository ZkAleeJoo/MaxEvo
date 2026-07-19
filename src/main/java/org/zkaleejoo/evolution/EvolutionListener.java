package org.zkaleejoo.evolution;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Listens to game events and forwards them to the EvolutionManager
 * for mission progress tracking.
 */
public class EvolutionListener implements Listener {

    private final EvolutionManager manager;

    public EvolutionListener(EvolutionManager manager) {
        this.manager = manager;
    }

    /**
     * Tracks entity kills for KILL_ENTITY and KILL_PLAYER missions.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();
        if (killer == null) return;

        ItemStack held = killer.getInventory().getItemInMainHand();
        String toolType = manager.resolveToolType(held.getType().name());
        if (toolType == null) return;

        // KILL_ENTITY mission
        String entityType = entity.getType().name();
        manager.processMissionProgress(killer, toolType, "KILL_ENTITY", entityType, 1);

        // KILL_PLAYER mission
        if (entity instanceof Player) {
            manager.processMissionProgress(killer, toolType, "KILL_PLAYER", null, 1);
        }
    }

    /**
     * Tracks block breaks for MINE_BLOCK missions.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack held = player.getInventory().getItemInMainHand();
        String toolType = manager.resolveToolType(held.getType().name());
        if (toolType == null) return;

        String blockType = event.getBlock().getType().name();
        manager.processMissionProgress(player, toolType, "MINE_BLOCK", blockType, 1);
    }
}
