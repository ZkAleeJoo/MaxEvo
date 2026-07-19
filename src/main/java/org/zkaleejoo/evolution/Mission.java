package org.zkaleejoo.evolution;

/**
 * Represents a single mission requirement within an evolution level.
 * For example: "Kill 5 zombies" or "Mine 50 stone blocks".
 */
public class Mission {
    private final String type;     // KILL_ENTITY, KILL_PLAYER, DEAL_DAMAGE, MINE_BLOCK
    private final String target;   // Entity type or block material (nullable for KILL_PLAYER/DEAL_DAMAGE)
    private final int amount;

    public Mission(String type, String target, int amount) {
        this.type = type;
        this.target = target;
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public String getTarget() {
        return target;
    }

    public int getAmount() {
        return amount;
    }
}
