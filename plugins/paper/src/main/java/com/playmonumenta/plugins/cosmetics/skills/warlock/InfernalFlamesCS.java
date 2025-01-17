package com.playmonumenta.plugins.cosmetics.skills.warlock;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.classes.ClassAbility;
import com.playmonumenta.plugins.particle.PartialParticle;
import com.playmonumenta.plugins.utils.FastUtils;
import com.playmonumenta.plugins.utils.ParticleUtils;
import java.util.List;
import org.apache.commons.math3.util.FastMath;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

public class InfernalFlamesCS extends CholericFlamesCS {
	//Twisted theme

	public static final String NAME = "Infernal Flames";

	private static final Color TWIST_COLOR_BASE = Color.fromRGB(130, 66, 66);
	private static final Color TWIST_COLOR_TIP = Color.fromRGB(127, 0, 0);

	@Override
	public @Nullable List<String> getDescription() {
		return List.of(
			"This infernal flame was ignited",
			"by your burning, twisted desires.");
	}

	@Override
	public Material getDisplayItem() {
		return Material.SOUL_CAMPFIRE;
	}

	@Override
	public @Nullable String getName() {
		return NAME;
	}

	@Override
	public void flameEffects(Player player, World world, Location loc, double range) {
		world.playSound(loc, Sound.ENTITY_PHANTOM_DEATH, SoundCategory.PLAYERS, 1.5f, 0);
		world.playSound(loc, Sound.ENTITY_WITHER_SHOOT, SoundCategory.PLAYERS, 1.5f, 0.65f);
		world.playSound(loc, Sound.ENTITY_EVOKER_PREPARE_ATTACK, SoundCategory.PLAYERS, 1.5f, 0.85f);
		world.playSound(loc, Sound.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, SoundCategory.PLAYERS, 1.5f, 0.9f);
		world.playSound(loc, Sound.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, SoundCategory.PLAYERS, 1.5f, 0.9f);
		new BukkitRunnable() {

			double mRadius = 0;

			@Override
			public void run() {

				for (int i = 0; i < 2; i++) {
					mRadius += 0.75;

					for (int j = 0; j < 36; j++) {
						final int degree = (360 / 36) * j;
						Vector vec = new Vector(FastUtils.cos(degree) * mRadius, 0, FastUtils.sin(degree) * mRadius);
						Location l = loc.clone().add(vec);

						if (mRadius >= 2 && FastUtils.RANDOM.nextInt(30) == 0) {
							spawnTendril(l, player);
						}

						vec = new Vector(FastUtils.cos(degree) * mRadius, 2 * FastMath.pow(mRadius / (range + 1), 3), FastUtils.sin(degree) * mRadius);
						l = loc.clone().add(vec);
						new PartialParticle(Particle.SOUL_FIRE_FLAME, l, 1, 0.1, 0.1, 0.1, 0.04)
							.minimumCount(0).spawnAsPlayerActive(player);
						new PartialParticle(Particle.SOUL, l, 1, 0.15, 0.15, 0.15, 0.01)
							.minimumCount(0).spawnAsPlayerActive(player);
						new PartialParticle(Particle.SMOKE_NORMAL, l, 5, 0.1, 0.1, 0.1, 0.075)
							.minimumCount(0).spawnAsPlayerActive(player);
					}
				}

				if (mRadius >= range + 1) {
					this.cancel();
				}

			}

		}.runTaskTimer(Plugin.getInstance(), 0, 1);

	}

	private void spawnTendril(Location loc, Player mPlayer) {
		Location to = loc.clone().add(0, 8, 0);

		new BukkitRunnable() {
			final Location mL = loc.clone();
			int mT = 0;

			final int DURATION = FastUtils.RANDOM.nextInt(7, 11);
			final int ITERATIONS = 3;

			final double mXMult = FastUtils.randomDoubleInRange(-1, 1);
			final double mZMult = FastUtils.randomDoubleInRange(-1, 1);
			double mJ = 0;
			@Override
			public void run() {
				mT++;

				for (int i = 0; i < ITERATIONS; i++) {
					mJ++;
					float size = 0.5f + (1.7f * (1f - (float) (mJ / (ITERATIONS * DURATION))));
					double offset = 0.1 * (1f - (mJ / (ITERATIONS * DURATION)));
					double transition = mJ / (ITERATIONS * DURATION);
					double pi = (Math.PI * 2) * (1f - (mJ / (ITERATIONS * DURATION)));


					Vector vec = new Vector(mXMult * FastUtils.cos(pi), 0,
						mZMult * FastUtils.sin(pi));
					Location tendrilLoc = mL.clone().add(vec);

					new PartialParticle(Particle.REDSTONE, tendrilLoc, 3, offset, offset, offset, 0, new Particle.DustOptions(
						ParticleUtils.getTransition(TWIST_COLOR_TIP, TWIST_COLOR_BASE, transition), size))
						.minimumCount(0)
						.spawnAsPlayerActive(mPlayer);

					mL.add(0, 0.25, 0);
					if (mL.distance(to) < 0.4) {
						this.cancel();
						return;
					}
				}

				if (mT >= DURATION) {
					this.cancel();
				}
			}

		}.runTaskTimer(Plugin.getInstance(), 0, 1);
	}
}
