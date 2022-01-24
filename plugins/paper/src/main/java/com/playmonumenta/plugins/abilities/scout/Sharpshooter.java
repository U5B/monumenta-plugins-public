package com.playmonumenta.plugins.abilities.scout;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.abilities.Ability;
import com.playmonumenta.plugins.abilities.AbilityManager;
import com.playmonumenta.plugins.abilities.AbilityWithChargesOrStacks;
import com.playmonumenta.plugins.events.DamageEvent;
import com.playmonumenta.plugins.events.DamageEvent.DamageType;
import com.playmonumenta.plugins.network.ClientModHandler;
import com.playmonumenta.plugins.utils.MessagingUtils;
import org.bukkit.Material;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.inventory.ItemStack;
import javax.annotation.Nullable;

public class Sharpshooter extends Ability implements AbilityWithChargesOrStacks {
	private static final double PERCENT_BASE_DAMAGE = 0.2;
	private static final int SHARPSHOOTER_DECAY_TIMER = 20 * 4;
	private static final int MAX_STACKS = 8;
	private static final double PERCENT_DAMAGE_PER_STACK = 0.03;

	public Sharpshooter(Plugin plugin, @Nullable Player player) {
		super(plugin, player, "Sharpshooter");
		mInfo.mScoreboardId = "Sharpshooter";
		mInfo.mShorthandName = "Ss";
		mInfo.mDescriptions.add("Your arrows deal 20% more damage.");
		mInfo.mDescriptions.add("Each enemy hit with a critical arrow or trident gives you a stack of Sharpshooter, up to 8. Stacks decay after 4 seconds of not gaining a stack. Each stack makes your arrows and tridents deal +3% damage.");
		mInfo.mIgnoreTriggerCap = true;
		mDisplayItem = new ItemStack(Material.TARGET, 1);
	}

	private int mStacks = 0;
	private int mTicksToStackDecay = 0;

	@Override
	public void onDamage(DamageEvent event, LivingEntity enemy) {
		if (event.getType() == DamageType.PROJECTILE && event.getDamager() instanceof AbstractArrow arrow) {

			event.setDamage(event.getDamage() * (1 + PERCENT_BASE_DAMAGE + mStacks * PERCENT_DAMAGE_PER_STACK));

			// Critical arrow and mob is actually going to take damage
			if (getAbilityScore() > 1 && (arrow.isCritical() || arrow instanceof Trident) && (enemy.getNoDamageTicks() <= 10 || enemy.getLastDamage() < event.getDamage())) {
				mTicksToStackDecay = SHARPSHOOTER_DECAY_TIMER;

				if (mStacks < MAX_STACKS) {
					mStacks++;
					MessagingUtils.sendActionBarMessage(mPlugin, mPlayer, "Sharpshooter Stacks: " + mStacks);
					ClientModHandler.updateAbility(mPlayer, this);
				}
			}
		}
	}

	@Override
	public void periodicTrigger(boolean twoHertz, boolean oneSecond, int ticks) {
		if (mPlayer == null) {
			return;
		}
		if (mStacks > 0) {
			mTicksToStackDecay -= 5;

			if (mTicksToStackDecay <= 0) {
				mTicksToStackDecay = SHARPSHOOTER_DECAY_TIMER;
				mStacks--;
				MessagingUtils.sendActionBarMessage(mPlugin, mPlayer, "Sharpshooter Stacks: " + mStacks);
				ClientModHandler.updateAbility(mPlayer, this);
			}
		}
	}

	public static void addStacks(Plugin plugin, Player player, int stacks) {
		Sharpshooter ss = AbilityManager.getManager().getPlayerAbility(player, Sharpshooter.class);
		if (ss != null) {
			ss.mStacks = Math.min(MAX_STACKS, ss.mStacks + stacks);
			MessagingUtils.sendActionBarMessage(plugin, player, "Sharpshooter Stacks: " + ss.mStacks);
			ClientModHandler.updateAbility(player, ss);
		}
	}

	public static double getDamageMultiplier(Player player) {
		Sharpshooter ss = AbilityManager.getManager().getPlayerAbility(player, Sharpshooter.class);
		return ss == null ? 1 : (1 + PERCENT_BASE_DAMAGE + ss.mStacks * PERCENT_DAMAGE_PER_STACK);
	}

	@Override
	public int getCharges() {
		return mStacks;
	}

	@Override
	public int getMaxCharges() {
		return MAX_STACKS;
	}

}
