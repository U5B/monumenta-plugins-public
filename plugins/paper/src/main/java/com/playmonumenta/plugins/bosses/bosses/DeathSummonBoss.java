package com.playmonumenta.plugins.bosses.bosses;

import com.playmonumenta.plugins.bosses.SpellManager;
import com.playmonumenta.plugins.bosses.parameters.BossParam;
import com.playmonumenta.plugins.bosses.parameters.LoSPool;
import com.playmonumenta.plugins.bosses.parameters.ParticlesList;
import com.playmonumenta.plugins.bosses.parameters.SoundsList;
import java.util.Collections;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

public class DeathSummonBoss extends BossAbilityGroup {
	public static final String identityTag = "boss_death_summon";

	public static class Parameters extends BossParameters {

		@BossParam(help = "Pool of mobs to summon")
		public LoSPool POOL = LoSPool.EMPTY;

		@BossParam(help = "Particles summon when the mob spawm")
		public ParticlesList PARTICLES = ParticlesList.fromString("[(SOUL_FIRE_FLAME,20,0.7,0.7,0.7,0.2)]");

		@BossParam(help = "Sounds summon when the mob spawm")
		public SoundsList SOUNDS = SoundsList.fromString("[(BLOCK_SOUL_SAND_FALL,2,0.5)]");

		@BossParam(help = "Delay for the mob spawned to get AI activated")
		public int MOB_AI_DELAY = 10;

		@BossParam(help = "if the mob spawned will have the same agro as the mob dead")
		public boolean AUTO_AGGRO = true;

		@BossParam(help = "Number of mobs summoned")
		public int MOB_COUNT = 1;

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
	public void death(@Nullable EntityDeathEvent event) {
		Location loc = mBoss.getLocation();
		mParam.PARTICLES.spawn(mBoss, loc.clone().add(0, 0.5, 0));
		mParam.SOUNDS.play(loc);
		for (int i = 0; i < mParam.MOB_COUNT; i++) {
			Entity entity = mParam.POOL.spawn(loc);
			if (entity instanceof LivingEntity livingEntity) {
				livingEntity.setRotation(loc.getYaw(), loc.getPitch());
				livingEntity.setAI(false);
				Bukkit.getScheduler().runTaskLater(mPlugin, () -> {
					livingEntity.setAI(true);
					if (mParam.AUTO_AGGRO && entity instanceof Mob newMob && mBoss instanceof Mob oldMob) {
						newMob.setTarget(oldMob.getTarget());
					}
				}, mParam.MOB_AI_DELAY);
			}
		}
	}
}
