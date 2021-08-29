package com.playmonumenta.plugins.depths.abilities.steelsage;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.depths.DepthsTree;
import com.playmonumenta.plugins.depths.DepthsUtils;
import com.playmonumenta.plugins.depths.abilities.DepthsAbility;
import com.playmonumenta.plugins.depths.abilities.DepthsTrigger;
import com.playmonumenta.plugins.effects.Bleed;

import net.md_5.bungee.api.ChatColor;

public class FocusedCombos extends DepthsAbility {

	public static final String ABILITY_NAME = "Focused Combos";
	public static final double[] DAMAGE = {1.20, 1.25, 1.30, 1.35, 1.40};
	public static final int BLEED_LEVEL = 2;
	public static final int BLEED_DURATION = 20 * 3;

	private int mComboCount = 0;

	public FocusedCombos(Plugin plugin, Player player) {
		super(plugin, player, ABILITY_NAME);
		mDisplayItem = Material.SPECTRAL_ARROW;
		mTree = DepthsTree.METALLIC;
	}

	@Override
	public boolean livingEntityShotByPlayerEvent(Projectile proj, LivingEntity le, EntityDamageByEntityEvent event) {
		if (proj instanceof AbstractArrow && ((AbstractArrow) proj).isCritical() && !proj.hasMetadata(RapidFire.META_DATA_TAG)) {
			mComboCount++;

			if (mComboCount >= 3) {
				mComboCount = 0;
				mPlugin.mEffectManager.addEffect(le, ABILITY_NAME, new Bleed(BLEED_DURATION, BLEED_LEVEL, mPlugin));
				event.setDamage(event.getDamage() * DAMAGE[mRarity - 1]);

				Location playerLoc = mPlayer.getLocation();
				mPlayer.playSound(playerLoc, Sound.BLOCK_WEEPING_VINES_BREAK, 2, 0.8f);
				mPlayer.playSound(playerLoc, Sound.BLOCK_ANVIL_PLACE, 0.4f, 1.75f);
			}
		}

		return true;
	}

	@Override
	public String getDescription(int rarity) {
		return "Every third critical arrow shot deals " + DepthsUtils.getRarityColor(rarity) + DAMAGE[rarity - 1] + ChatColor.WHITE + " times damage and applies Bleed " + BLEED_LEVEL + " for " + BLEED_DURATION / 20 + " seconds.";
	}

	@Override
	public DepthsTree getDepthsTree() {
		return DepthsTree.METALLIC;
	}

	@Override
	public DepthsTrigger getTrigger() {
		return DepthsTrigger.COMBO;
	}
}