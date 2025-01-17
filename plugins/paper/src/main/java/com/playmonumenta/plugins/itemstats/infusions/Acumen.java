package com.playmonumenta.plugins.itemstats.infusions;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.itemstats.Infusion;
import com.playmonumenta.plugins.utils.ItemStatUtils.InfusionType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerExpChangeEvent;

public class Acumen implements Infusion {

	public static final double ACUMEN_MULTIPLIER = 0.02;

	@Override
	public String getName() {
		return "Acumen";
	}

	@Override
	public InfusionType getInfusionType() {
		return InfusionType.ACUMEN;
	}

	@Override
	public void onExpChange(Plugin plugin, Player player, double value, PlayerExpChangeEvent event) {
		double expBuffPct = ACUMEN_MULTIPLIER * value;
		event.setAmount((int)(event.getAmount() * (1.0 + expBuffPct)));
	}

}
