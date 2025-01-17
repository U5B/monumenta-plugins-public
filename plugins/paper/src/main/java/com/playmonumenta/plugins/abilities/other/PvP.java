package com.playmonumenta.plugins.abilities.other;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.abilities.Ability;
import com.playmonumenta.plugins.abilities.AbilityInfo;
import com.playmonumenta.plugins.particle.PartialParticle;
import com.playmonumenta.plugins.utils.PotionUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

/*
 * This is a utility "ability" that makes it easy to see whether a player has PvP enabled or not
 */
public class PvP extends Ability {

	public static final AbilityInfo<PvP> INFO =
		new AbilityInfo<>(PvP.class, null, PvP::new)
			.canUse(player -> player.getScoreboardTags().contains("pvp"))
			.ignoresSilence(true);

	public PvP(Plugin plugin, Player player) {
		super(plugin, player, INFO);

		if (player != null) {
			AttributeInstance maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
			if (maxHealth != null) {
				maxHealth.setBaseValue(40);
			}
		}
	}

	@Override
	public void playerDeathEvent(PlayerDeathEvent event) {
		Player player = mPlayer;
		if (player.getKiller() != null) {
			AttributeInstance maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
			if (maxHealth != null) {
				event.setReviveHealth(maxHealth.getValue());
			}
			if (event.getDeathMessage() != null) {
				Bukkit.broadcastMessage(event.getDeathMessage());
			}
			event.setCancelled(true);
			player.setGameMode(GameMode.SPECTATOR);
			World world = mPlayer.getWorld();
			world.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_DEATH, SoundCategory.PLAYERS, 1, 1);
			new PartialParticle(Particle.BLOCK_CRACK, player.getLocation().add(0, 1, 0), 30, 0.15, 0.15, 0.15, 0.75F, Material.REDSTONE_BLOCK.createBlockData()).spawnAsPlayerActive(player);
			player.sendTitle(ChatColor.RED + "Respawning...", ChatColor.GREEN + "In 3 seconds...", 10, 20 * 2, 10);
			PotionUtils.clearNegatives(mPlugin, player);
			@Nullable ItemStack[] inv = player.getInventory().getContents();
			for (ItemStack item : inv) {
				if (item != null && item.getType().getMaxDurability() > 0) {
					ItemMeta meta = item.getItemMeta();
					((Damageable) meta).setDamage(item.getType().getMaxDurability());
					item.setItemMeta(meta);
				}
				if (item != null) {
					world.dropItemNaturally(player.getLocation(), item);
				}
			}
			player.getInventory().clear();
			new BukkitRunnable() {
				@Override
				public void run() {
					player.teleport(world.getSpawnLocation());
					player.setGameMode(GameMode.SURVIVAL);
					player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20 * 3, 10));

				}

			}.runTaskLater(mPlugin, 20 * 3);
		}
	}

}
