package com.playmonumenta.plugins.abilities.other;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.abilities.Ability;
import com.playmonumenta.plugins.utils.EntityUtils;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.Nullable;

public class AttribaAttackDamage extends Ability {
	public static final double REDUCTION = -0.75;
	public static final String MODIFIER_NAME = "AttribaAttackDamage";

	@Override
	public boolean canUse(Player player) {
		return player != null && player.getScoreboardTags().contains(MODIFIER_NAME);
	}

	public AttribaAttackDamage(Plugin plugin, @Nullable Player player) {
		super(plugin, player, "AttribaAttackDamage");

		if (player != null) {
			EntityUtils.addAttribute(player, Attribute.GENERIC_ATTACK_DAMAGE,
			                         new AttributeModifier(MODIFIER_NAME, REDUCTION, AttributeModifier.Operation.MULTIPLY_SCALAR_1));
		}
	}
}
