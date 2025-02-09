package com.projectkorra.projectkorra.ability.util;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.util.ParticleEffect;
import org.bukkit.Location;

public interface CustomParticleHandler {
	boolean canHandle(BendingPlayer bPlayer);
	void displayParticles(BendingPlayer bPlayer, CoreAbility ability, Location loc, int amount, double xOffset, double yOffset, double zOffset);
	ParticleEffect getParticleEffect();
}
