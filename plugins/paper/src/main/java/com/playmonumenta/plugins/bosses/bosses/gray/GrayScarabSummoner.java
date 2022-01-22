package com.playmonumenta.plugins.bosses.bosses.gray;

import com.playmonumenta.plugins.bosses.bosses.BossAbilityGroup;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;

public class GrayScarabSummoner extends GraySwarmSummonerBase {
	public static final String identityTag = "boss_gray_summ_bug";
	public static final int detectionRange = 35;
	private static final String name = "Scarab";

	public static BossAbilityGroup deserialize(Plugin plugin, LivingEntity boss) throws Exception {
		return new GrayScarabSummoner(plugin, boss);
	}

	public GrayScarabSummoner(Plugin plugin, LivingEntity boss) throws Exception {
		super(plugin, boss, identityTag, detectionRange, name);
	}
}
