package com.playmonumenta.plugins.abilities.alchemist;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import javax.annotation.Nullable;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.abilities.AbilityManager;
import com.playmonumenta.plugins.abilities.alchemist.apothecary.WardingRemedy;
import com.playmonumenta.plugins.abilities.alchemist.harbinger.Taboo;
import com.playmonumenta.plugins.utils.EntityUtils;
import com.playmonumenta.plugins.utils.ItemUtils;

public class GruesomeAlchemy extends PotionAbility {
	private static final int GRUESOME_ALCHEMY_DURATION = 8 * 20;
	private static final double GRUESOME_ALCHEMY_1_SLOWNESS_AMPLIFIER = 0.1;
	private static final double GRUESOME_ALCHEMY_2_SLOWNESS_AMPLIFIER = 0.2;
	private static final double GRUESOME_ALCHEMY_1_VULNERABILITY_AMPLIFIER = 0.1;
	private static final double GRUESOME_ALCHEMY_2_VULNERABILITY_AMPLIFIER = 0.2;
	private static final double GRUESOME_ALCHEMY_WEAKEN_AMPLIFIER = 0.1;

	public static final double GRUESOME_POTION_DAMAGE_MULTIPLIER = 0.6;

	private final double mSlownessAmount;
	private final double mVulnerabilityAmount;

	private @Nullable AlchemistPotions mAlchemistPotions;
	private @Nullable Taboo mTaboo;
	private @Nullable WardingRemedy mWardingRemedy;

	public GruesomeAlchemy(Plugin plugin, @Nullable Player player) {
		super(plugin, player, "Gruesome Alchemy", 0, 0);
		mInfo.mScoreboardId = "GruesomeAlchemy";
		mInfo.mShorthandName = "GA";
		mInfo.mDescriptions.add("Swap hands while holding an Alchemist's Bag to switch to Gruesome potions. These potions deal 60% of the damage of your Brutal potions and do not afflict damage over time. Instead, they apply 10% Slow, 10% Vulnerability, and 10% Weaken.");
		mInfo.mDescriptions.add("The Slow and Vulnerability are increased to 20%.");

		mSlownessAmount = getAbilityScore() == 1 ? GRUESOME_ALCHEMY_1_SLOWNESS_AMPLIFIER : GRUESOME_ALCHEMY_2_SLOWNESS_AMPLIFIER;
		mVulnerabilityAmount = getAbilityScore() == 1 ? GRUESOME_ALCHEMY_1_VULNERABILITY_AMPLIFIER : GRUESOME_ALCHEMY_2_VULNERABILITY_AMPLIFIER;
		mDisplayItem = new ItemStack(Material.SKELETON_SKULL, 1);

		Bukkit.getScheduler().runTask(Plugin.getInstance(), () -> {
			mAlchemistPotions = AbilityManager.getManager().getPlayerAbilityIgnoringSilence(player, AlchemistPotions.class);
			mTaboo = AbilityManager.getManager().getPlayerAbilityIgnoringSilence(player, Taboo.class);
			mWardingRemedy = AbilityManager.getManager().getPlayerAbilityIgnoringSilence(player, WardingRemedy.class);
		});
	}

	@Override
	public void apply(LivingEntity mob, ThrownPotion potion, boolean isGruesome) {
		if (isGruesome) {
			EntityUtils.applySlow(mPlugin, GRUESOME_ALCHEMY_DURATION, mSlownessAmount, mob);
			EntityUtils.applyVulnerability(mPlugin, GRUESOME_ALCHEMY_DURATION, mVulnerabilityAmount, mob);
			EntityUtils.applyWeaken(mPlugin, GRUESOME_ALCHEMY_DURATION, GRUESOME_ALCHEMY_WEAKEN_AMPLIFIER, mob);
		}
	}

	@Override
	public void playerSwapHandItemsEvent(PlayerSwapHandItemsEvent event) {
		event.setCancelled(true);

		if (mPlayer == null || (mPlayer.isSneaking() && (mTaboo != null || mWardingRemedy != null))) {
			return;
		}

		if (ItemUtils.isAlchemistItem(mPlayer.getInventory().getItemInMainHand()) && mAlchemistPotions != null) {
			mAlchemistPotions.swapMode();
		}
	}
}
