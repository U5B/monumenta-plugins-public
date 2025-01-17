package com.playmonumenta.plugins.cosmetics.skills.mage;

import com.google.common.collect.ImmutableMap;
import com.playmonumenta.plugins.classes.ClassAbility;
import com.playmonumenta.plugins.cosmetics.Cosmetic;
import com.playmonumenta.plugins.cosmetics.skills.CosmeticSkill;
import com.playmonumenta.plugins.particle.PartialParticle;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class PrismaticShieldCS implements CosmeticSkill {

	@Override
	public ClassAbility getAbility() {
		return ClassAbility.PRISMATIC_SHIELD;
	}

	@Override
	public Material getDisplayItem() {
		return Material.SHIELD;
	}

	public void prismaEffect(World world, Player mPlayer, double radius) {
		world.playSound(mPlayer.getLocation(), Sound.ITEM_TOTEM_USE, SoundCategory.PLAYERS, 1, 1.35f);
		new PartialParticle(Particle.FIREWORKS_SPARK, mPlayer.getLocation().add(0, 1.15, 0), 150, 0.2, 0.35, 0.2, 0.5).spawnAsPlayerActive(mPlayer);
		new PartialParticle(Particle.SPELL_INSTANT, mPlayer.getLocation().add(0, 1.15, 0), 100, 0.2, 0.35, 0.2, 1).spawnAsPlayerActive(mPlayer);
	}

	public void prismaOnStun(LivingEntity mob, int stunTime, Player mPlayer) {
		//Nope!
	}

	public void prismaOnHeal(Player mPlayer) {
		//Nope!
	}
}
