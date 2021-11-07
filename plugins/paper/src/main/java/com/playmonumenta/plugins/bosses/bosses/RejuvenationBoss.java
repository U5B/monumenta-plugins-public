package com.playmonumenta.plugins.bosses.bosses;

import java.util.Arrays;

import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;

import com.playmonumenta.plugins.bosses.SpellManager;
import com.playmonumenta.plugins.bosses.parameters.BossParam;
import com.playmonumenta.plugins.bosses.spells.SpellMobHealAoE;
import com.playmonumenta.plugins.utils.BossUtils;

public class RejuvenationBoss extends BossAbilityGroup {
	public static final String identityTag = "boss_rejuvenation";

	public static class Parameters extends BossParameters {
		@BossParam(help = "not written")
		public int HEAL = 25;
		@BossParam(help = "not written")
		public int RANGE = 14;
		@BossParam(help = "not written")
		public int DURATION = 80;
		@BossParam(help = "not written")
		public int DETECTION = 20;
		@BossParam(help = "not written")
		public int DELAY = 5 * 20;
		@BossParam(help = "not written")
		public int COOLDOWN = 15 * 20;
	}

	public static BossAbilityGroup deserialize(Plugin plugin, LivingEntity boss) throws Exception {
		return new RejuvenationBoss(plugin, boss);
	}

	public RejuvenationBoss(Plugin plugin, LivingEntity boss) {
		super(plugin, identityTag, boss);

		Parameters p = BossParameters.getParameters(boss, identityTag, new Parameters());

		SpellManager activeSpells = new SpellManager(Arrays.asList(
			new SpellMobHealAoE(plugin, boss, p.HEAL, p.RANGE, p.DURATION, p.COOLDOWN)
		                                             ));

		super.constructBoss(activeSpells, null, p.DETECTION, null, p.DELAY);
	}
}
