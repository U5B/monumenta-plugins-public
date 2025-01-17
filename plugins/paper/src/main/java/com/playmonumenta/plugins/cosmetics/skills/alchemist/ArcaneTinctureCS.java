package com.playmonumenta.plugins.cosmetics.skills.alchemist;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.particle.PPCircle;
import com.playmonumenta.plugins.particle.PPPeriodic;
import com.playmonumenta.plugins.particle.PartialParticle;
import com.playmonumenta.plugins.utils.FastUtils;
import com.playmonumenta.plugins.utils.VectorUtils;
import java.util.List;
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

public class ArcaneTinctureCS extends IronTinctureCS {

	public static final String NAME = "Arcane Tincture";

	@Override
	public @Nullable List<String> getDescription() {
		return List.of(
			"Enchanting a potion with protective spells",
			"provides a formidable and fast-acting line of",
			"defense for the traveling alchemist.");
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
	public String tinctureName() {
		return "Arcane Tincture";
	}

	@Override
	public void onGroundEffect(Location location, Player caster, int twoTicks) {
		// sound
		if (twoTicks % 10 == 0) {
			location.getWorld().playSound(location, Sound.BLOCK_MEDIUM_AMETHYST_BUD_PLACE, SoundCategory.PLAYERS, 1, 0.5f);
		}

		// particles
		Vector vec = VectorUtils.rotateYAxis(new Vector(0.4, 0, 0), twoTicks * 15);
		new PPPeriodic(Particle.ENCHANTMENT_TABLE, location.clone().add(vec).add(0, 0.4, 0))
			.manualTimeOverride(twoTicks)
			.directionalMode(true).delta(0, -0.25, 0).extra(1)
			.spawnAsPlayerActive(caster);
		new PPPeriodic(Particle.ENCHANTMENT_TABLE, location.clone().subtract(vec).add(0, 0.4, 0))
			.manualTimeOverride(twoTicks)
			.directionalMode(true).delta(0, -0.25, 0).extra(1)
			.spawnAsPlayerActive(caster);
		if (twoTicks > 0 && twoTicks % 5 == 0) {
			new PPPeriodic(Particle.WAX_OFF, location.clone().add(0, 0.3, 0))
				.manualTimeOverride(twoTicks)
				.delta(0.05, 0.1, 0.05)
				.spawnAsPlayerActive(caster);
		}
	}

	@Override
	public void tinctureExpireEffects(Location location, Player caster) {
		location.getWorld().playSound(location, Sound.BLOCK_MEDIUM_AMETHYST_BUD_BREAK, SoundCategory.PLAYERS, 1, 0.5f);
		new PartialParticle(Particle.ENCHANTMENT_TABLE, location.clone().add(0, 0.3, 0))
				.count(20).delta(0.05, 0.1, 0.05)
				.spawnAsPlayerActive(caster);
	}

	@Override
	public void pickupEffects(Location location, Player p) {
		location.getWorld().playSound(location, Sound.BLOCK_GLASS_BREAK, SoundCategory.PLAYERS, 1, 0.85f);
		location.getWorld().playSound(location, Sound.BLOCK_LARGE_AMETHYST_BUD_BREAK, SoundCategory.PLAYERS, 1, 0.5f);
		new PartialParticle(Particle.BLOCK_DUST, location, 50, 0.1, 0.1, 0.1, 0.1, Material.GLASS.createBlockData()).spawnAsPlayerActive(p);
		new PartialParticle(Particle.ENCHANTMENT_TABLE, location.clone().add(0, 0.3, 0))
				.count(20).delta(0.05, 0.1, 0.05)
				.spawnAsPlayerActive(p);
	}

	@Override
	public void pickupEffectsForPlayer(Player player, Location tinctureLocation) {
		World world = player.getWorld();
		world.playSound(player.getLocation(), Sound.ENTITY_ILLUSIONER_PREPARE_MIRROR, SoundCategory.PLAYERS, 1.2f, 1.25f);
		new PartialParticle(Particle.SPELL_INSTANT, player.getLocation(), 20, 0.25, 0.1, 0.25, 1).spawnAsPlayerActive(player);
		double radius = 1.15;
		new PPCircle(Particle.ENCHANTMENT_TABLE, player.getLocation().add(0, 0.2, 0), radius)
				.ringMode(true).countPerMeter(8)
				.spawnAsPlayerActive(player);
		new BukkitRunnable() {
			double mRotation = 0;
			double mY = 0.15;

			final PPPeriodic mParticleMain = new PPPeriodic(Particle.SCRAPE, player.getLocation());
			final PPPeriodic mParticleEnchant = new PPPeriodic(Particle.ENCHANTMENT_TABLE, player.getLocation());

			@Override
			public void run() {
				double rotStep = 20;
				double yStep = 0.175;
				mRotation += rotStep;
				mY += yStep;
				for (int i = 0; i < 3; i++) {
					double degree = mRotation + (i * 120);
					mParticleMain.location(player.getLocation().add(FastUtils.cosDeg(degree) * radius, mY, FastUtils.sinDeg(degree) * radius))
							.spawnAsPlayerActive(player);
					mParticleEnchant.location(player.getLocation().add(FastUtils.cosDeg(degree - rotStep / 2) * radius, mY - yStep / 2, FastUtils.sinDeg(degree - rotStep / 2) * radius))
							.spawnAsPlayerActive(player);
				}

				if (mY >= 1.8) {
					this.cancel();
				}
			}
		}.runTaskTimer(Plugin.getInstance(), 0, 1);
	}

}
