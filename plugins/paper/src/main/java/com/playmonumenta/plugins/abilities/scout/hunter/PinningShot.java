package com.playmonumenta.plugins.abilities.scout.hunter;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.abilities.Ability;
import com.playmonumenta.plugins.events.DamageEvent;
import com.playmonumenta.plugins.events.DamageEvent.DamageType;
import com.playmonumenta.plugins.utils.DamageUtils;
import com.playmonumenta.plugins.utils.EntityUtils;



public class PinningShot extends Ability {

	private static final double PINNING_SHOT_1_DAMAGE_MULTIPLIER = 0.1;
	private static final double PINNING_SHOT_2_DAMAGE_MULTIPLIER = 0.2;
	private static final int PINNING_SHOT_DURATION = (int) (20 * 2.5);
	private static final double PINNING_SLOW = 1.0;
	private static final double PINNING_SLOW_BOSS = 0.3;
	private static final double PINNING_WEAKEN_1 = 0.3;
	private static final double PINNING_WEAKEN_2 = 0.6;

	private final double mDamageMultiplier;
	private final double mWeaken;

	private final Map<LivingEntity, Boolean> mPinnedMobs = new HashMap<LivingEntity, Boolean>();

	public PinningShot(Plugin plugin, @Nullable Player player) {
		super(plugin, player, "Pinning Shot");
		mInfo.mScoreboardId = "PinningShot";
		mInfo.mShorthandName = "PSh";
		mInfo.mDescriptions.add("The first time you shoot a non-boss enemy, pin it for 2.5s. Pinned enemies are afflicted with 100% Slowness and 30% Weaken (Bosses receive 30% Slowness and no Weaken). Shooting a pinned non-boss enemy deals 10% of its max health on top of regular damage and removes the pin. A mob cannot be pinned more than once.");
		mInfo.mDescriptions.add("Weaken increased to 60% and bonus damage increased to 20% max health.");
		mInfo.mIgnoreTriggerCap = true;
		mDisplayItem = new ItemStack(Material.CROSSBOW, 1);

		mDamageMultiplier = getAbilityScore() == 1 ? PINNING_SHOT_1_DAMAGE_MULTIPLIER : PINNING_SHOT_2_DAMAGE_MULTIPLIER;
		mWeaken = getAbilityScore() == 1 ? PINNING_WEAKEN_1 : PINNING_WEAKEN_2;
	}

	@Override
	public void onDamage(DamageEvent event, LivingEntity enemy) {
		if (event.getType() != DamageType.PROJECTILE || !(event.getDamager() instanceof AbstractArrow)) {
			return;
		}

		World world = mPlayer.getWorld();
		if (mPinnedMobs.containsKey(enemy)) {
			// If currently pinned
			if (mPinnedMobs.get(enemy)) {
				Location loc = enemy.getEyeLocation();
				world.playSound(loc, Sound.BLOCK_GLASS_BREAK, 1, 0.5f);
				world.spawnParticle(Particle.FIREWORKS_SPARK, loc, 20, 0, 0, 0, 0.2);
				world.spawnParticle(Particle.SNOWBALL, loc, 30, 0, 0, 0, 0.25);
				EntityUtils.setSlowTicks(mPlugin, enemy, 1);
				EntityUtils.setWeakenTicks(mPlugin, enemy, 1);
				if (!EntityUtils.isBoss(enemy)) {
					DamageUtils.damage(mPlayer, enemy, DamageType.OTHER, EntityUtils.getMaxHealth(enemy) * mDamageMultiplier, mInfo.mLinkedSpell, true, false);
				}
				mPinnedMobs.put(enemy, false);
			}
		} else if (!mPinnedMobs.containsKey(enemy)) {
			Location loc = enemy.getLocation();
			world.playSound(loc, Sound.BLOCK_SLIME_BLOCK_PLACE, 1, 0.5f);
			world.spawnParticle(Particle.EXPLOSION_NORMAL, loc, 8, 0, 0, 0, 0.2);
			if (EntityUtils.isBoss(enemy)) {
				EntityUtils.applySlow(mPlugin, PINNING_SHOT_DURATION, PINNING_SLOW_BOSS, enemy);
			} else {
				EntityUtils.applySlow(mPlugin, PINNING_SHOT_DURATION, PINNING_SLOW, enemy);
				EntityUtils.applyWeaken(mPlugin, PINNING_SHOT_DURATION, mWeaken, enemy);
			}
			mPinnedMobs.put(enemy, true);
			new BukkitRunnable() {
				@Override
				public void run() {
					mPinnedMobs.put(enemy, false);
				}
			}.runTaskLater(mPlugin, PINNING_SHOT_DURATION);
		}
	}

	@Override
	public void periodicTrigger(boolean twoHertz, boolean oneSecond, int ticks) {
		if (oneSecond) {
			mPinnedMobs.entrySet().removeIf((entry) -> !entry.getKey().isValid() || entry.getKey().isDead());
		}
	}

}
