package com.playmonumenta.plugins.enchantments.curses;

import java.util.EnumSet;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.enchantments.BaseEnchantment;
import com.playmonumenta.plugins.enchantments.EnchantmentManager.ItemSlot;
import com.playmonumenta.plugins.potion.PotionManager.PotionID;
import com.playmonumenta.plugins.utils.ZoneUtils;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class CurseOfCorruption implements BaseEnchantment {
	private static String PROPERTY_NAME = ChatColor.RED + "Curse of Corruption";

	@Override
	public String getProperty() {
		return PROPERTY_NAME;
	}

	@Override
	public boolean isMultiLevel() {
		return false;
	}

	@Override
	public EnumSet<ItemSlot> getValidSlots() {
		return EnumSet.of(ItemSlot.INVENTORY);
	}

	@Override
	public void applyProperty(Plugin plugin, Player player, int level) {
		if (level > 1) {
			if (!ZoneUtils.isInPlot(player)) {
				if (player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR) {
					plugin.mPotionManager.addPotion(player, PotionID.ITEM, new PotionEffect(PotionEffectType.BLINDNESS, 1000000, 0, true, false));
				}
				plugin.mPotionManager.addPotion(player, PotionID.ITEM, new PotionEffect(PotionEffectType.SLOW, 1000000, level - 2, true, false));
			}
		}
	}

	@Override
	public void removeProperty(Plugin plugin, Player player) {
		plugin.mPotionManager.removePotion(player, PotionID.ITEM, PotionEffectType.BLINDNESS);
		plugin.mPotionManager.removePotion(player, PotionID.ITEM, PotionEffectType.SLOW);
	}
}
