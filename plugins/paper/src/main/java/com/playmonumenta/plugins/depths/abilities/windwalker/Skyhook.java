package com.playmonumenta.plugins.depths.abilities.windwalker;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.abilities.Ability;
import com.playmonumenta.plugins.abilities.AbilityInfo;
import com.playmonumenta.plugins.abilities.AbilityManager;
import com.playmonumenta.plugins.classes.ClassAbility;
import com.playmonumenta.plugins.depths.DepthsTree;
import com.playmonumenta.plugins.depths.DepthsUtils;
import com.playmonumenta.plugins.depths.abilities.DepthsAbility;
import com.playmonumenta.plugins.depths.abilities.DepthsTrigger;
import com.playmonumenta.plugins.depths.abilities.aspects.BowAspect;
import com.playmonumenta.plugins.depths.abilities.steelsage.RapidFire;
import com.playmonumenta.plugins.events.DamageEvent;
import com.playmonumenta.plugins.events.DamageEvent.DamageType;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.AbstractArrow.PickupStatus;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Skyhook extends DepthsAbility {
	public static final String ABILITY_NAME = "Skyhook";
	public static final int[] COOLDOWN = {16 * 20, 14 * 20, 12 * 20, 10 * 20, 8 * 20, 4 * 20};
	public static final int MAX_TICKS = 20 * 20;
	public static final String SKYHOOK_ARROW_METADATA = "SkyhookArrow";

	public Skyhook(Plugin plugin, Player player) {
		super(plugin, player, ABILITY_NAME);
		mDisplayMaterial = Material.FISHING_ROD;
		mTree = DepthsTree.WINDWALKER;
		mInfo.mLinkedSpell = ClassAbility.SKYHOOK;
		mInfo.mCooldown = mRarity == 0 ? COOLDOWN[0] : COOLDOWN[mRarity - 1];
		mInfo.mIgnoreCooldown = true;
	}

	@Override
	public boolean onDamage(DamageEvent event, LivingEntity enemy) {
		Entity damager = event.getDamager();
		if (event.getType() == DamageType.PROJECTILE && damager instanceof AbstractArrow && damager.hasMetadata(SKYHOOK_ARROW_METADATA)) {
			hook(damager);
			damager.removeMetadata(SKYHOOK_ARROW_METADATA, mPlugin);
		}
		return false; // prevents multiple calls itself
	}

	// Since Snowballs disappear after landing, we need an extra detection for when it hits the ground.
	@Override
	public void projectileHitEvent(ProjectileHitEvent event, Projectile proj) {
		if (mPlayer != null && proj instanceof Snowball && proj.hasMetadata(SKYHOOK_ARROW_METADATA)) {
			hook(proj);
			proj.removeMetadata(SKYHOOK_ARROW_METADATA, mPlugin);
		}
	}

	private void hook(Entity arrow) {
		Location loc = arrow.getLocation();
		Location playerStartLoc = mPlayer.getLocation();
		World world = mPlayer.getWorld();

		if (loc.clone().add(0, 0.5, 0).getBlock().isSolid()) {
			loc = loc.subtract(0, 1, 0);
		}

		//Check if the location is in an inescapable place for the player
		if (!(loc.getBlock().getType() == Material.BEDROCK) && !(loc.getBlock().getRelative(BlockFace.UP).getType() == Material.BEDROCK)) {
			Location pLoc = loc.clone().add(0, 0.5, 0);
			Vector dir = mPlayer.getLocation().toVector().subtract(loc.toVector()).normalize();
			for (int i = 0; i <= mPlayer.getLocation().distance(loc); i++) {
				pLoc.add(dir);

				world.spawnParticle(Particle.SWEEP_ATTACK, pLoc, 5, 0.25, 0.25, 0.25, 0);
				world.spawnParticle(Particle.CLOUD, pLoc, 10, 0.05, 0.05, 0.05, 0.05);
			}

			world.playSound(mPlayer.getLocation(), Sound.ITEM_TRIDENT_RIPTIDE_1, 1, 1.5f);
			world.spawnParticle(Particle.SMOKE_LARGE, mPlayer.getLocation(), 10, .5, .2, .5, 0.65);
			world.spawnParticle(Particle.CLOUD, loc, 10, .5, .2, .5, 0.65);
			world.spawnParticle(Particle.SWEEP_ATTACK, loc, 5, .5, .2, .5, 0.65);
			loc.setDirection(mPlayer.getEyeLocation().getDirection());
			mPlayer.teleport(loc);

			//Refund cooldowns
			for (Ability abil : AbilityManager.getManager().getPlayerAbilities(mPlayer).getAbilities()) {
				AbilityInfo info = abil.getInfo();
				ClassAbility spell = info.mLinkedSpell;
				if (spell == null || spell == mInfo.mLinkedSpell) {
					continue;
				}
				int totalCD = info.mCooldown;
				int reducedCD = (int) (totalCD * (loc.distance(playerStartLoc) / 100.0));
				mPlugin.mTimers.updateCooldown(mPlayer, spell, reducedCD);
			}
		}
		world.playSound(mPlayer.getLocation(), Sound.ITEM_TRIDENT_RIPTIDE_1, 1, 1.5f);

		arrow.remove();
	}

	@Override
	public boolean playerShotProjectileEvent(Projectile projectile) {
		if (mPlugin.mTimers.isAbilityOnCooldown(mPlayer.getUniqueId(), mInfo.mLinkedSpell) || projectile.hasMetadata(RapidFire.META_DATA_TAG)) {
			return true;
		}

		if (mPlayer.isSneaking()) {
			mInfo.mCooldown = (int) (COOLDOWN[mRarity - 1] * BowAspect.getCooldownReduction(mPlayer));
			putOnCooldown();
			World world = mPlayer.getWorld();
			Location loc = mPlayer.getLocation();
			world.playSound(loc, Sound.ITEM_CROSSBOW_QUICK_CHARGE_3, 1, 1.0f);

			if (projectile instanceof AbstractArrow arrow) {
				arrow.setPierceLevel(0);
				arrow.setCritical(true);
				arrow.setPickupStatus(PickupStatus.CREATIVE_ONLY);
			}
			projectile.setMetadata(SKYHOOK_ARROW_METADATA, new FixedMetadataValue(mPlugin, 0));

			mPlugin.mProjectileEffectTimers.addEntity(projectile, Particle.FIREWORKS_SPARK);

			new BukkitRunnable() {
				int mT = 0;

				@Override
				public void run() {
					if (projectile == null || mT > MAX_TICKS) {
						mPlugin.mProjectileEffectTimers.removeEntity(projectile);
						projectile.removeMetadata(SKYHOOK_ARROW_METADATA, mPlugin);
						projectile.remove();
						this.cancel();
					}

					if (projectile.getVelocity().length() < .05 || projectile.isOnGround()) {
						hook(projectile);
						this.cancel();
					}
					mT++;
				}

			}.runTaskTimer(mPlugin, 0, 1);
		}

		return true;
	}

	@Override
	public String getDescription(int rarity) {
		return "Shooting a bow while sneaking shoots out a skyhook. When the skyhook lands, you dash to the location and reduce all ability cooldowns by 1% per block traveled. Cooldown: " + DepthsUtils.getRarityColor(rarity) + COOLDOWN[rarity - 1] / 20 + "s" + ChatColor.WHITE + ".";
	}

	@Override
	public DepthsTree getDepthsTree() {
		return DepthsTree.WINDWALKER;
	}

	@Override
	public DepthsTrigger getTrigger() {
		return DepthsTrigger.SHIFT_BOW;
	}
}
