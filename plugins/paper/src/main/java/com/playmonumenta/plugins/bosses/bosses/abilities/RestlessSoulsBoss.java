package com.playmonumenta.plugins.bosses.bosses.abilities;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;

import com.playmonumenta.plugins.abilities.Ability;
import com.playmonumenta.plugins.abilities.AbilityManager;
import com.playmonumenta.plugins.abilities.warlock.CholericFlames;
import com.playmonumenta.plugins.abilities.warlock.GraspingClaws;
import com.playmonumenta.plugins.abilities.warlock.MelancholicLament;
import com.playmonumenta.plugins.abilities.warlock.tenebrist.HauntingShades;
import com.playmonumenta.plugins.abilities.warlock.tenebrist.WitheringGaze;
import com.playmonumenta.plugins.bosses.SpellManager;
import com.playmonumenta.plugins.bosses.bosses.BossAbilityGroup;
import com.playmonumenta.plugins.classes.ClassAbility;
import com.playmonumenta.plugins.effects.CustomDamageOverTime;
import com.playmonumenta.plugins.events.DamageEvent;
import com.playmonumenta.plugins.events.DamageEvent.DamageType;
import com.playmonumenta.plugins.utils.AbilityUtils;
import com.playmonumenta.plugins.utils.DamageUtils;
import com.playmonumenta.plugins.utils.EntityUtils;
import com.playmonumenta.plugins.utils.PotionUtils;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import javax.annotation.Nullable;

public class RestlessSoulsBoss extends BossAbilityGroup {
	public static final String identityTag = "boss_restlesssouls";
	public static final int detectionRange = 64;

	private final com.playmonumenta.plugins.Plugin mMonPlugin = com.playmonumenta.plugins.Plugin.getInstance();
	private @Nullable Player mPlayer;
	private double mDamage = 0;
	private int mSilenceTime = 0;
	private int mDuration = 0;
	private boolean mLevelOne;
	private FixedMetadataValue mPlayerItemStats;

	private Ability[] mAbilities = {};
	private static final String DOT_EFFECT_NAME = "RestlessSoulsDamageOverTimeEffect";

	public static BossAbilityGroup deserialize(Plugin plugin, LivingEntity boss) throws Exception {
		return new RestlessSoulsBoss(plugin, boss);
	}

	public RestlessSoulsBoss(Plugin plugin, LivingEntity boss) throws Exception {
		super(plugin, identityTag, boss);
		boss.setInvulnerable(true);
		super.constructBoss(SpellManager.EMPTY, Collections.emptyList(), detectionRange, null);
	}

	public void spawn(Player player, double damage, int silenceTime, int duration, boolean levelone, FixedMetadataValue playerItemStats) {
		mPlayer = player;
		mDamage = damage;
		mSilenceTime = silenceTime;
		mDuration = duration;
		mLevelOne = levelone;
		mPlayerItemStats = playerItemStats;

		if (player != null) {
			Bukkit.getScheduler().runTask(mMonPlugin, () -> {
				mAbilities = Stream.of(CholericFlames.class, GraspingClaws.class,
						MelancholicLament.class, HauntingShades.class, WitheringGaze.class)
					.map(c -> AbilityManager.getManager().getPlayerAbilityIgnoringSilence(player, c)).toArray(Ability[]::new);
			});
		}
	}

	@Override
	public void onDamage(DamageEvent event, LivingEntity damagee) {
		if (mPlayer != null) {
			event.setCancelled(true);
			mBoss.getWorld().playSound(mBoss.getLocation(), Sound.BLOCK_END_PORTAL_FRAME_FILL, 1.5f, 1.0f);

			// tag mob to prevent it from spawning more stuff
			damagee.addScoreboardTag("TeneGhost");

			DamageEvent damageEvent = new DamageEvent(damagee, mPlayer, mPlayer, DamageType.MAGIC, ClassAbility.RESTLESS_SOULS, mDamage);
			damageEvent.setDelayed(true);
			damageEvent.setPlayerItemStat(mPlayerItemStats);
			DamageUtils.damage(damageEvent, true, true, null);

			// remove tag if mob is not dead
			if (!damagee.isDead()) {
				damagee.removeScoreboardTag("TeneGhost");
			}
			// debuff
			if (!EntityUtils.isBoss(damagee)) {
				EntityUtils.applySilence(mMonPlugin, mSilenceTime, damagee);
			}
			if (!mLevelOne) {
				for (Ability ability : mAbilities) {
					if (ability != null && mMonPlugin.mTimers.isAbilityOnCooldown(mPlayer.getUniqueId(), ability.getInfo().mLinkedSpell)) {
						if (ability.getInfo().mLinkedSpell == ClassAbility.CHOLERIC_FLAMES) {
							damagee.setFireTicks(mDuration);
							if (ability.getAbilityScore() == 2) {
								PotionUtils.applyPotion(mPlayer, damagee, new PotionEffect(PotionEffectType.HUNGER, mDuration, 0, false, true));
							}
						} else if (ability.getInfo().mLinkedSpell == ClassAbility.GRASPING_CLAWS) {
							EntityUtils.applySlow(mMonPlugin, mDuration, 0.1, damagee);
						} else if (ability.getInfo().mLinkedSpell == ClassAbility.MELANCHOLIC_LAMENT) {
							EntityUtils.applyWeaken(mMonPlugin, mDuration, 0.1, damagee);
						} else if (ability.getInfo().mLinkedSpell == ClassAbility.HAUNTING_SHADES) {
							PotionUtils.applyPotion(mPlayer, damagee, new PotionEffect(PotionEffectType.UNLUCK, mDuration, 0, true, false));
						} else if (ability.getInfo().mLinkedSpell == ClassAbility.WITHERING_GAZE) {
							mMonPlugin.mEffectManager.addEffect(damagee, DOT_EFFECT_NAME, new CustomDamageOverTime(mDuration, 1, 40, mPlayer, null, Particle.SQUID_INK));
						}
					}
				}
			}
			// kill vex
			mBoss.remove();
		}
	}

	@Override
	public void bossChangedTarget(EntityTargetEvent event) {
		Entity target = event.getTarget();
		Set<String> tags = target.getScoreboardTags();
		if (!EntityUtils.isHostileMob(target) || (tags != null && tags.contains(AbilityUtils.IGNORE_TAG))) {
			event.setCancelled(true);
		}
	}
}