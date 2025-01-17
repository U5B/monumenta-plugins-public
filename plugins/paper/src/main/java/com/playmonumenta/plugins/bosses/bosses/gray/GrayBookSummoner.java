package com.playmonumenta.plugins.bosses.bosses.gray;

import com.playmonumenta.plugins.bosses.bosses.BossAbilityGroup;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;

public class GrayBookSummoner extends GraySwarmSummonerBase {
	public static final String identityTag = "boss_gray_summ_book";
	public static final int detectionRange = 35;
	private static final String name = "AnimatedText";

	public static BossAbilityGroup deserialize(Plugin plugin, LivingEntity boss) throws Exception {
		return new GrayBookSummoner(plugin, boss);
	}

	public GrayBookSummoner(Plugin plugin, LivingEntity boss) throws Exception {
		super(plugin, boss, identityTag, detectionRange, name);
	}
}
