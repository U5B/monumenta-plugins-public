package com.playmonumenta.plugins.abilities.warlock;

import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.abilities.Ability;
import com.playmonumenta.plugins.abilities.AbilityTrigger;
import com.playmonumenta.plugins.classes.ClassAbility;
import com.playmonumenta.plugins.events.DamageEvent;
import com.playmonumenta.plugins.events.DamageEvent.DamageType;
import com.playmonumenta.plugins.listeners.DamageListener;
import com.playmonumenta.plugins.utils.DamageUtils;
import com.playmonumenta.plugins.utils.EntityUtils;
import com.playmonumenta.plugins.utils.ItemStatUtils;
import com.playmonumenta.plugins.utils.ItemUtils;
import com.playmonumenta.plugins.utils.MovementUtils;



public class GraspingClaws extends Ability {

	private static final int RADIUS = 8;
	private static final float PULL_SPEED = 0.175f;
	private static final double AMPLIFIER_1 = 0.2;
	private static final double AMPLIFIER_2 = 0.3;
	private static final int DAMAGE_1 = 3;
	private static final int DAMAGE_2 = 8;
	private static final int DURATION = 8 * 20;
	private static final int COOLDOWN_1 = 16 * 20;
	private static final int COOLDOWN_2 = 12 * 20;

	private final double mAmplifier;
	private final int mDamage;
	private @Nullable Arrow mArrow = null;

	public GraspingClaws(Plugin plugin, @Nullable Player player) {
		super(plugin, player, "Grasping Claws");
		mInfo.mScoreboardId = "GraspingClaws";
		mInfo.mShorthandName = "GC";
		mInfo.mDescriptions.add("Left-clicking while shifted while holding a bow or crossbow fires an arrow that pulls nearby enemies towards your arrow once it makes contact with a mob or block. Mobs caught in the arrow's 8 block radius are given 20% Slowness for 8 seconds and take 3 magic damage. Cooldown: 16s.");
		mInfo.mDescriptions.add("The pulled enemies now take 8 damage, and their Slowness is increased to 30%.");
		mInfo.mLinkedSpell = ClassAbility.GRASPING_CLAWS;
		mInfo.mCooldown = getAbilityScore() == 1 ? COOLDOWN_1 : COOLDOWN_2;
		mInfo.mTrigger = AbilityTrigger.LEFT_CLICK;
		mInfo.mIgnoreCooldown = true;
		mDisplayItem = new ItemStack(Material.BOW, 1);
		mAmplifier = getAbilityScore() == 1 ? AMPLIFIER_1 : AMPLIFIER_2;
		mDamage = getAbilityScore() == 1 ? DAMAGE_1 : DAMAGE_2;
	}

	@Override
	public void cast(Action action) {
		if (mPlayer == null) {
			return;
		}
		ItemStack inMainHand = mPlayer.getInventory().getItemInMainHand();
		if (!mPlugin.mTimers.isAbilityOnCooldown(mPlayer.getUniqueId(), ClassAbility.GRASPING_CLAWS) && mPlayer.isSneaking() && ItemUtils.isBowOrTrident(inMainHand) && !ItemStatUtils.isShattered(inMainHand)) {
			mArrow = mPlayer.getWorld().spawnArrow(mPlayer.getEyeLocation(), mPlayer.getLocation().getDirection(), 1.5f, 0, Arrow.class);
			mArrow.setShooter(mPlayer);
			mArrow.setDamage(0);
			mArrow.setPickupStatus(AbstractArrow.PickupStatus.CREATIVE_ONLY);
			mPlugin.mProjectileEffectTimers.addEntity(mArrow, Particle.SPELL_WITCH);
			putOnCooldown();
		}
	}

	@Override
	public void projectileHitEvent(ProjectileHitEvent event, Projectile proj) {
		if (mPlayer != null && this.mArrow != null && this.mArrow == proj) {
			this.mArrow = null;
			Location loc = proj.getLocation();
			World world = proj.getWorld();

			world.playSound(loc, Sound.BLOCK_BEACON_POWER_SELECT, 1.25f, 1.25f);
			world.playSound(loc, Sound.BLOCK_PORTAL_TRIGGER, 1.25f, 1.45f);
			world.playSound(loc, Sound.ENTITY_ENDER_DRAGON_HURT, 1.25f, 0.65f);
			world.spawnParticle(Particle.PORTAL, loc, 125, 2, 2, 2, 0.25);
			world.spawnParticle(Particle.PORTAL, loc, 400, 0, 0, 0, 1.45);
			world.spawnParticle(Particle.DRAGON_BREATH, loc, 85, 0, 0, 0, 0.125);
			world.spawnParticle(Particle.FALLING_DUST, loc, 150, 2, 2, 2, Material.ANVIL.createBlockData());

			for (LivingEntity mob : EntityUtils.getNearbyMobs(loc, RADIUS, mPlayer)) {
				Object value = proj.getMetadata(DamageListener.PROJECTILE_ITEM_STATS_METAKEY).get(0);
				if (value instanceof FixedMetadataValue playerItemStats) {
					DamageEvent damageEvent = new DamageEvent(mob, mPlayer, mPlayer, DamageType.MAGIC, mInfo.mLinkedSpell, mDamage);
					damageEvent.setDelayed(true);
					damageEvent.setPlayerItemStat(playerItemStats);
					DamageUtils.damage(damageEvent, false, true, null);
				} else {
					mPlugin.getLogger().log(Level.WARNING, "Malformed ProjectileItemStats metadata detected (Grasping Claws)");
				}

				MovementUtils.pullTowards(proj, mob, PULL_SPEED);
				EntityUtils.applySlow(mPlugin, DURATION, mAmplifier, mob);
			}

			proj.remove();
		}
	}
}
