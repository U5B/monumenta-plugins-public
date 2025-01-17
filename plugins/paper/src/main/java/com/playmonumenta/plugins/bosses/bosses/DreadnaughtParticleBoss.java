package com.playmonumenta.plugins.bosses.bosses;

import com.playmonumenta.plugins.bosses.SpellManager;
import com.playmonumenta.plugins.bosses.spells.Spell;
import com.playmonumenta.plugins.bosses.spells.SpellDreadnaughtParticle;
import com.playmonumenta.plugins.events.DamageEvent;
import com.playmonumenta.plugins.integrations.LibraryOfSoulsIntegration;
import com.playmonumenta.plugins.particle.PartialParticle;
import com.playmonumenta.plugins.utils.EntityUtils;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;

public class DreadnaughtParticleBoss extends BossAbilityGroup {

	public static final String identityTag = "boss_dreadnaughtparticle";
	public static final int detectionRange = 40;

	private static final String DREADLING_TERRAIN_SOUL_NAME = "Dreadling";
	private static final String DREADLING_WATER_SOUL_NAME = "Hydraling";
	private static final double DREADLING_SPAWN_THRESHOLD = 0.35;

	private double mDamageCounter = 0;

	public static BossAbilityGroup deserialize(Plugin plugin, LivingEntity boss) throws Exception {
		return new DreadnaughtParticleBoss(plugin, boss);
	}

	public DreadnaughtParticleBoss(Plugin plugin, LivingEntity boss) {
		super(plugin, identityTag, boss);
		List<Spell> passiveSpells = List.of(
			new SpellDreadnaughtParticle(boss)
		);

		super.constructBoss(SpellManager.EMPTY, passiveSpells, detectionRange, null);
	}

	@Override
	public void onHurtByEntityWithSource(DamageEvent event, Entity damager, LivingEntity source) {
		Location loc = mBoss.getLocation();

		mDamageCounter += event.getFinalDamage(false);

		double regionCap = EntityUtils.getMaxHealth(mBoss) * DREADLING_SPAWN_THRESHOLD;

		if (mDamageCounter >= regionCap) {
			mDamageCounter -= regionCap;

			if (loc.getBlock().getType() == Material.WATER) {
				LibraryOfSoulsIntegration.summon(loc, DREADLING_WATER_SOUL_NAME);
				LibraryOfSoulsIntegration.summon(loc, DREADLING_WATER_SOUL_NAME);
				LibraryOfSoulsIntegration.summon(loc, DREADLING_WATER_SOUL_NAME);
			} else {
				LibraryOfSoulsIntegration.summon(loc, DREADLING_TERRAIN_SOUL_NAME);
				LibraryOfSoulsIntegration.summon(loc, DREADLING_TERRAIN_SOUL_NAME);
				LibraryOfSoulsIntegration.summon(loc, DREADLING_TERRAIN_SOUL_NAME);
			}

			loc.add(0, 1, 0);

			World world = mBoss.getWorld();
			world.playSound(loc, Sound.ENTITY_BLAZE_DEATH, SoundCategory.HOSTILE, 1, 0.5f);
			new PartialParticle(Particle.FLAME, loc, 50, 3, 1, 3, 0).spawnAsEntityActive(mBoss);
			new PartialParticle(Particle.SMOKE_LARGE, loc, 200, 3, 1, 3, 0).spawnAsEntityActive(mBoss);
		}
	}
}
