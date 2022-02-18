package com.playmonumenta.plugins.bosses.spells;

import com.playmonumenta.plugins.integrations.LibraryOfSoulsIntegration;
import com.playmonumenta.plugins.utils.EntityUtils;
import com.playmonumenta.plugins.utils.LocationUtils;
import com.playmonumenta.plugins.utils.PlayerUtils;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.List;

public class SpellThrowSummon extends Spell {

	public static final int MOB_DETECTION_RADIUS = 10;
	public static final int MOB_CAP = 15;

	private final Plugin mPlugin;
	private final LivingEntity mBoss;
	private final int mRange;
	private final int mLobs;
	private final int mCooldown;
	private final String mSummonName;

	public SpellThrowSummon(Plugin plugin, LivingEntity boss, int range, int lobs, int cooldownTicks, String summonName) {
		mPlugin = plugin;
		mBoss = boss;
		mRange = range;
		mLobs = lobs;
		mCooldown = cooldownTicks;
		mSummonName = summonName;
	}

	@Override
	public void run() {
		// Choose random player within range that has line of sight to boss
		List<Player> players = PlayerUtils.playersInRange(mBoss.getLocation(), mRange, false);

		BukkitRunnable task = new BukkitRunnable() {
			int mTicks = 0;
			@Override
			public void run() {
				mTicks++;

				// TODO: Add particles
				Collections.shuffle(players);
				for (Player player : players) {
					if (!player.getGameMode().equals(GameMode.CREATIVE) && LocationUtils.hasLineOfSight(mBoss, player)) {
						launch(player);
						break;
					}
				}
				if (mTicks >= mLobs) {
					this.cancel();
				}
			}

		};

		task.runTaskTimer(mPlugin, 0, 15);
		mActiveRunnables.add(task);
	}

	@Override
	public int cooldownTicks() {
		return mCooldown;
	}

	public void launch(Player target) {
		Location sLoc = mBoss.getEyeLocation();
		sLoc.getWorld().playSound(sLoc, Sound.ENTITY_SHULKER_SHOOT, 1, 1);
		try {
			Entity e = LibraryOfSoulsIntegration.summon(sLoc, mSummonName);
			Location pLoc = target.getLocation();
			Location tLoc = e.getLocation();
			Vector vect = new Vector(pLoc.getX() - tLoc.getX(), 0, pLoc.getZ() - tLoc.getZ());
			vect.normalize().multiply(pLoc.distance(tLoc) / 10).setY(0.7f);
			e.setVelocity(vect);

		} catch (Exception e) {
			mPlugin.getLogger().warning("Failed to summon entity for throw summon: " + e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public boolean canRun() {
		if (EntityUtils.getNearbyMobs(mBoss.getLocation(), MOB_DETECTION_RADIUS).size() > MOB_CAP) {
			return false;
		}
		return true;
	}
}