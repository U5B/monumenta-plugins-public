package com.playmonumenta.plugins.bosses.bosses;

import com.playmonumenta.plugins.bosses.spells.Spell;
import com.playmonumenta.plugins.bosses.spells.SpellRunAction;
import com.playmonumenta.plugins.events.DamageEvent;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.List;

public class ImmortalMountBoss extends BossAbilityGroup {

	public static final String identityTag = "boss_immortalmount";
	public static final int detectionRange = 40;

	public static BossAbilityGroup deserialize(Plugin plugin, LivingEntity boss) throws Exception {
		return new ImmortalMountBoss(plugin, boss);
	}

	public ImmortalMountBoss(Plugin plugin, LivingEntity boss) {
		super(plugin, identityTag, boss);

		List<Spell> passiveSpells = Arrays.asList(
			new SpellRunAction(() -> {
				if (boss.getPassengers().size() == 0) {
					boss.setHealth(0);
				}
			}, 1, true)
		);

		super.constructBoss(null, passiveSpells, detectionRange, null);
	}

	@Override
	public void onHurt(DamageEvent event) {
		event.setDamage(0);
	}

}
