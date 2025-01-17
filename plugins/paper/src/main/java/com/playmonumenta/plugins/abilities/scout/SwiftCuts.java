package com.playmonumenta.plugins.abilities.scout;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.abilities.Ability;
import com.playmonumenta.plugins.abilities.AbilityInfo;
import com.playmonumenta.plugins.effects.Effect;
import com.playmonumenta.plugins.effects.PercentDamageDealt;
import com.playmonumenta.plugins.events.DamageEvent;
import com.playmonumenta.plugins.events.DamageEvent.DamageType;
import com.playmonumenta.plugins.itemstats.abilities.CharmManager;
import com.playmonumenta.plugins.particle.PartialParticle;
import com.playmonumenta.plugins.utils.DamageUtils;
import com.playmonumenta.plugins.utils.EntityUtils;
import java.util.EnumSet;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class SwiftCuts extends Ability {
	private static final double CONSECUTIVE_PERCENT_DAMAGE_1 = 0.2;
	private static final double CONSECUTIVE_PERCENT_DAMAGE_2 = 0.35;
	private static final double SWEEP_RADIUS = 2.5;
	private static final double PERCENT_AOE_DAMAGE_1 = 0.1;
	private static final double PERCENT_AOE_DAMAGE_2 = 0.2;
	private static final double ENHANCEMENT_DAMAGE_PERCENT = 0.35;
	private static final int ENHANCEMENT_CDR = 20;

	public static final String CHARM_DAMAGE = "Swift Cuts Damage";
	public static final String CHARM_SWEEP_DAMAGE = "Swift Cuts Sweep Damage";
	public static final String CHARM_RADIUS = "Swift Cuts Radius";

	public static final AbilityInfo<SwiftCuts> INFO =
		new AbilityInfo<>(SwiftCuts.class, "Swift Cuts", SwiftCuts::new)
			.scoreboardId("SwiftCuts")
			.shorthandName("SC")
			.descriptions(
				String.format("If you perform a melee attack on the same mob 2 or more times in a row, each hit after the first does +%d%% damage and deals %d%% of the damage to all other mobs in a %s block radius.", (int) (CONSECUTIVE_PERCENT_DAMAGE_1 * 100), (int) (PERCENT_AOE_DAMAGE_1 * 100), SWEEP_RADIUS),
				String.format("Bonus damage increased to +%d%%, sweep damage increased to %d%%.", (int) (CONSECUTIVE_PERCENT_DAMAGE_2 * 100), (int) (PERCENT_AOE_DAMAGE_2 * 100)),
				"Every third fully charged melee attack in a row against the same mob deals " + (int) (ENHANCEMENT_DAMAGE_PERCENT * 100) + "% more damage and reduces the cooldowns of your skills by " + (ENHANCEMENT_CDR / 20) + "s.")
			.displayItem(new ItemStack(Material.STONE_SWORD, 1));

	private final double mConsecutivePercentDamage;
	private final double mPercentAoEDamage;

	private @Nullable LivingEntity mLastTarget = null;
	private int mEnhancementHits = 0;

	public SwiftCuts(Plugin plugin, Player player) {
		super(plugin, player, INFO);
		mConsecutivePercentDamage = (isLevelOne() ? CONSECUTIVE_PERCENT_DAMAGE_1 : CONSECUTIVE_PERCENT_DAMAGE_2) + CharmManager.getLevelPercentDecimal(mPlayer, CHARM_DAMAGE);
		mPercentAoEDamage = isLevelOne() ? PERCENT_AOE_DAMAGE_1 : PERCENT_AOE_DAMAGE_2;
	}

	@Override
	public boolean onDamage(DamageEvent event, LivingEntity enemy) {
		if (event.getType() == DamageType.MELEE) {
			if (enemy.equals(mLastTarget)) {
				Location loc = enemy.getLocation();
				World world = mPlayer.getWorld();
				world.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 0.5f, 1.5f);
				new PartialParticle(Particle.SWEEP_ATTACK, loc, 2, 0.25, 0.35, 0.25, 0.001).spawnAsPlayerActive(mPlayer);

				event.setDamage(event.getDamage() * (1 + mConsecutivePercentDamage));

				double sweepDamage = CharmManager.calculateFlatAndPercentValue(mPlayer, CHARM_SWEEP_DAMAGE, event.getDamage() * mPercentAoEDamage);

				if (mPlugin.mEffectManager.hasEffect(mPlayer, PercentDamageDealt.class)) {
					for (Effect priorityEffects : mPlugin.mEffectManager.getPriorityEffects(mPlayer).values()) {
						if (priorityEffects instanceof PercentDamageDealt damageEffect) {
							EnumSet<DamageType> types = damageEffect.getAffectedDamageTypes();
							if (types == null || types.contains(DamageType.MELEE)) {
								sweepDamage = sweepDamage * (1 + damageEffect.getMagnitude() * (damageEffect.isBuff() ? 1 : -1));
							}
						}
					}
				}

				for (LivingEntity mob : EntityUtils.getNearbyMobs(loc, CharmManager.getRadius(mPlayer, CHARM_RADIUS, SWEEP_RADIUS), enemy)) {
					DamageUtils.damage(mPlayer, mob, DamageType.OTHER, sweepDamage, mInfo.getLinkedSpell(), true, true);
				}

				if (isEnhanced()) {
					// If swung with full charge
					if (mPlayer.getAttackCooldown() == 1.0) {
						mEnhancementHits += 1;
					}

					if (mEnhancementHits == 3) {
						event.setDamage(event.getDamage() * (1 + ENHANCEMENT_DAMAGE_PERCENT));
						mPlugin.mTimers.updateCooldowns(mPlayer, ENHANCEMENT_CDR);
						mEnhancementHits = 0;
					}
				}
			} else {
				mLastTarget = enemy;

				if (isEnhanced()) {
					mEnhancementHits = 0;

					// If swung with full charge
					if (mPlayer.getAttackCooldown() == 1.0) {
						mEnhancementHits += 1;
					}
				}
			}
		}
		return false; // only changes event damage
	}

}
