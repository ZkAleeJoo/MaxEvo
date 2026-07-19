package org.zkaleejoo.listeners;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockMultiPlaceEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.zkaleejoo.MaxEvo;
import org.zkaleejoo.evolution.EvolutionMilestone;
import org.zkaleejoo.evolution.AbilityTrigger;
import org.zkaleejoo.evolution.ToolCategory;
import org.zkaleejoo.evolution.ProgressType;
import org.zkaleejoo.evolution.PlayerPlacedBlockTracker;
import org.zkaleejoo.evolution.SpecialAbilityConfig;
import org.zkaleejoo.evolution.ToolEvolutionManager;
import org.zkaleejoo.evolution.abilities.BlockBreakAbilityContext;
import org.zkaleejoo.utils.MessageUtils;

public class ToolEvolutionListener implements Listener {

    private final MaxEvo plugin;
    private final ToolEvolutionManager evolutionManager;
    private final PlayerPlacedBlockTracker playerPlacedBlockTracker;

    public ToolEvolutionListener(MaxEvo plugin, ToolEvolutionManager evolutionManager,
            PlayerPlacedBlockTracker playerPlacedBlockTracker) {
        this.plugin = plugin;
        this.evolutionManager = evolutionManager;
        this.playerPlacedBlockTracker = playerPlacedBlockTracker;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event instanceof BlockMultiPlaceEvent multiPlaceEvent) {
            multiPlaceEvent.getReplacedBlockStates()
                    .forEach(state -> playerPlacedBlockTracker.markPlaced(state.getBlock()));
            return;
        }

        playerPlacedBlockTracker.markPlaced(event.getBlockPlaced());
    }

    private void handleProgress(Player player, ItemStack tool, ItemMeta meta, ProgressType progressType, int amount, Runnable onAbilityProcess) {
        boolean isTestTool = evolutionManager.isTestTool(meta);
        if (!isTestTool) {
            evolutionManager.syncEvolution(meta, tool.getType(), evolutionManager.getUsage(meta));
        }

        ToolCategory category = ToolCategory.fromMaterial(tool.getType());
        int usage = isTestTool
                ? evolutionManager.getUsage(meta, progressType)
                : evolutionManager.incrementUsage(meta, category, progressType, amount);
        
        evolutionManager.updateProgressDisplay(meta, tool.getType(), usage);
        evolutionManager.updateProgressLore(meta, usage);
        
        java.util.List<EvolutionMilestone> reachedMilestones = isTestTool
                ? java.util.List.of()
                : evolutionManager.getNewlyReachedMilestones(meta, tool.getType(), usage);

        for (EvolutionMilestone milestone : reachedMilestones) {
            if (milestone.progressType() != progressType) continue;
            boolean changed = evolutionManager.applyMilestone(meta, tool.getType(), milestone);
            if (!changed) continue;
            String milestoneReward = "-";
            if (milestone.enchantment() != null && !milestone.enchantment().isBlank()) {
                String enchantmentName = plugin.getConfigManager().getEnchantmentName(milestone.enchantment());
                String message = plugin.getConfigManager().getMsgMilestoneReached()
                        .replace("%blocks%", String.valueOf(milestone.requiredCount()))
                        .replace("%enchant%", enchantmentName)
                        .replace("%level%", String.valueOf(milestone.level()));
                player.sendMessage(org.zkaleejoo.utils.MessageUtils.getColoredMessage(plugin.getConfigManager().getPrefix() + message));
                milestoneReward = enchantmentName + " " + milestone.level();
            }
            if (plugin.getDiscordWebhookNotifier() != null) {
                plugin.getDiscordWebhookNotifier().notifyMilestoneUnlocked(player, tool.getType(), usage, milestoneReward);
            }
            for (String abilityId : evolutionManager.getAbilitiesToNotify(milestone, tool.getType())) {
                String message = plugin.getConfigManager().getMsgSpecialUnlocked()
                        .replace("%ability%", abilityId);
                player.sendMessage(org.zkaleejoo.utils.MessageUtils.getColoredMessage(plugin.getConfigManager().getPrefix() + message));
                if (plugin.getDiscordWebhookNotifier() != null) {
                    plugin.getDiscordWebhookNotifier().notifyAbilityUnlocked(player, tool.getType(), usage, abilityId);
                }
            }
        }

        if (onAbilityProcess != null) onAbilityProcess.run();
        tool.setItemMeta(meta);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItem(EquipmentSlot.HAND);

        if (!evolutionManager.isTrackedTool(tool)) {
            return;
        }

        ItemMeta meta = tool.getItemMeta();
        if (meta == null) {
            return;
        }

        if (evolutionManager.isCustomToolRevoked(meta)) {
            event.setCancelled(true);
            player.getInventory().setItem(EquipmentSlot.HAND, null);
            player.updateInventory();
            player.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMsgCustomToolRevoked()));
            return;
        }

        if (!evolutionManager.isEvolutionEligibleTool(tool)) {
            return;
        }

        if (!evolutionManager.shouldCountBlock(event.getBlock(), tool)) {
            return;
        }

        boolean isTestTool = evolutionManager.isTestTool(meta);
        if (!evolutionManager.ensureCustomToolRegistered(tool, player, isTestTool)) {
            return;
        }

        meta = tool.getItemMeta();
        if (meta == null) {
            return;
        }

        final ItemMeta finalMeta = meta;
        handleProgress(player, tool, finalMeta, ProgressType.BLOCKS_BROKEN, 1, () -> {
            Set<String> unlockedAbilities = new LinkedHashSet<>(
                    evolutionManager.getUnlockedAbilities(finalMeta.getPersistentDataContainer()));
            BlockBreakAbilityContext context = new BlockBreakAbilityContext(
                    plugin,
                    evolutionManager,
                    event,
                    player,
                    tool,
                    finalMeta);
            processBlockBreakAbilities(context, unlockedAbilities);
            finalizeCustomDrops(context);
            evolutionManager.processSpecialAbilities(finalMeta, tool.getType(), unlockedAbilities);
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null) return;
        Player player = event.getEntity().getKiller();
        ItemStack tool = player.getInventory().getItem(EquipmentSlot.HAND);
        if (!evolutionManager.isTrackedTool(tool)) return;
        ItemMeta meta = tool.getItemMeta();
        if (meta == null || evolutionManager.isCustomToolRevoked(meta)) return;
        if (!evolutionManager.isEvolutionEligibleTool(tool)) return;
        boolean isTestTool = evolutionManager.isTestTool(meta);
        if (!evolutionManager.ensureCustomToolRegistered(tool, player, isTestTool)) return;
        meta = tool.getItemMeta();
        if (meta == null) return;
        boolean isPlayer = event.getEntity() instanceof Player;
        ProgressType type = isPlayer ? ProgressType.PLAYERS_KILLED : ProgressType.MOBS_KILLED;
        handleProgress(player, tool, meta, type, 1, null);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerFish(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (!evolutionManager.isTrackedTool(tool)) return;
        ItemMeta meta = tool.getItemMeta();
        if (meta == null || evolutionManager.isCustomToolRevoked(meta)) return;
        if (!evolutionManager.isEvolutionEligibleTool(tool)) return;
        boolean isTestTool = evolutionManager.isTestTool(meta);
        if (!evolutionManager.ensureCustomToolRegistered(tool, player, isTestTool)) return;
        meta = tool.getItemMeta();
        if (meta == null) return;
        handleProgress(player, tool, meta, ProgressType.FISH_CAUGHT, 1, null);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (!evolutionManager.isTrackedTool(tool)) return;
        ItemMeta meta = tool.getItemMeta();
        if (meta == null || evolutionManager.isCustomToolRevoked(meta)) return;
        if (!evolutionManager.isEvolutionEligibleTool(tool)) return;
        boolean isTestTool = evolutionManager.isTestTool(meta);
        if (!evolutionManager.ensureCustomToolRegistered(tool, player, isTestTool)) return;
        meta = tool.getItemMeta();
        if (meta == null) return;
        handleProgress(player, tool, meta, ProgressType.DAMAGE_DEALT, (int)event.getFinalDamage(), null);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        int removed = purgeRevokedTools(player.getInventory()) + purgeRevokedTools(player.getEnderChest());
        int synced = syncEvolutionTools(player.getInventory()) + syncEvolutionTools(player.getEnderChest());
        if (removed > 0 || synced > 0) {
            player.updateInventory();
            if (removed > 0) {
                player.sendMessage(MessageUtils.getColoredMessage(
                        plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMsgCustomToolRevoked()));
            }
        }
    }

    @SuppressWarnings("unused")
    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null || from.getWorld() == null || !from.getWorld().equals(to.getWorld())) {
            return;
        }

        if (from.getBlockX() == to.getBlockX()
                && from.getBlockY() == to.getBlockY()
                && from.getBlockZ() == to.getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItem(EquipmentSlot.HAND);
        if (!evolutionManager.isTrackedTool(tool)) {
            return;
        }

        ItemMeta meta = tool.getItemMeta();
        if (meta == null) {
            return;
        }

        if (evolutionManager.isCustomToolRevoked(meta)) {
            player.getInventory().setItem(EquipmentSlot.HAND, null);
            player.updateInventory();
            player.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMsgCustomToolRevoked()));
            return;
        }

        Set<String> unlockedAbilities = new LinkedHashSet<>(
                evolutionManager.getUnlockedAbilities(meta.getPersistentDataContainer()));
        boolean changed = false;
        if (unlockedAbilities.isEmpty()) {
            changed = evolutionManager.syncEvolution(meta, tool.getType(), evolutionManager.getUsage(meta)).changed();
            unlockedAbilities = new LinkedHashSet<>(
                    evolutionManager.getUnlockedAbilities(meta.getPersistentDataContainer()));
        }
        if (unlockedAbilities.isEmpty()) {
            if (changed) {
                tool.setItemMeta(meta);
            }
            return;
        }

        double distance = horizontalDistance(from, to);
        String progressKey = buildWalkDistanceProgressKey(player, tool, meta);
        for (String abilityId : unlockedAbilities) {
            SpecialAbilityConfig ability = evolutionManager.getSpecialAbilityConfig(abilityId, tool.getType());
            if (ability == null || !ability.enabled() || ability.trigger() != AbilityTrigger.WALK_DISTANCE) {
                continue;
            }
            if (ability.requireMainHand()
                    && !tool.equals(player.getInventory().getItem(EquipmentSlot.HAND))) {
                continue;
            }
            if (evolutionManager.processWalkDistanceAbility(meta, ability, distance, progressKey)) {
                changed = true;
            }
        }

        if (changed) {
            tool.setItemMeta(meta);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        int removed = purgeRevokedTools(event.getInventory());
        if (removed > 0 && event.getPlayer() instanceof Player player) {
            player.updateInventory();
            player.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMsgCustomToolRevoked()));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        ItemStack itemStack = event.getItem().getItemStack();
        ItemMeta meta = itemStack == null ? null : itemStack.getItemMeta();
        if (meta == null || !evolutionManager.isCustomTool(itemStack) || !evolutionManager.isCustomToolRevoked(meta)) {
            return;
        }

        event.setCancelled(true);
        event.getItem().remove();
        if (event.getEntity() instanceof Player player) {
            player.sendMessage(MessageUtils.getColoredMessage(
                    plugin.getConfigManager().getPrefix() + plugin.getConfigManager().getMsgCustomToolRevoked()));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSuccessfulBlockBreak(BlockBreakEvent event) {
        playerPlacedBlockTracker.forget(event.getBlock());
    }

    private void processBlockBreakAbilities(BlockBreakAbilityContext context, Set<String> unlockedAbilities) {
        resolveActiveAbilities(unlockedAbilities, context.tool().getType()).forEach(ability -> {
            evolutionManager.getAbilityHandlerRegistry().find(ability.type()).ifPresent(handler -> {
                if (!handler.canTrigger(context.meta(), ability, evolutionManager)) {
                    return;
                }
                handler.onBlockBreak(context, ability);
            });
        });
    }

    private void finalizeCustomDrops(BlockBreakAbilityContext context) {
        if (!context.hasCustomDrops() || context.isDropsDispatched()) {
            return;
        }

        context.event().setDropItems(false);
        context.getDrops().forEach(drop -> context.event().getBlock().getWorld()
                .dropItemNaturally(context.event().getBlock().getLocation(), drop));
    }

    private List<SpecialAbilityConfig> resolveActiveAbilities(Set<String> unlockedAbilities,
            org.bukkit.Material toolType) {
        return unlockedAbilities.stream()
                .map(abilityId -> evolutionManager.getSpecialAbilityConfig(abilityId, toolType))
                .filter(config -> config != null && config.enabled())
                .sorted(Comparator.comparingInt(config -> config.type().ordinal()))
                .toList();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        var playerId = event.getPlayer().getUniqueId();
        evolutionManager.clearWalkDistanceProgress(playerId);
        evolutionManager.getAbilityHandlerRegistry().asMap().values()
                .forEach(handler -> handler.onPlayerDisconnect(playerId));
    }

    private int purgeRevokedTools(Inventory inventory) {
        if (inventory == null) {
            return 0;
        }

        ItemStack[] contents = inventory.getContents();
        int removed = 0;
        boolean changed = false;
        for (int slot = 0; slot < contents.length; slot++) {
            ItemStack item = contents[slot];
            if (item == null || item.getType() == Material.AIR) {
                continue;
            }

            ItemMeta meta = item.getItemMeta();
            if (meta != null && evolutionManager.isCustomTool(item) && evolutionManager.isCustomToolRevoked(meta)) {
                contents[slot] = null;
                removed += Math.max(1, item.getAmount());
                changed = true;
                continue;
            }

            int nestedRemoved = purgeRevokedNestedContainer(item);
            if (nestedRemoved > 0) {
                contents[slot] = item;
                removed += nestedRemoved;
                changed = true;
            }
        }

        if (changed) {
            inventory.setContents(contents);
        }
        return removed;
    }

    private int syncEvolutionTools(Inventory inventory) {
        if (inventory == null) {
            return 0;
        }

        int synced = 0;
        for (ItemStack item : inventory.getContents()) {
            if (item == null || item.getType() == Material.AIR || !evolutionManager.isTrackedTool(item)) {
                continue;
            }
            if (evolutionManager.syncEvolution(item).changed()) {
                synced++;
            }
        }
        return synced;
    }

    private double horizontalDistance(Location from, Location to) {
        double x = to.getX() - from.getX();
        double z = to.getZ() - from.getZ();
        return Math.sqrt((x * x) + (z * z));
    }

    private String buildWalkDistanceProgressKey(Player player, ItemStack tool, ItemMeta meta) {
        String toolId = evolutionManager.getCustomToolId(meta);
        if (!toolId.isBlank()) {
            return player.getUniqueId() + ":" + toolId;
        }
        return player.getUniqueId() + ":" + tool.getType().name() + ":" + evolutionManager.getUsage(meta);
    }

    private int purgeRevokedNestedContainer(ItemStack item) {
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) {
            return 0;
        }

        ItemMeta meta = item.getItemMeta();
        if (!(meta instanceof BlockStateMeta blockStateMeta)
                || !(blockStateMeta.getBlockState() instanceof Container container)) {
            return 0;
        }

        int removed = purgeRevokedTools(container.getInventory());
        if (removed > 0) {
            blockStateMeta.setBlockState(container);
            item.setItemMeta(blockStateMeta);
        }
        return removed;
    }
}
