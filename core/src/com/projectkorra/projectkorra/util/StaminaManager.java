package com.projectkorra.projectkorra.util;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Manager;
import com.projectkorra.projectkorra.ProjectKorra;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StaminaManager extends Manager implements Runnable {
    private final ProjectKorra plugin = ProjectKorra.plugin;
    private final Map<UUID, StaminaBar> staminaBars = new HashMap<>();

    private int tickCounter = 0;
    private static final int STAMINA_REGEN_INTERVAL = 60;

    @Override
    public void onActivate() {
        this.scheduleStaminaTask();
    }

    private void scheduleStaminaTask() {
        this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, this, 1, 1);
    }

    @Override
    public void run() {
        tickCounter++;

        for (final Player player : this.plugin.getServer().getOnlinePlayers()) {
            BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

            if (tickCounter >= STAMINA_REGEN_INTERVAL) {
                if (bPlayer.getCurrentStamina() >= bPlayer.getMaxStamina()) {
                    bPlayer.setCurrentStamina(bPlayer.getMaxStamina());
                } else {
                    bPlayer.setCurrentStamina(bPlayer.getCurrentStamina() + 1);
                }
            }

            updateStaminaBar(player);
        }

        if (tickCounter >= STAMINA_REGEN_INTERVAL) {
            tickCounter = 0;
        }
    }

    public void updateStaminaBar(Player player) {
        StaminaBar staminaBar = staminaBars.get(player.getUniqueId());

        if (staminaBar == null) {
            staminaBar = new StaminaBar(player);
            staminaBars.put(player.getUniqueId(), staminaBar);
            staminaBar.show();
        }

        staminaBar.updateStaminaBar();
    }

    /**
     * Removes stamina bar for a player
     */
    public void removeStaminaBar(Player player) {
        StaminaBar staminaBar = staminaBars.remove(player.getUniqueId());
        if (staminaBar != null) {
            staminaBar.remove();
        }
    }

    /**
     * Gets the stamina bar for a player
     */
    public StaminaBar getStaminaBar(Player player) {
        return staminaBars.get(player.getUniqueId());
    }
}
