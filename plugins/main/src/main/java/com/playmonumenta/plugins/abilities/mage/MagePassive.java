package com.playmonumenta.plugins.abilities.mage;

import java.util.Random;

import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.abilities.Ability;
import com.playmonumenta.plugins.utils.EntityUtils;
import com.playmonumenta.plugins.utils.InventoryUtils;
import com.playmonumenta.plugins.utils.MetadataUtils;
import com.playmonumenta.plugins.utils.ScoreboardUtils;

public class MagePassive extends Ability {

	private static final double PASSIVE_DAMAGE = 1.5;

	public MagePassive(Plugin plugin, World world, Random random, Player player) {
		super(plugin, world, random, player);
	}

	@Override
	public boolean canUse(Player player) {
		return ScoreboardUtils.getScoreboardValue(player, "Class") == 1;
	}

	@Override
	public boolean LivingEntityDamagedByPlayerEvent(EntityDamageByEntityEvent event) {
		Entity damagee = event.getEntity();

		ItemStack mainHand = mPlayer.getInventory().getItemInMainHand();
		if (event.getCause() == DamageCause.ENTITY_ATTACK && InventoryUtils.isWandItem(mainHand)
		    && !MetadataUtils.happenedThisTick(mPlugin, mPlayer, EntityUtils.PLAYER_DEALT_CUSTOM_DAMAGE_METAKEY, 0)) {
			event.setDamage(event.getDamage() + PASSIVE_DAMAGE);
		}
		return true;
	}

	@Override
	public boolean PlayerDamagedByLivingEntityEvent(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Player) {
			event.setDamage(event.getDamage() * 1.15);
		}
		return true;
	}
}
