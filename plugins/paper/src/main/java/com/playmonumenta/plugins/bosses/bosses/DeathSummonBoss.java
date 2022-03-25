package com.playmonumenta.plugins.bosses.bosses;

import com.playmonumenta.plugins.bosses.SpellManager;
import com.playmonumenta.plugins.bosses.parameters.BossParam;
import com.playmonumenta.plugins.bosses.parameters.LoSPool;
import com.playmonumenta.plugins.bosses.parameters.ParticlesList;
import com.playmonumenta.plugins.bosses.parameters.SoundsList;
import java.util.Collections;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.Plugin;

public class DeathSummonBoss extends BossAbilityGroup {
	public static final String identityTag = "boss_death_summon";

	public static class Parameters extends BossParameters {

		@BossParam(help = "Pool of mobs to summon")
		public LoSPool POOL = LoSPool.EMPTY;

		@BossParam(help = "Particles summon when the mob spawm")
		public ParticlesList PARTICLES = ParticlesList.fromString("[(SOUL_FIRE_FLAME,20,0.7,0.7,0.7,0.2)]");

		@BossParam(help = "Sounds summon when the mob spawm")
		public SoundsList SOUNDS = SoundsList.fromString("[(BLOCK_SOUL_SAND_FALL,2,0.5)]");

	}

	private final Parameters mParam;

	public DeathSummonBoss(Plugin plugin, LivingEntity boss) {
		super(plugin, identityTag, boss);
		mParam = BossParameters.getParameters(boss, identityTag, new Parameters());

		super.constructBoss(SpellManager.EMPTY, Collections.emptyList(), -1, null);
	}

	public static BossAbilityGroup deserialize(Plugin plugin, LivingEntity boss) throws Exception {
		return new DeathSummonBoss(plugin, boss);
	}

	@Override
	public void death(EntityDeathEvent event) {
		Entity entity = mParam.POOL.spawn(mBoss.getLocation());
		if (entity != null) {
			mParam.PARTICLES.spawn(mBoss.getLocation().clone().add(0, 0.5, 0));
			mParam.SOUNDS.play(mBoss.getLocation());
		}

		if (entity instanceof Mob newMob && mBoss instanceof Mob oldMob) {
			newMob.setTarget(oldMob.getTarget());
		}

	}
}