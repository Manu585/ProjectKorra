package com.projectkorra.projectkorra.firebending.util;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.FireAbility;

public class FireStaminaUtil {
    public static void handleNoStaminaAnimation() {
        for (CoreAbility ability : CoreAbility.getAbilitiesByElement(Element.FIRE)) {
            if (ability == null) return;

            BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(ability.getPlayer());
            int stamina = bPlayer.getCurrentStamina();
            if (stamina < ability.getStaminaCost()) {
                FireAbility.playOutOfStaminaAnimation(GeneralMethods.getMainHandLocation(bPlayer.getPlayer()));
            }
        }
    }
}
