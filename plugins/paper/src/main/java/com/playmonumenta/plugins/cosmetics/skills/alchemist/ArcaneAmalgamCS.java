package com.playmonumenta.plugins.cosmetics.skills.alchemist;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.particle.PPCircle;
import com.playmonumenta.plugins.particle.PPLine;
import com.playmonumenta.plugins.particle.PPParametric;
import com.playmonumenta.plugins.particle.PPPeriodic;
import com.playmonumenta.plugins.particle.PartialParticle;
import com.playmonumenta.plugins.utils.FastUtils;
import com.playmonumenta.plugins.utils.VectorUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
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

public class ArcaneAmalgamCS extends UnstableAmalgamCS {

	public static final String NAME = "Arcane Amalgam";

	private static final double COS_30 = Math.cos(Math.toRadians(30));
	private static final double SPARK_PARTICLE_SPEED = 4.5;
	private static final int SPARK_PARTICLE_LIFETIME = 3;

	@Override
	public @Nullable List<String> getDescription() {
		return List.of(
			"Using the volatility of mercury, alchemists",
			"can craft highly reactive, but unstable alloys.",
			"Many an alchemist has lost fingers or entire limbs,",
			"even when handling these with the utmost care.");
	}

	@Override
	public Material getDisplayItem() {
		return Material.ENCHANTING_TABLE;
	}

	@Override
	public @Nullable String getName() {
		return NAME;
	}

	@Override
	public void periodicEffects(Player caster, Location loc, double radius, int ticks, int duration) {
		if (ticks % SPARK_PARTICLE_LIFETIME != 0) {
			return;
		}

		if (ticks % (2 * SPARK_PARTICLE_LIFETIME) == 0) {
			float fraction = 1f * ticks / duration;
			loc.getWorld().playSound(loc, Sound.BLOCK_AMETHYST_BLOCK_STEP, SoundCategory.PLAYERS, 0.5f + 0.5f * fraction, 0.5f + 0.5f * fraction);
		}

		loc = loc.clone().add(0, 0.25, 0);

		// big circle
		new PPCircle(Particle.ENCHANTMENT_TABLE, loc.clone().add(0, 0.25, 0), radius)
			.ringMode(true)
			.countPerMeter(0.4)
			.arcDegree(loc.getYaw(), loc.getYaw() + 360)
			.offset((ticks % 20) / 20.0)
			.directionalMode(true).delta(0, -0.25, 0).extra(1)
			.spawnAsPlayerActive(caster);

		// lines towards the center
		// outer half: star-shaped
		double lineLength = radius / 2 / COS_30;
		double outerLength = Math.min(2.0 * ticks / duration, 1) * lineLength;
		for (int i = 0; i < 6; i++) {
			double rot = loc.getYaw() + 60 * i;
			Location lineStart = loc.clone().add(VectorUtils.rotateYAxis(new Vector(radius, 0, 0), rot));
			for (int j = 0; j < 2; j++) {
				Vector dir = VectorUtils.rotateYAxis(new Vector(1, 0, 0), rot + (j == 0 ? -150 : 150));
				Location lineEnd = lineStart.clone().add(dir.clone().multiply(outerLength));
				if (ticks <= duration / 2.0 - 2) {
					new PartialParticle(Particle.ELECTRIC_SPARK, lineEnd)
						.directionalMode(true).delta(dir.getX(), 0, dir.getZ()).extra(0.085 * radius)
						.spawnAsPlayerActive(caster);
				}
				new PPLine(Particle.ENCHANTMENT_TABLE, lineStart.clone().add(0, 0.25, 0), lineEnd.clone().add(0, 0.25, 0))
					.countPerMeter(0.2)
					.offset((ticks % 20) / 20.0)
					.directionalMode(true).delta(0, -0.25, 0).extra(1)
					.spawnAsPlayerActive(caster);
			}
		}

		// inner half: straight rays
		if (ticks >= duration / 2.0) {
			double innerLength = (2.0 * ticks / duration - 1) * lineLength;
			for (int i = 0; i < 6; i++) {
				double rot = loc.getYaw() + 30 + 60 * i;
				Vector dir = VectorUtils.rotateYAxis(new Vector(1, 0, 0), rot); // direction to center
				Location lineStart = loc.clone().add(dir.clone().multiply(-lineLength));
				Location lineEnd = lineStart.clone().add(dir.clone().multiply(innerLength));
				new PartialParticle(Particle.ELECTRIC_SPARK, lineEnd)
					.directionalMode(true)
					.delta(dir.getX(), 0, dir.getZ())
					.extra(SPARK_PARTICLE_SPEED / COS_30 * radius / duration)
					.spawnAsPlayerActive(caster);
				if (ticks > duration / 2.0) {
					new PPLine(Particle.ENCHANTMENT_TABLE, lineStart.clone().add(0, 0.25, 0), lineEnd.clone().add(0, 0.25, 0))
						.countPerMeter(0.2)
						.offset((ticks % 20) / 20.0)
						.directionalMode(true)
						.delta(0, -0.25, 0)
						.extra(1)
						.spawnAsPlayerActive(caster);
				}
			}
		}
	}

	@Override
	public void explodeEffects(Player caster, Location loc, double radius) {
		World world = loc.getWorld();
		world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 0.6f, 0f);
		world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 0.6f, 1.25f);
		world.playSound(loc, Sound.BLOCK_AMETHYST_CLUSTER_BREAK, SoundCategory.PLAYERS, 2, 0.5f);
		world.playSound(loc, Sound.BLOCK_AMETHYST_BLOCK_BREAK, SoundCategory.PLAYERS, 2, 0.5f);

		// exploding enchantment particles
		new PPParametric(Particle.ENCHANTMENT_TABLE, loc,
			(param, builder) -> {
				double yaw = FastUtils.randomDoubleInRange(0, 360);
				double pitch = -FastUtils.randomDoubleInRange(-20, 80);
				double distance = FastUtils.randomDoubleInRange(0.5, 1) * radius;
				Vector dir = VectorUtils.rotationToVector(yaw, pitch).multiply(distance);
				builder.offset(-dir.getX(), -dir.getY(), -dir.getZ());
				builder.location(loc.clone().add(dir));
			}).count(30)
			.directionalMode(true)
			.extra(1)
			.spawnAsPlayerActive(caster);

		// sparks with enchantment particle trails
		int numSparks = 8;
		int numSplitSparks = 4;
		double sparkLength = 0.75 * radius;
		double splitSparkLength = 0.5 * radius;
		int sparkTime = 2 * SPARK_PARTICLE_LIFETIME; // time before splitting
		int splitSparkTime = 1 * SPARK_PARTICLE_LIFETIME;
		new BukkitRunnable() {
			int mT = 0;
			List<Location> mStarts = new ArrayList<>(IntStream.range(0, numSparks).mapToObj(i -> loc).toList());
			List<Vector> mDirections = new ArrayList<>(IntStream.range(0, numSparks).mapToObj(i -> {
				Vector v = VectorUtils.randomUnitVector().multiply(sparkLength);
				if (i < 2 && v.getY() < 0) {
					// make sure at least 2 sparks travel upwards
					v.multiply(-1);
				}
				return v;
			}).toList());

			@Override
			public void run() {

				// split the spark after a while
				if (mT == sparkTime) {
					List<Location> oldStarts = mStarts;
					List<Vector> oldDirections = mDirections;
					mStarts = new ArrayList<>(numSparks * numSplitSparks);
					mDirections = new ArrayList<>(numSparks * numSplitSparks);
					for (int i = 0; i < numSparks; i++) {
						Location start = oldStarts.get(i).clone().add(oldDirections.get(i));
						for (int j = 0; j < numSplitSparks; j++) {
							mStarts.add(start);
							mDirections.add(VectorUtils.randomUnitVector().multiply(splitSparkLength));
						}
					}
				}

				for (int i = 0; i < mStarts.size(); i++) {
					Vector dir = mDirections.get(i);
					Location start = mStarts.get(i);
					Location l = start.clone().add(dir.clone().multiply(mT >= sparkTime ? 1.0 * (mT - sparkTime) / splitSparkTime : 1.0 * mT / sparkTime));
					if (mT % SPARK_PARTICLE_LIFETIME == 0) {
						new PartialParticle(Particle.ELECTRIC_SPARK, l)
							.directionalMode(true)
							.delta(dir.getX(), dir.getY(), dir.getZ())
							.extra(SPARK_PARTICLE_SPEED / sparkTime)
							.spawnAsPlayerActive(caster);
					}
					new PPPeriodic(Particle.ENCHANTMENT_TABLE, l)
						.manualTimeOverride(mT)
						.spawnAsPlayerActive(caster);
				}

				mT++;
				if (mT >= sparkTime + splitSparkTime) {
					cancel();
				}
			}
		}.runTaskTimer(Plugin.getInstance(), 0, 1);

	}

	@Override
	public void unstablePotionSplash(Player caster, Location loc, double radius) {
		new PPCircle(Particle.ENCHANTMENT_TABLE, loc.clone().add(0, 0.25, 0), radius)
			.ringMode(true)
			.countPerMeter(6)
			.directionalMode(true).delta(0, -0.25, 0).extra(1)
			.spawnAsPlayerActive(caster);
	}

}
