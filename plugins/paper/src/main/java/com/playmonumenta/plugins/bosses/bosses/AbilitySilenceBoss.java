package com.playmonumenta.plugins.bosses.bosses;

import com.playmonumenta.plugins.bosses.SpellManager;
import com.playmonumenta.plugins.events.DamageEvent;
import com.playmonumenta.plugins.particle.PartialParticle;
import com.playmonumenta.plugins.utils.AbilityUtils;
import java.util.Collections;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * @deprecated use boss_onhit instead, like this:
 * <blockquote><pre>
 * /boss var Tags add boss_onhit
 * /boss var Tags add boss_onhit[SilenceTicks=60,COLOR=XXXXX]
 * we have yet to find a way to look at color
 * </pre></blockquote>
 * G3m1n1Boy
 */
@Deprecated
public class AbilitySilenceBoss extends BossAbilityGroup {

	public static final String identityTag = "boss_abilitysilence";

	public static class Parameters extends BossParameters {
		public int DETECTION = 32;
		public int DURATION = 20 * 3;
		public Particle.DustOptions COLOR = new Particle.DustOptions(Color.fromRGB(127, 0, 0), 1.0f);
	}

	private final Parameters mParams;

	public static BossAbilityGroup deserialize(Plugin plugin, LivingEntity boss) throws Exception {
		return new AbilitySilenceBoss(plugin, boss);
	}

	public AbilitySilenceBoss(Plugin plugin, LivingEntity boss) throws Exception {
		super(plugin, identityTag, boss);

		mParams = BossParameters.getParameters(boss, identityTag, new Parameters());

		super.constructBoss(SpellManager.EMPTY, Collections.emptyList(), mParams.DETECTION, null);
	}

	@Override
	public void onDamage(DamageEvent event, LivingEntity damagee) {
		if (event.isBlocked()) {
			return;
		}

		if (damagee instanceof Player player) {
			World world = player.getWorld();
			Location loc = player.getLocation().add(0, 1, 0);
			world.playSound(loc, Sound.BLOCK_PORTAL_TRIGGER, SoundCategory.HOSTILE, 0.25f, 2f);
			new PartialParticle(Particle.REDSTONE, loc, 100, 0, 0, 0, 0.5, mParams.COLOR).spawnAsEntityActive(mBoss);

			AbilityUtils.silencePlayer(player, mParams.DURATION);
		}
	}
}
