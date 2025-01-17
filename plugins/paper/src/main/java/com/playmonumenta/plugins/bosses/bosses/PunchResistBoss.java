package com.playmonumenta.plugins.bosses.bosses;

import com.playmonumenta.plugins.bosses.SpellManager;
import com.playmonumenta.plugins.events.DamageEvent;
import java.util.Collections;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class PunchResistBoss extends BossAbilityGroup {
	public static final String identityTag = "boss_punchresist";
	public static final int detectionRange = 100;

	public static BossAbilityGroup deserialize(Plugin plugin, LivingEntity boss) throws Exception {
		return new PunchResistBoss(plugin, boss);
	}

	public PunchResistBoss(Plugin plugin, LivingEntity boss) {
		super(plugin, identityTag, boss);
		super.constructBoss(SpellManager.EMPTY, Collections.emptyList(), detectionRange, null);
	}

	@Override
	public void bossHitByProjectile(ProjectileHitEvent event) {
		mBoss.setVelocity(new Vector(0, 0, 0));
	}

	@Override
	public void onHurtByEntity(DamageEvent event, Entity damager) {
		if (damager instanceof Projectile) {
			new BukkitRunnable() {
				int mTicks = 0;

				@Override
				public void run() {
					mBoss.setVelocity(new Vector(0, 0, 0));
					mTicks += 1;
					if (mTicks > 2) {
						this.cancel();
					}
				}
			}.runTaskTimer(mPlugin, 0, 1);
		}
	}
}
