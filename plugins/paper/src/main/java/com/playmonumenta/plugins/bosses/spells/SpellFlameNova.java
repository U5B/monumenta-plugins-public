package com.playmonumenta.plugins.bosses.spells;

import com.playmonumenta.plugins.events.DamageEvent.DamageType;
import com.playmonumenta.plugins.particle.PartialParticle;
import com.playmonumenta.plugins.utils.BossUtils;
import com.playmonumenta.plugins.utils.EntityUtils;
import com.playmonumenta.plugins.utils.PlayerUtils;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class SpellFlameNova extends SpellBaseAoE {

	private final int mDamage;
	private final int mFireTicks;

	public SpellFlameNova(Plugin plugin, LivingEntity launcher, int radius, int time, int cooldown, int damage, int fireTicks) {
		super(plugin, launcher, radius, time, cooldown, false, Sound.BLOCK_FIRE_AMBIENT);
		mDamage = damage;
		mFireTicks = fireTicks;
	}

	@Override
	protected void chargeAuraAction(Location loc) {
		new PartialParticle(Particle.LAVA, loc, 1, ((double) mRadius) / 2, ((double) mRadius) / 2, ((double) mRadius) / 2, 0.05).spawnAsEntityActive(mLauncher);
	}

	@Override
	protected void chargeCircleAction(Location loc) {
		new PartialParticle(Particle.FLAME, loc, 1, 0.25, 0.25, 0.25, 0.1).spawnAsEntityActive(mLauncher);
	}

	@Override
	protected void outburstAction(Location loc) {
		World world = loc.getWorld();
		world.playSound(loc, Sound.ENTITY_WITHER_SHOOT, SoundCategory.HOSTILE, 1.5f, 0.65F);
	}

	@Override
	protected void circleOutburstAction(Location loc) {
		new PartialParticle(Particle.FLAME, loc, 1, 0.1, 0.1, 0.1, 0.3).spawnAsEntityActive(mLauncher);
		new PartialParticle(Particle.SMOKE_NORMAL, loc, 2, 0.25, 0.25, 0.25, 0.1).spawnAsEntityActive(mLauncher);
	}

	@Override
	protected void dealDamageAction(Location loc) {
		for (Player player : PlayerUtils.playersInRange(mLauncher.getLocation(), mRadius, true)) {
			BossUtils.blockableDamage(mLauncher, player, DamageType.MAGIC, mDamage);
			EntityUtils.applyFire(com.playmonumenta.plugins.Plugin.getInstance(), mFireTicks, player, mLauncher);
		}
	}

}
