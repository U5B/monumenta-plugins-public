package com.playmonumenta.plugins.itemstats.enchantments;

import com.playmonumenta.plugins.events.DamageEvent.DamageType;
import com.playmonumenta.plugins.utils.ItemStatUtils.EnchantmentType;

public class MeleeProtection extends Protection {

	@Override
	public String getName() {
		return "Melee Protection";
	}

	@Override
	public EnchantmentType getEnchantmentType() {
		return EnchantmentType.MELEE_PROTECTION;
	}

	@Override
	protected DamageType getType() {
		return DamageType.MELEE;
	}

	@Override
	protected int getEPF() {
		return 2;
	}

}
