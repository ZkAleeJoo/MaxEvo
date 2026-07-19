package org.zkaleejoo.evolution;

import java.util.HashMap;
import java.util.Map;

public class PlayerProgress {

    private final Map<String, ToolProgress> data = new HashMap<>();

    public void addProgress(String itemType, String missionKey, int amount) {
        ToolProgress tp = data.computeIfAbsent(itemType.toUpperCase(), k -> new ToolProgress());
        tp.missionCounters.merge(missionKey, amount, (a, b) -> (a == null ? 0 : a) + (b == null ? 0 : b));
    }

    public int getCurrentLevel(String itemType) {
        ToolProgress tp = data.get(itemType.toUpperCase());
        return tp != null ? tp.currentLevel : 1;
    }

    public void levelUp(String itemType) {
        ToolProgress tp = data.computeIfAbsent(itemType.toUpperCase(), k -> new ToolProgress());
        tp.currentLevel++;
        tp.missionCounters.clear();
    }

    public Map<String, Integer> getMissionCounters(String itemType) {
        ToolProgress tp = data.get(itemType.toUpperCase());
        return tp != null ? tp.missionCounters : new HashMap<>();
    }

    public int getMissionProgress(String itemType, String missionKey) {
        return getMissionCounters(itemType).getOrDefault(missionKey, 0);
    }

    private static class ToolProgress {
        int currentLevel = 1;
        final Map<String, Integer> missionCounters = new HashMap<>();
    }
}
