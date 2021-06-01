package com.playmonumenta.plugins.bosses.bosses;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.plugin.Plugin;

import com.playmonumenta.plugins.utils.AbilityUtils;
import com.playmonumenta.plugins.utils.BossUtils;


/**
 * @deprecated use boss_onhit instead, like this:
 *<blockquote><pre>
 * /boss var Tags add boss_onhit
 * /boss var Tags add boss_onhit[SilenceTicks=100]
 * CARE this ability has some particle & sound, fix those too if you don't want the default values
 * </pre></blockquote>
 * @G3m1n1Boy
 *
*/
public class SilenceOnHitBoss extends BossAbilityGroup {
	public static final String identityTag = "boss_silencehit";
	public static final int detectionRange = 30;

	public static BossAbilityGroup deserialize(Plugin plugin, LivingEntity boss) {
		return new SilenceOnHitBoss(plugin, boss);
	}

	public SilenceOnHitBoss(Plugin plugin, LivingEntity boss) {
		super(plugin, identityTag, boss);
		super.constructBoss(null, null, detectionRange, null);
	}

	@Override
	public void bossDamagedEntity(EntityDamageByEntityEvent event) {
		LivingEntity target = (LivingEntity) event.getEntity();

		if (target instanceof Player) {
			Player player = (Player)target;
			if (BossUtils.bossDamageBlocked(player, event.getDamage(), event.getDamager().getLocation()) && event.getCause() != DamageCause.MAGIC) {
				return;
			}
			AbilityUtils.silencePlayer((Player)target, 5 * 20);
		}

	}
}
