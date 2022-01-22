package com.playmonumenta.plugins.bosses.bosses;

import com.playmonumenta.plugins.events.DamageEvent;
import com.playmonumenta.plugins.events.DamageEvent.DamageType;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;

public class VolatileBoss extends BossAbilityGroup {
	public static final String identityTag = "boss_volatile";
	public static final int detectionRange = 20;

	private final Creeper mBoss;

	public static BossAbilityGroup deserialize(Plugin plugin, LivingEntity boss) throws Exception {
		if (!(boss instanceof Creeper)) {
			throw new Exception("Attempted to give non-creeper the " + identityTag + " ability: " + boss.toString());
		}
		return new VolatileBoss(plugin, boss);
	}

	public VolatileBoss(Plugin plugin, LivingEntity boss) throws Exception {
		super(plugin, identityTag, boss);
		if (!(boss instanceof Creeper)) {
			throw new Exception("Attempted to give non-creeper the " + identityTag + " ability: " + boss.toString());
		}

		mBoss = (Creeper)boss;

		// Boss effectively does nothing
		super.constructBoss(null, null, detectionRange, null);
	}

	@Override
	public void onHurt(DamageEvent event) {
		if (event.getType() == DamageType.BLAST) {
			mBoss.explode();
		}
	}
}
