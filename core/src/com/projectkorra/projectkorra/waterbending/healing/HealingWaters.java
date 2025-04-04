package com.projectkorra.projectkorra.waterbending.healing;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.ElementalAbility;
import com.projectkorra.projectkorra.ability.HealingAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.attribute.markers.DayNightFactor;
import com.projectkorra.projectkorra.chiblocking.Smokescreen;
import com.projectkorra.projectkorra.region.RegionProtection;
import com.projectkorra.projectkorra.util.LightManager;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.waterbending.util.WaterReturn;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.HashMap;

public class HealingWaters extends HealingAbility {

	// Configurable Variables.
	@Attribute(Attribute.COOLDOWN) @DayNightFactor(invert = true)
	private long cooldown;
	@Attribute(Attribute.RANGE) @DayNightFactor
	private double range;
	private long interval;
	@Attribute(Attribute.CHARGE_DURATION) @DayNightFactor(invert = true)
	private long chargeTime;
	@Attribute("PotionPotency")
	private int potionPotency;
	@Attribute(Attribute.DURATION) @DayNightFactor(invert = true)
	private long duration;
	private boolean enableParticles;
	private boolean dynamicLighting;
	private int brightness;
	private long keepAlive;

	// Instance related and predefined variables.
	private Player player;
	private LivingEntity target;
	private Location origin;
	private Location location;
	private long currTime;
	private int pstage;
	private int tstage1;
	private int tstage2;
	private boolean healing = false;
	private boolean healingSelf = false;
	public boolean charged = false;
	private boolean bottle = false;
	private boolean hasReached = false;
	private String hex;

	public HealingWaters(final Player player) {
		super(player);

		if (!this.bPlayer.canBend(this)) {
			this.remove();
			return;
		}

		this.setFields();
		this.player = player;
		this.origin = player.getLocation().clone().add(player.getLocation().getDirection()).add(0, 1.5, 0);
		this.location = this.origin.clone();
		this.currTime = System.currentTimeMillis();
		this.pstage = 0;
		this.tstage1 = 0;
		this.tstage2 = 18;

		this.start();
	}

	public void setFields() {

		this.cooldown = getConfig().getLong("Abilities.Water.HealingWaters.Cooldown");
		this.range = getConfig().getDouble("Abilities.Water.HealingWaters.Range");
		this.interval = getConfig().getLong("Abilities.Water.HealingWaters.Interval");
		this.chargeTime = getConfig().getLong("Abilities.Water.HealingWaters.ChargeTime");
		this.potionPotency = getConfig().getInt("Abilities.Water.HealingWaters.PotionPotency");
		this.duration = getConfig().getLong("Abilities.Water.HealingWaters.Duration");
		this.enableParticles = getConfig().getBoolean("Abilities.Water.HealingWaters.EnableParticles");
		this.dynamicLighting = getConfig().getBoolean("Abilities.Water.HealingWaters.DynamicLight.Enabled");
		this.brightness = getConfig().getInt("Abilities.Water.HealingWaters.DynamicLight.Brightness");
		this.keepAlive = getConfig().getLong("Abilities.Water.HealingWaters.DynamicLight.KeepAlive");
		this.hex = "00ffff";
	}

	@Override
	public void progress() {

		if (!this.bPlayer.canBend(this)) {
			this.remove();
			return;
		}

		if (this.duration != 0) {
			if (System.currentTimeMillis() >= this.getStartTime() + this.duration) {
				this.bPlayer.addCooldown(this);
				this.remove();
				return;
			}
		}

		if (!this.player.isSneaking()) {
			this.bPlayer.addCooldown(this);
			this.remove();
			return;
		}

		if (!this.inWater(this.player) && !WaterReturn.hasWaterBottle(this.player) && !this.charged) {
			this.bPlayer.addCooldown(this);
			this.remove();
			return;
		}

		if (WaterReturn.hasWaterBottle(this.player)) {
			this.bottle = true;
		}

		// If ability is is charged, set charged = true. If not, play charging particles.
		if (System.currentTimeMillis() >= this.getStartTime() + this.chargeTime) {
			if (!this.charged) {
				this.charged = true;
				WaterReturn.emptyWaterBottle(this.player);
			}
		} else {
			GeneralMethods.displayColoredParticle(this.hex, this.origin);
			emitLight(this.origin);
		}

		// If the ability is charged, try healing.
		if (this.charged) {

			if (this.target != null) {
				this.displayHealingWater(this.target);
			} else {
				this.displayHealingWater(this.player);
			}

			// Try to heal themselves/target with 'interval' millisecond intervals.
			if (System.currentTimeMillis() - this.currTime >= this.interval) {

				this.heal(this.player);
				this.currTime = System.currentTimeMillis();
			}

			// Display healing particles.
			if (this.healing && this.enableParticles) {
				if (this.healingSelf) {
					this.displayHealingParticlesSelf();
				} else {
					this.displayHealingParticlesOther();
				}
			}
		}
	}

	public void click() {
		final Entity target = GeneralMethods.getTargetedEntity(this.player, this.range);
		if (target != null && !target.equals(this.target) && target instanceof LivingEntity) {
			this.hasReached = false;
			this.target = (LivingEntity) target;
		} else if (target != null && target.equals(this.target) && target instanceof LivingEntity) {
			this.hasReached = false;
			this.target = null;
		}
	}

	private void heal(final Player player) {
		final Entity target = GeneralMethods.getTargetedEntity(player, this.range);
		if (target != null && this.target != null && target instanceof LivingEntity) {
			if (this.target.getEntityId() == target.getEntityId() && this.hasReached) {
				this.giveHP(this.target);
			}
		} else {
			if (this.hasReached) {
				this.giveHP(player);
				this.target = null;
			}
		}
	}

	private void giveHP(final Player player) {
		if (!player.isDead() && player.getHealth() < player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue()) {
			this.applyHealing(player);
		} else {
			this.healing = false;
		}

		for (final PotionEffect effect : player.getActivePotionEffects()) {
			if (isNegativeEffect(effect.getType())) {
				if ((effect.getType() == PotionEffectType.BLINDNESS) && Smokescreen.getBlindedTimes().containsKey(player.getName())) {
					return;
				}
				player.removePotionEffect(effect.getType());
			}
		}
	}

	private void giveHP(final LivingEntity livingEntity) {
		if (!livingEntity.isDead() && livingEntity.getHealth() < livingEntity.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue()) {
			this.applyHealing(livingEntity);
		} else {
			this.healing = false;
		}

		for (final PotionEffect effect : livingEntity.getActivePotionEffects()) {
			if (ElementalAbility.isNegativeEffect(effect.getType())) {
				livingEntity.removePotionEffect(effect.getType());
			}
		}
	}

	private void applyHealing(final Player player) {
		if (!RegionProtection.isRegionProtected(player, player.getLocation(), "HealingWaters")) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 30, this.potionPotency));
			AirAbility.breakBreathbendingHold(player);
			this.healing = true;
			this.healingSelf = true;
		}
	}

	private void applyHealing(final LivingEntity livingEntity) {
		if (livingEntity.getHealth() < livingEntity.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue()) {
			livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 30, this.potionPotency));
			AirAbility.breakBreathbendingHold(livingEntity);
			this.healing = true;
			this.healingSelf = false;
		}
	}

	private boolean inWater(final Player player) {
		final Block block = player.getLocation().getBlock();
		return (isWater(block) || isCauldron(block)) && !TempBlock.isTempBlock(block);
	}

	public void displayHealingParticlesSelf() {
		if (this.hasReached) {
			final Location centre = this.player.getLocation().clone().add(0, 1, 0);
			final double increment = (2 * Math.PI) / 36;
			final double angle = this.pstage * increment;
			final double x = centre.getX() + (0.75 * Math.cos(angle));
			final double z = centre.getZ() + (0.75 * Math.sin(angle));
			Location loc = new Location(centre.getWorld(), x, centre.getY(), z);
			GeneralMethods.displayColoredParticle(this.hex, loc);
			emitLight(loc);

			if (this.pstage >= 36) {
				this.pstage = 0;
			}
			this.pstage++;
		}
	}

	public void displayHealingParticlesOther() {
		if (this.target != null) {
			if (this.hasReached) {
				final Location centre = this.target.getLocation().clone().add(0, 1, 0);
				final double increment = (2 * Math.PI) / 36;
				final double angle1 = this.tstage1 * increment;
				final double angle2 = this.tstage2 * increment;
				final double x1 = centre.getX() + (0.75 * Math.cos(angle1));
				final double z1 = centre.getZ() + (0.75 * Math.sin(angle1));
				final double x2 = centre.getX() + (0.75 * Math.cos(angle2));
				final double z2 = centre.getZ() + (0.75 * Math.sin(angle2));

				Location point1 = new Location(centre.getWorld(), x1, centre.getY() + (0.75 * Math.cos(angle1)), z1);
				Location point2 = new Location(centre.getWorld(), x2, centre.getY() + (0.75 * -Math.cos(angle2)), z2);

				GeneralMethods.displayColoredParticle(this.hex, point1);
				GeneralMethods.displayColoredParticle(this.hex, point2);

				emitLight(point1);
				emitLight(point2);

				if (this.tstage1 >= 36) {
					this.tstage1 = 0;
				}
				this.tstage1++;

				if (this.tstage2 >= 36) {
					this.tstage2 = 0;
				}
				this.tstage2++;
			}

		}
	}

	public void displayHealingWater(final LivingEntity target) {
		final double factor = 0.2;

		final Location targetLoc = target.getLocation().clone().add(0, 1, 0);
		double distance = 0;
		if (this.location.getWorld().equals(targetLoc.getWorld())) {
			distance = this.location.distance(targetLoc);
		}
		final Vector vec = new Vector(targetLoc.getX() - this.location.getX(), targetLoc.getY() - this.location.getY(), targetLoc.getZ() - this.location.getZ()).normalize();

		if (this.location.getWorld().equals(targetLoc.getWorld()) && this.location.distance(targetLoc) <= distance) {
			this.location = this.location.clone().add(vec.clone().multiply(factor));
			if (this.location.distance(targetLoc) <= 0.5) {
				this.hasReached = true;
			} else {
				this.hasReached = false;
			}
		}

		GeneralMethods.displayColoredParticle(this.hex, this.location);
		emitLight(this.location);
	}

	private void fillBottle() {
		final PlayerInventory inventory = this.player.getInventory();
		if (inventory.contains(Material.GLASS_BOTTLE)) {
			final int index = inventory.first(Material.GLASS_BOTTLE);
			final ItemStack item = inventory.getItem(index);

			final ItemStack water = WaterReturn.waterBottleItem();

			if (item.getAmount() == 1) {
				inventory.setItem(index, water);
			} else {
				item.setAmount(item.getAmount() - 1);
				inventory.setItem(index, item);
				final HashMap<Integer, ItemStack> leftover = inventory.addItem(water);
				for (final int left : leftover.keySet()) {
					this.player.getWorld().dropItemNaturally(this.player.getLocation(), leftover.get(left));
				}
			}
		}
	}

	private void emitLight(Location loc) {
		LightManager.createLight(loc).brightness(this.brightness).timeUntilFadeout(this.keepAlive).emit();
	}

	@Override
	public void remove() {
		if (this.bottle && this.charged) {
			this.fillBottle();
		}
		super.remove();
	}

	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		return true;
	}

	@Override
	public long getCooldown() {
		return this.cooldown;
	}

	@Override
	public String getName() {
		return "HealingWaters";
	}

	@Override
	public Location getLocation() {
		return this.location;
	}

}
