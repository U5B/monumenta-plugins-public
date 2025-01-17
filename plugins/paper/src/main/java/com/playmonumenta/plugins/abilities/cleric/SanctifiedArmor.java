package com.playmonumenta.plugins.abilities.cleric;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.abilities.Ability;
import com.playmonumenta.plugins.abilities.AbilityInfo;
import com.playmonumenta.plugins.classes.ClassAbility;
import com.playmonumenta.plugins.cosmetics.skills.CosmeticSkills;
import com.playmonumenta.plugins.cosmetics.skills.cleric.SanctifiedArmorCS;
import com.playmonumenta.plugins.effects.SanctifiedArmorHeal;
import com.playmonumenta.plugins.events.DamageEvent;
import com.playmonumenta.plugins.events.DamageEvent.DamageType;
import com.playmonumenta.plugins.itemstats.abilities.CharmManager;
import com.playmonumenta.plugins.server.properties.ServerProperties;
import com.playmonumenta.plugins.utils.DamageUtils;
import com.playmonumenta.plugins.utils.EntityUtils;
import com.playmonumenta.plugins.utils.MovementUtils;
import com.playmonumenta.plugins.utils.PlayerUtils;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;


public class SanctifiedArmor extends Ability {

	private static final double MAX_PERCENT_DAMAGE_1 = 0.15;
	private static final double MAX_PERCENT_DAMAGE_2 = 0.25;
	private static final int DAMAGE_COOLDOWN_1 = 3 * 20;
	private static final int DAMAGE_COOLDOWN_2 = 2 * 20;
	private static final double SLOWNESS_AMPLIFIER_2 = 0.2;
	private static final int SLOWNESS_DURATION = 20 * 3;
	private static final float KNOCKBACK_SPEED = 0.4f;
	private static final String ENHANCEMENT_EFFECT_NAME = "SanctifiedArmorHealEffect";

	private static final double L1_DAMAGE_CAP_R1 = 30;
	private static final double L1_DAMAGE_CAP_R2 = 60;
	private static final double L1_DAMAGE_CAP_R3 = 120;

	private static final double L2_DAMAGE_CAP_R1 = 50;
	private static final double L2_DAMAGE_CAP_R2 = 100;
	private static final double L2_DAMAGE_CAP_R3 = 200;

	public static final String CHARM_DAMAGE = "Sanctified Armor Damage";
	public static final String CHARM_SLOW = "Sanctified Armor Slowness Amplifier";
	public static final String CHARM_DURATION = "Sanctified Armor Slowness Duration";

	public static final AbilityInfo<SanctifiedArmor> INFO =
		new AbilityInfo<>(SanctifiedArmor.class, "Sanctified Armor", SanctifiedArmor::new)
			.linkedSpell(ClassAbility.SANCTIFIED_ARMOR)
			.scoreboardId("Sanctified")
			.shorthandName("Sa")
			.descriptions(
				"Whenever you are damaged by melee or projectile hits from an undead enemy, deals up to a maximum of 15% of the undead's max health." +
					" For every 1% health you are missing, Sanctified Armor deals 1% less max health damage." +
					" Damage is capped based on current region (R1 30/R2 60/R3 120 damage)." +
					" Cooldown: 3s",
				"Deals up to 25% of the undead's max health as true damage (Capped at R1 50/R2 100/R3 200 damage) instead," +
					" and the undead enemy is also afflicted with 20% Slowness for 3 seconds." +
					" Cooldown: 2s",
				"If the most recently affected mob is killed by any means other than Sanctified Armor or Thorns damage, regain half the health lost from the last damage taken.")
			.cooldown(DAMAGE_COOLDOWN_1, DAMAGE_COOLDOWN_2)
			.displayItem(new ItemStack(Material.IRON_CHESTPLATE, 1))
			.priorityAmount(5000); // after all damage modifiers, but before lifelines, to get the proper final damage

	private final double mMaxPercentDamage;
	private final double mDamageCap;

	private @Nullable UUID mLastAffectedMob = null;
	private double mLastDamage;

	private final SanctifiedArmorCS mCosmetic;

	private @Nullable Crusade mCrusade;

	public SanctifiedArmor(Plugin plugin, Player player) {
		super(plugin, player, INFO);
		mMaxPercentDamage = isLevelOne() ? MAX_PERCENT_DAMAGE_1 : MAX_PERCENT_DAMAGE_2;
		if (isLevelOne()) {
			mDamageCap = ServerProperties.getAbilityEnhancementsEnabled(player) ? L1_DAMAGE_CAP_R3 : (ServerProperties.getClassSpecializationsEnabled(player) ? L1_DAMAGE_CAP_R2 : L1_DAMAGE_CAP_R1);
		} else {
			mDamageCap = ServerProperties.getAbilityEnhancementsEnabled(player) ? L2_DAMAGE_CAP_R3 : (ServerProperties.getClassSpecializationsEnabled(player) ? L2_DAMAGE_CAP_R2 : L2_DAMAGE_CAP_R1);
		}

		mCosmetic = CosmeticSkills.getPlayerCosmeticSkill(player, new SanctifiedArmorCS());

		Bukkit.getScheduler().runTask(plugin, () -> mCrusade = plugin.mAbilityManager.getPlayerAbilityIgnoringSilence(player, Crusade.class));
	}

	@Override
	public void onHurt(DamageEvent event, @Nullable Entity damager, @Nullable LivingEntity source) {
		if (source != null
			&& (event.getType() == DamageType.MELEE || event.getType() == DamageType.PROJECTILE)
			&& (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK || damager instanceof Projectile)
			&& Crusade.enemyTriggersAbilities(source, mCrusade)) {
			Location loc = source.getLocation();
			World world = mPlayer.getWorld();

			MovementUtils.knockAway(mPlayer, source, KNOCKBACK_SPEED, KNOCKBACK_SPEED, true);
			if (isLevelTwo()) {
				EntityUtils.applySlow(mPlugin, CharmManager.getDuration(mPlayer, CHARM_DURATION, SLOWNESS_DURATION), SLOWNESS_AMPLIFIER_2 + CharmManager.getLevelPercentDecimal(mPlayer, CHARM_SLOW), source);
				mCosmetic.sanctOnTrigger2(world, mPlayer, loc, source);
			} else {
				mCosmetic.sanctOnTrigger1(world, mPlayer, loc, source);
			}

			if (!event.isBlocked()) {
				processDamage(event, source);

				// Update this separately as this is based on any last damage taken. Cooldowns does make this skill a bit weirder.
				if (isEnhanced() && source.isValid()) {
					mLastDamage = event.getFinalDamage(false);
				}
			}
		}
	}

	public void onMobKilled(LivingEntity entity) {
		if (isEnhanced()
			&& mLastAffectedMob != null
			&& mLastAffectedMob.equals(entity.getUniqueId())
			&& entity.getLastDamageCause() != null
			&& entity.getLastDamageCause().getCause() != EntityDamageEvent.DamageCause.THORNS) {
			PlayerUtils.healPlayer(mPlugin, mPlayer, mLastDamage / 2.0);
			mCosmetic.sanctOnHeal(mPlayer, entity);
		}
	}

	public void processDamage(DamageEvent event, LivingEntity entity) {
		if (isOnCooldown()) {
			return;
		}

		double maxHealth = EntityUtils.getMaxHealth(entity);
		double playerMissingHPPercent = (EntityUtils.getMaxHealth(mPlayer) - mPlayer.getHealth()) / EntityUtils.getMaxHealth(mPlayer);
		double percentDamage = Math.max(0, mMaxPercentDamage - playerMissingHPPercent);
		double charmDamage = CharmManager.calculateFlatAndPercentValue(mPlayer, CHARM_DAMAGE, percentDamage * maxHealth);
		double damage = Math.min(charmDamage, mDamageCap);

		if (damage > 0) {
			mLastAffectedMob = null;
			DamageUtils.damage(mPlayer, entity, DamageType.TRUE, damage, mInfo.getLinkedSpell(), true);

			if (isEnhanced() && entity.isValid()) {
				Optional<SanctifiedArmorHeal> existingEffect = mPlugin.mEffectManager.getEffects(entity, SanctifiedArmorHeal.class).stream().findFirst();
				if (existingEffect.isPresent()) {
					existingEffect.get().addPlayer(mPlayer);
				} else {
					mPlugin.mEffectManager.addEffect(entity, ENHANCEMENT_EFFECT_NAME, new SanctifiedArmorHeal(mPlayer.getUniqueId()).displaysTime(false));
				}
				mLastAffectedMob = entity.getUniqueId();
			}

			putOnCooldown();
		}
	}
}
