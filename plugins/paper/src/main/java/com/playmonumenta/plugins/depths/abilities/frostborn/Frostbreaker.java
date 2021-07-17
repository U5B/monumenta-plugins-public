package com.playmonumenta.plugins.depths.abilities.frostborn;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffectType;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.depths.DepthsTree;
import com.playmonumenta.plugins.depths.DepthsUtils;
import com.playmonumenta.plugins.depths.abilities.DepthsAbility;
import com.playmonumenta.plugins.enchantments.Inferno;
import com.playmonumenta.plugins.utils.EntityUtils;
import com.playmonumenta.plugins.utils.PotionUtils;

import net.md_5.bungee.api.ChatColor;

public class Frostbreaker extends DepthsAbility {

	public static final String ABILITY_NAME = "Icebreaker";
	public static final double[] DAMAGE = {1.24, 1.28, 1.32, 1.36, 1.40};

	public Frostbreaker(Plugin plugin, Player player) {
		super(plugin, player, ABILITY_NAME);
		mDisplayItem = Material.TUBE_CORAL_FAN;
		mTree = DepthsTree.FROSTBORN;
	}

	@Override
	public boolean livingEntityDamagedByPlayerEvent(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof LivingEntity) {
			return handleEvent((LivingEntity) event.getEntity(), event);
		}
		return true;
	}

	@Override
	public boolean livingEntityShotByPlayerEvent(Projectile proj, LivingEntity damagee, EntityDamageByEntityEvent event) {
		return handleEvent(damagee, event);
	}

	public boolean handleEvent(LivingEntity entity, EntityDamageByEntityEvent event) {
		List<PotionEffectType> e = PotionUtils.getNegativeEffects(mPlugin, entity);
		if (e.size() > 0 || EntityUtils.isStunned(entity) || EntityUtils.isConfused(entity) || EntityUtils.isBleeding(mPlugin, entity)
				|| EntityUtils.isSlowed(mPlugin, entity) || EntityUtils.isWeakened(mPlugin, entity) || EntityUtils.isSilenced(entity) || EntityUtils.vulnerabilityMult(entity) > 1
				|| entity.getFireTicks() > 0 || Inferno.getMobInfernoLevel(mPlugin, entity) > 0 || isOnIce(entity)) {
			event.setDamage(DAMAGE[mRarity - 1] * event.getDamage());
		}
		return true;
	}

	public boolean isOnIce(LivingEntity entity) {
		if (entity.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == DepthsUtils.ICE_MATERIAL) {
			return true;
		}
		return false;
	}

	@Override
	public String getDescription(int rarity) {
		return "Damage you deal to mobs that are on ice or debuffed is multiplied by " + DepthsUtils.getRarityColor(rarity) + DAMAGE[rarity - 1] + ChatColor.WHITE + ".";
	}

	@Override
	public DepthsTree getDepthsTree() {
		return DepthsTree.FROSTBORN;
	}
}
