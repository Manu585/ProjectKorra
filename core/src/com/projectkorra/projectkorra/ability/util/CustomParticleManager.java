package com.projectkorra.projectkorra.ability.util;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ability.CoreAbility;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class CustomParticleManager {
	private static final List<CustomParticleHandler> handlers = new ArrayList<>();

	public static void registerHandler(CustomParticleHandler handler) {
		handlers.add(handler);
	}

	public static boolean displayCustomParticles(BendingPlayer bPlayer, CoreAbility ability, Location loc, int amount, double xOffset, double yOffset, double zOffset) {
		for (CustomParticleHandler handler : handlers) {
			if (handler.canHandle(bPlayer)) {
				handler.displayParticles(bPlayer, ability, loc, amount, xOffset, yOffset, zOffset);
				return true;
			}
		}
		return false;
	}

}
