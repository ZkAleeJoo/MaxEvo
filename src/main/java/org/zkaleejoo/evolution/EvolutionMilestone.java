package org.zkaleejoo.evolution;

import java.util.List;

public record EvolutionMilestone(int requiredCount, ProgressType progressType, String enchantment, int level, List<String> unlockAbilities) {

    /**
     * Legacy compatibility: returns requiredCount (previously called blocksRequired).
     * @deprecated Use {@link #requiredCount()} instead.
     */
    @Deprecated
    public int blocksRequired() {
        return requiredCount;
    }
}