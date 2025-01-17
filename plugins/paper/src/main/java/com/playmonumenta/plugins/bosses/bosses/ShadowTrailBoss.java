package com.playmonumenta.plugins.bosses.bosses;

import com.playmonumenta.plugins.bosses.SpellManager;
import com.playmonumenta.plugins.bosses.spells.Spell;
import com.playmonumenta.plugins.bosses.spells.SpellBaseTrail;
import com.playmonumenta.plugins.events.DamageEvent;
import com.playmonumenta.plugins.particle.PartialParticle;
import com.playmonumenta.plugins.utils.DamageUtils;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class ShadowTrailBoss extends BossAbilityGroup {
	public static final String identityTag = "boss_shadowtrail";
	public static final int detectionRange = 24;

	private static final int TICK_RATE = 3;
	private static final int TRAIL_RATE = 6;
	private static final int TRAIL_DURATION = 20 * 10;
	private static final boolean TRAIL_GROUND_ONLY = true;
	private static final boolean TRAIL_CONSUMED = false;
	private static final int HITBOX_LENGTH = 1;
	private static final int DAMAGE = 15;

	public static BossAbilityGroup deserialize(Plugin plugin, LivingEntity boss) throws Exception {
		return new ShadowTrailBoss(plugin, boss);
	}

	public ShadowTrailBoss(Plugin plugin, LivingEntity boss) {
		super(plugin, identityTag, boss);

		List<Spell> passiveSpells = Arrays.asList(
			new SpellBaseTrail(boss, TICK_RATE, TRAIL_RATE, TRAIL_DURATION, TRAIL_GROUND_ONLY, TRAIL_CONSUMED, HITBOX_LENGTH,
				// Trail Aesthetic
				(World world, Location loc) -> {
					new PartialParticle(Particle.SMOKE_LARGE, loc, 1, 0.3, 0.1, 0.3, 0).spawnAsEntityActive(boss);
					new PartialParticle(Particle.FALLING_OBSIDIAN_TEAR, loc, 1, 0.3, 0.2, 0.3, 0).spawnAsEntityActive(boss);
				},
				// Hit Action
				(World world, Player player, Location loc) -> {
					world.playSound(loc, Sound.ENTITY_SHULKER_SHOOT, SoundCategory.HOSTILE, 1f, 0.5f);
					new PartialParticle(Particle.SMOKE_LARGE, loc, 10, 0, 0, 0, 0.25).spawnAsEntityActive(boss);
					DamageUtils.damage(boss, player, DamageEvent.DamageType.MAGIC, DAMAGE);
				},
				// Expire Action
				(World world, Location loc) -> {
				})
		);

		super.constructBoss(SpellManager.EMPTY, passiveSpells, detectionRange, null);
	}
}
