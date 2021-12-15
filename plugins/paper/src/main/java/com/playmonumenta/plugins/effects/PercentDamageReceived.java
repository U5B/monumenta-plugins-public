package com.playmonumenta.plugins.effects;

import java.util.EnumSet;

import org.bukkit.event.entity.EntityDamageEvent;
import org.checkerframework.checker.nullness.qual.Nullable;

public class PercentDamageReceived extends Effect {

	private final double mAmount;
	private final @Nullable EnumSet<EntityDamageEvent.DamageCause> mAffectedDamageCauses;

	public PercentDamageReceived(int duration, double amount, @Nullable EnumSet<EntityDamageEvent.DamageCause> affectedDamageCauses) {
		super(duration);
		mAmount = amount;
		mAffectedDamageCauses = affectedDamageCauses;
	}

	public PercentDamageReceived(int duration, double amount) {
		this(duration, amount, null);
	}

	@Override
	public double getMagnitude() {
		return Math.abs(mAmount);
	}

	@Override
	public boolean entityReceiveDamageEvent(EntityDamageEvent event) {
		if (mAffectedDamageCauses == null || mAffectedDamageCauses.contains(event.getCause())) {
			event.setDamage(event.getDamage() * (1 + mAmount));
		}

		return true;
	}

	@Override
	public String toString() {
		String causes;
		if (mAffectedDamageCauses != null) {
			causes = "";
			for (EntityDamageEvent.DamageCause cause : mAffectedDamageCauses) {
				if (!causes.isEmpty()) {
					causes += ",";
				}
				causes += cause.name();
			}
		} else {
			causes = "any";
		}
		return String.format("PercentDamageReceived duration:%d causes:%s amount:%f", this.getDuration(), causes, mAmount);
	}
}
