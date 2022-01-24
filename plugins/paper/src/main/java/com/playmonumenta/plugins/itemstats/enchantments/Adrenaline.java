package com.playmonumenta.plugins.itemstats.enchantments;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.effects.Effect;
import com.playmonumenta.plugins.effects.PercentSpeed;
import com.playmonumenta.plugins.events.DamageEvent;
import com.playmonumenta.plugins.events.DamageEvent.DamageType;
import com.playmonumenta.plugins.itemstats.Enchantment;
import com.playmonumenta.plugins.utils.ItemStatUtils.EnchantmentType;
import com.playmonumenta.plugins.utils.ItemUtils;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;

import java.util.NavigableSet;

public class Adrenaline implements Enchantment {

	private static final String PERCENT_SPEED_EFFECT_NAME = "AdrenalinePercentSpeedEffect";
	private static final int DURATION = 20 * 3;
	private static final int SPAWNER_DURATION = 20 * 8;
	private static final double PERCENT_SPEED_PER_LEVEL = 0.1;

	private static final Particle.DustOptions RED_COLOR = new Particle.DustOptions(Color.fromRGB(200, 0, 0), 1.0f);

	@Override
	public @NotNull String getName() {
		return "Adrenaline";
	}

	@Override
	public EnchantmentType getEnchantmentType() {
		return EnchantmentType.ADRENALINE;
	}

	@Override
	public void onDamage(@NotNull Plugin plugin, @NotNull Player player, double value, @NotNull DamageEvent event, @NotNull LivingEntity enemy) {
		if (event.getType() == DamageType.MELEE) {
			player.getWorld().spawnParticle(Particle.REDSTONE, player.getLocation().add(0, 1, 0), 12, 0.4, 0.5, 0.4, RED_COLOR);
			NavigableSet<Effect> speedEffects = plugin.mEffectManager.getEffects(player, PERCENT_SPEED_EFFECT_NAME);
			if (speedEffects != null) {
				for (Effect effect : speedEffects) {
					effect.setDuration(DURATION);
				}
			} else {
				double level = plugin.mItemStatManager.getEnchantmentLevel(player, EnchantmentType.ADRENALINE);
				plugin.mEffectManager.addEffect(player, PERCENT_SPEED_EFFECT_NAME, new PercentSpeed(DURATION, PERCENT_SPEED_PER_LEVEL * level, PERCENT_SPEED_EFFECT_NAME));
			}
		}
	}

	@Override
	public void onBlockBreak(Plugin plugin, Player player, double value, BlockBreakEvent event) {
		if (ItemUtils.isPickaxe(player.getItemInHand()) && event.getBlock().getType() == Material.SPAWNER) {
			player.getWorld().spawnParticle(Particle.REDSTONE, player.getLocation().add(0, 1, 0), 12, 0.4, 0.5, 0.4, RED_COLOR);
			NavigableSet<Effect> speedEffects = plugin.mEffectManager.getEffects(player, PERCENT_SPEED_EFFECT_NAME);
			if (speedEffects != null) {
				for (Effect effect : speedEffects) {
					effect.setDuration(SPAWNER_DURATION);
				}
			} else {
				double level = plugin.mItemStatManager.getEnchantmentLevel(player, EnchantmentType.ADRENALINE);
				plugin.mEffectManager.addEffect(player, PERCENT_SPEED_EFFECT_NAME, new PercentSpeed(DURATION, PERCENT_SPEED_PER_LEVEL * level, PERCENT_SPEED_EFFECT_NAME));
			}
		}
	}
}