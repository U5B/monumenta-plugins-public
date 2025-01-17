package com.playmonumenta.plugins.cosmetics.skills.rogue;

import com.google.common.collect.ImmutableMap;
import com.playmonumenta.plugins.classes.ClassAbility;
import com.playmonumenta.plugins.cosmetics.Cosmetic;
import com.playmonumenta.plugins.cosmetics.skills.CosmeticSkill;
import com.playmonumenta.plugins.particle.PartialParticle;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class AdvancingShadowsCS implements CosmeticSkill {

	@Override
	public ClassAbility getAbility() {
		return ClassAbility.ADVANCING_SHADOWS;
	}

	@Override
	public Material getDisplayItem() {
		return Material.ENDER_EYE;
	}

	public void tpStart(Player mPlayer) {

	}

	public void tpParticle(Player mPlayer) {
		new PartialParticle(Particle.SPELL_WITCH, mPlayer.getLocation().add(0, 1.1, 0), 50, 0.35, 0.5, 0.35, 1.0).spawnAsPlayerActive(mPlayer);
		new PartialParticle(Particle.SMOKE_LARGE, mPlayer.getLocation().add(0, 1.1, 0), 12, 0.35, 0.5, 0.35, 0.05).spawnAsPlayerActive(mPlayer);
	}

	public void tpTrail(Player mPlayer, Location loc, int i) {
		new PartialParticle(Particle.SPELL_WITCH, loc.clone().add(0, 1, 0), 4, 0.3, 0.5, 0.3, 1.0).spawnAsPlayerActive(mPlayer);
		new PartialParticle(Particle.SMOKE_NORMAL, loc.clone().add(0, 1, 0), 10, 0.3, 0.5, 0.3, 0.025).spawnAsPlayerActive(mPlayer);
	}

	public void tpSound(World world, Player mPlayer) {
		world.playSound(mPlayer.getLocation(), Sound.ENTITY_ILLUSIONER_MIRROR_MOVE, SoundCategory.PLAYERS, 1.0f, 1.1f);
	}

	public void tpSoundFail(World world, Player mPlayer) {
		world.playSound(mPlayer.getLocation(), Sound.ENTITY_ILLUSIONER_MIRROR_MOVE, SoundCategory.PLAYERS, 1.0f, 1.8f);
	}
}
