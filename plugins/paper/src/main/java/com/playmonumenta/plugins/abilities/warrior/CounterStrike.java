package com.playmonumenta.plugins.abilities.warrior;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.abilities.Ability;
import com.playmonumenta.plugins.classes.ClassAbility;
import com.playmonumenta.plugins.events.DamageEvent;
import com.playmonumenta.plugins.events.DamageEvent.DamageType;
import com.playmonumenta.plugins.particle.PartialParticle;
import com.playmonumenta.plugins.utils.DamageUtils;
import com.playmonumenta.plugins.utils.EntityUtils;
import java.util.List;
import javax.annotation.Nullable;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CounterStrike extends Ability {

	private static final double COUNTER_STRIKE_1_REFLECT = 0.2;
	private static final double COUNTER_STRIKE_2_REFLECT = 0.4;
	private static final float COUNTER_STRIKE_RADIUS = 3.0f;

	private final double mReflect;

	public CounterStrike(Plugin plugin, @Nullable Player player) {
		super(plugin, player, "Counter Strike");
		mInfo.mScoreboardId = "CounterStrike";
		mInfo.mShorthandName = "CS";
		mInfo.mDescriptions.add("When you take melee damage, deal melee damage equal to 20% of pre-mitigation damage taken to all mobs in a 3 block radius.");
		mInfo.mDescriptions.add("The damage is increased to 40% of pre-mitigation damage.");
		mInfo.mLinkedSpell = ClassAbility.COUNTER_STRIKE;
		mDisplayItem = new ItemStack(Material.CACTUS, 1);
		mReflect = getAbilityScore() == 1 ? COUNTER_STRIKE_1_REFLECT : COUNTER_STRIKE_2_REFLECT;
	}

	@Override
	public void onHurt(DamageEvent event, @Nullable Entity damager, @Nullable LivingEntity source) {
		if (event.getType() == DamageType.MELEE
			    && source != null
			    && !event.isBlocked()
			    && mPlayer != null
			    && mPlayer.getNoDamageTicks() <= mPlayer.getMaximumNoDamageTicks() / 2f) {

			Location loc = mPlayer.getLocation().add(0, 1, 0);
			new PartialParticle(Particle.SWEEP_ATTACK, loc, 6, 0.75, 0.5, 0.75, 0.001).spawnAsPlayerActive(mPlayer);
			new PartialParticle(Particle.FIREWORKS_SPARK, loc, 8, 0.75, 0.5, 0.75, 0.1).spawnAsPlayerActive(mPlayer);
			mPlayer.playSound(mPlayer.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 0.6f, 0.7f);
			double eventDamage = event.getOriginalDamage() * mReflect;
			List<LivingEntity> entityList = EntityUtils.getNearbyMobs(mPlayer.getLocation(), COUNTER_STRIKE_RADIUS, mPlayer);

			if (entityList.remove(damager)) {
				DamageUtils.damage(mPlayer, (LivingEntity)damager, DamageType.MELEE_SKILL, eventDamage, mInfo.mLinkedSpell, true, true);
			}

			for (LivingEntity mob : entityList) {
				DamageUtils.damage(mPlayer, mob, DamageType.WARRIOR_AOE, eventDamage, mInfo.mLinkedSpell, true, true);
			}
		}
	}
}
