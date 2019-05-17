package com.playmonumenta.plugins.abilities.warlock.tenebrist;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.abilities.Ability;
import com.playmonumenta.plugins.abilities.AbilityTrigger;
import com.playmonumenta.plugins.classes.Spells;
import com.playmonumenta.plugins.utils.EntityUtils;
import com.playmonumenta.plugins.utils.InventoryUtils;
import com.playmonumenta.plugins.utils.LocationUtils;
import com.playmonumenta.plugins.utils.MetadataUtils;
import com.playmonumenta.plugins.utils.PotionUtils;

/*
 * Fractal Enervation: Sprint right-click fires a dark magic beam
 * (max range: 9), afflicting all enemies it hits with blindness that
 * lasts 12s. The beam then instantly
 * spreads to all enemies in a 3 / 4-block radius, and then from them,
 * and so on. All debuffs on the enemies increase by 1 effect level.
 * At level 2, each enemy hit is dealt 5 damage.
 * Cooldown: 16s / 13s
 */
public class FractalEnervation extends Ability {

	private static final String CHECK_ONCE_THIS_TICK_METAKEY = "FractalTickRightClicked";

	private static final int FRACTAL_DAMAGE = 5;
	private static final int FRACTAL_BLINDNESS_DURATION = 20 * 12;
	private static final int FRACTAL_1_CHAIN_RANGE = 3 + 1; // The +1 accounts for the mob's nonzero hitbox so that the distance between 2 mobs is approx 3 still
	private static final int FRACTAL_2_CHAIN_RANGE = 4 + 1;

	private int mRightClicks = 0;

	public FractalEnervation(Plugin plugin, World world, Random random, Player player) {
		super(plugin, world, random, player);
		mInfo.scoreboardId = "Fractal";
		mInfo.linkedSpell = Spells.FRACTAL_ENERVATION;
		mInfo.trigger = AbilityTrigger.RIGHT_CLICK;
		mInfo.cooldown = getAbilityScore() == 1 ? 20 * 16 : 20 * 13;
	}

	private List<LivingEntity> hit = new ArrayList<LivingEntity>();

	@Override
	public void cast() {
		if (MetadataUtils.checkOnceThisTick(mPlugin, mPlayer, CHECK_ONCE_THIS_TICK_METAKEY)) {
			mRightClicks++;
			new BukkitRunnable() {
				@Override
				public void run() {
					if (mRightClicks > 0) {
						mRightClicks--;
					}
					this.cancel();
				}
			}.runTaskLater(mPlugin, 5);
		}
		if (mRightClicks < 2) {
			return;
		}
		mRightClicks = 0;

		hit.clear();
		mWorld.playSound(mPlayer.getLocation(), Sound.BLOCK_BUBBLE_COLUMN_WHIRLPOOL_INSIDE, 1, 0.9f);
		BoundingBox box = BoundingBox.of(mPlayer.getEyeLocation(), 0.7, 0.7, 0.7);
		Vector dir = mPlayer.getEyeLocation().getDirection();
		List<LivingEntity> mobs = EntityUtils.getNearbyMobs(mPlayer.getEyeLocation(), 9, mPlayer);
		int chainRange = getAbilityScore() == 1 ? FRACTAL_1_CHAIN_RANGE : FRACTAL_2_CHAIN_RANGE;
		int range = 9;
		boolean cancel = false;
		for (int i = 0; i < range; i++) {
			box.shift(dir);
			Location loc = box.getCenter().toLocation(mWorld);
			mWorld.spawnParticle(Particle.SPELL_WITCH, loc, 5, 0.15, 0.15,
			                     0.15, 0.15);
			mWorld.spawnParticle(Particle.SMOKE_NORMAL, loc, 4, 0.15, 0.15,
			                     0.15, 0.075);
			mWorld.spawnParticle(Particle.SMOKE_LARGE, loc, 2, 0.1, 0.1, 0.1,
			                     0.1);
			for (LivingEntity mob : mobs) {
				if (mob.getBoundingBox().overlaps(box)) {
					if (!hit.contains(mob)) {
						hit.add(mob);
						for (PotionEffectType types : PotionUtils.getNegativeEffects(mob)) {
							PotionEffect effect = mob.getPotionEffect(types);
							mob.removePotionEffect(types);
							mob.addPotionEffect(
							    new PotionEffect(types, effect.getDuration(), effect.getAmplifier() + 1));
						}
						PotionUtils.applyPotion(mPlayer, mob, new PotionEffect(PotionEffectType.SLOW_DIGGING, FRACTAL_BLINDNESS_DURATION, 0));
						if (getAbilityScore() > 1) {
							EntityUtils.damageEntity(mPlugin, mob, FRACTAL_DAMAGE, mPlayer);
						}
						mWorld.spawnParticle(Particle.SPELL_WITCH, loc, 40, 0.25, 0.45, 0.25, 0.15);
						mWorld.spawnParticle(Particle.SPELL_MOB, loc, 20, 0.25, 0.45, 0.25, 0);
						i = 0;
						LivingEntity nextMob = null;
						double dist = 100;
						for (LivingEntity next : EntityUtils.getNearbyMobs(mob.getLocation(), chainRange, mPlayer)) {
							if (next.getLocation().distance(loc) < dist && !next.getUniqueId().equals(mob.getUniqueId())
							    && !hit.contains(next)) {
								nextMob = next;
								dist = next.getLocation().distance(loc);
							}
						}
						if (nextMob != null) {
							Vector to = LocationUtils.getDirectionTo(nextMob.getLocation().add(0, nextMob.getHeight() / 2, 0), loc);
							dir = to;
							range = chainRange;
						} else {
							cancel = true;
						}
						break;
					}
				}
			}

			if (loc.getBlock().getType().isSolid() || cancel) {
				break;
			}

		}
		putOnCooldown();
	}

	@Override
	public boolean runCheck() {
		ItemStack mHand = mPlayer.getInventory().getItemInMainHand();
		return InventoryUtils.isScytheItem(mHand);
	}

}
