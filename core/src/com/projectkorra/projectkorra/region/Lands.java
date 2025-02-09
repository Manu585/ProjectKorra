package com.projectkorra.projectkorra.region;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.versions.modern.ModernSkullProvider;
import me.angeschossen.lands.api.LandsIntegration;
import me.angeschossen.lands.api.flags.enums.FlagTarget;
import me.angeschossen.lands.api.flags.type.NaturalFlag;
import me.angeschossen.lands.api.land.Area;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Lands extends RegionProtectionBase {
    private static LandsIntegration landsIntegration;
    private static NaturalFlag bendingNaturalFlag;

    protected Lands() {
        super("Lands");
    }

    @Override
    public boolean isRegionProtectedReal(Player player, Location location, CoreAbility ability, boolean igniteAbility, boolean explosiveAbility) {
        final Area area = landsIntegration.getArea(location);
        final boolean isClaimed = area != null;

        if (isClaimed && !player.hasPermission("lands.bypass.bending")) {
            return !area.hasNaturalFlag(bendingNaturalFlag);
        }
        return false;
    }

    public static void createBendingFlags(ProjectKorra pk) {
        ItemStack aangSkull = getItemStack();

        landsIntegration = LandsIntegration.of(pk);

        bendingNaturalFlag = NaturalFlag.of(landsIntegration, FlagTarget.PLAYER,"bending");

        bendingNaturalFlag.setDisplayName("Bending")
                .setIcon(aangSkull)
                .setDescription("Enable or Disable bending in areas!")
                .setAlwaysAllowInWilderness(true)
                .setDefaultState(false)
                .setActiveInWar(true)
                .setApplyInSubareas(true);
    }

    private static ItemStack getItemStack() {
        String textureVal = "http://textures.minecraft.net/texture/e45c254da148921001dad0ebbd841802ed2e6c6f9fb08df99e8da3e4b2a74e54";
        return new ModernSkullProvider().getSkull(textureVal);
    }
}
