package com.playmonumenta.plugins.bosses.spells.kaul;

import com.playmonumenta.plugins.bosses.bosses.Kaul;
import com.playmonumenta.plugins.bosses.spells.Spell;
import com.playmonumenta.plugins.particle.PartialParticle;
import com.playmonumenta.plugins.utils.BossUtils;
import com.playmonumenta.plugins.utils.PlayerUtils;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;


public class SpellLightningStorm extends Spell {
	private int mTicks = 0;
	private final LivingEntity mBoss;
	private final Kaul mKaul;
	private static final String LIGHTNING_STORM_TAG = "KaulLightningStormTag";
	private @Nullable Location mCenter;
	private final List<Player> mWarnedPlayers = new ArrayList<>();
	private static final Particle.DustOptions YELLOW_1_COLOR = new Particle.DustOptions(Color.fromRGB(255, 255, 20), 1.0f);
	private static final Particle.DustOptions YELLOW_2_COLOR = new Particle.DustOptions(Color.fromRGB(255, 255, 120), 1.0f);

	public SpellLightningStorm(LivingEntity boss, Kaul kaul) {
		mBoss = boss;
		mKaul = kaul;
		for (Entity e : boss.getWorld().getEntities()) {
			if (e.getScoreboardTags().contains(LIGHTNING_STORM_TAG) && e instanceof LivingEntity) {
				mCenter = e.getLocation();
				break;
			}
		}
	}

	@Override
	public void run() {
		if (mCenter == null) {
			return;
		}
		mTicks--;
		if (mTicks <= 0) {
			//TODO For now, increased interval a bit to avoid getting lightninged multiple times while unable to descend any faster, especially during lag spikes.
			// Still have to fix players getting missed (becomes matter of luck on when the check runs).
			// Instead, could check more frequently but add them to an anti-lightning list for about 2s as a per-player cooldown
			mTicks = 10;

			double arenaSurfaceY = mCenter.getY(); // This should be 8.0 based on the armour stand placement
			for (Player player : mKaul.getArenaParticipants()) {
				double playerY = player.getLocation().getY();

				// If standing on heightened ground or going up a climbable to cheese,
				// enforce stricter 2-block threshold.
				// Eg disallow pillaring up
				int yThreshold = 2;

				// If truly in the air (not on ground and not climbing - like the Thunder Step requirement),
				// have relaxed 8-block threshold.
				// Eg recoil, jump boost, Primordial yeet
				if (PlayerUtils.isFreeFalling(player)) {
					yThreshold = 8;
				}

				if (playerY > arenaSurfaceY + yThreshold) {
					lightning(player);
				}
			}
		}
	}

	private void lightning(Player player) {
		World world = player.getWorld();
		Location strike = player.getLocation().add(0, 10, 0);
		Location loc = player.getLocation();
		for (int i = 0; i < 10; i++) {
			strike.subtract(0, 1, 0);
			new PartialParticle(Particle.REDSTONE, strike, 10, 0.3, 0.3, 0.3, YELLOW_1_COLOR).spawnAsEntityActive(mBoss);
			new PartialParticle(Particle.REDSTONE, strike, 10, 0.3, 0.3, 0.3, YELLOW_2_COLOR).spawnAsEntityActive(mBoss);
		}
		new PartialParticle(Particle.FIREWORKS_SPARK, loc, 15, 0, 0, 0, 0.25).spawnAsEntityActive(mBoss);
		new PartialParticle(Particle.FLAME, loc, 50, 0, 0, 0, 0.175).spawnAsEntityActive(mBoss);
		new PartialParticle(Particle.SMOKE_LARGE, loc, 15, 0, 0, 0, 0.25).spawnAsEntityActive(mBoss);
		world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 1, 0.9f);
		world.playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.HOSTILE, 1, 1);

		/* Too-high lightning hits you directly and can not be blocked */
		BossUtils.bossDamagePercent(mBoss, player, 0.5, "Lightning Storm");
		if (!mWarnedPlayers.contains(player)) {
			mWarnedPlayers.add(player);
			player.sendMessage(ChatColor.AQUA
				                   + "That hurt! There must be a lightning storm above you. Staying close to the ground might help to not get struck again.");
		}
	}

	@Override
	public int cooldownTicks() {
		return 0;
	}
}
