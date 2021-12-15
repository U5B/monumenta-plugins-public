package com.playmonumenta.plugins.itemindex;

import java.util.ArrayList;

import org.checkerframework.checker.nullness.qual.EnsuresNonNullIf;
import org.checkerframework.checker.nullness.qual.Nullable;

import com.playmonumenta.plugins.attributes.AttributeAbilityPower;
import com.playmonumenta.plugins.attributes.AttributeProjectileDamage;
import com.playmonumenta.plugins.attributes.AttributeProjectileSpeed;
import com.playmonumenta.plugins.attributes.AttributeThornsDamage;
import com.playmonumenta.plugins.attributes.AttributeThrowRate;
import com.playmonumenta.plugins.attributes.BaseAttribute;

public enum Attribute {
	ATTACK_DAMAGE(org.bukkit.attribute.Attribute.GENERIC_ATTACK_DAMAGE, " Attack Damage"),
	ATTACK_SPEED(org.bukkit.attribute.Attribute.GENERIC_ATTACK_SPEED, " Attack Speed"),
	RANGED_DAMAGE(new AttributeProjectileDamage()),
	PROJECTILE_SPEED(new AttributeProjectileSpeed()),
	THROW_RATE(new AttributeThrowRate()),
	THORNS_DAMAGE(new AttributeThornsDamage()),
	ABILITY_POWER(new AttributeAbilityPower()),

	MAX_HEALTH(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH, " Max Health"),
	FOLLOW_RANGE(org.bukkit.attribute.Attribute.GENERIC_FOLLOW_RANGE, " Follow Range"),
	KNOCKBACK_RESISTANCE(org.bukkit.attribute.Attribute.GENERIC_KNOCKBACK_RESISTANCE, " Knockback Resistance"),
	MOVEMENT_SPEED(org.bukkit.attribute.Attribute.GENERIC_MOVEMENT_SPEED, " Speed"),
	FLYING_SPEED(org.bukkit.attribute.Attribute.GENERIC_FLYING_SPEED, " Flying Speed"),
	ARMOR(org.bukkit.attribute.Attribute.GENERIC_ARMOR, " Armor"),
	TOUGHNESS(org.bukkit.attribute.Attribute.GENERIC_ARMOR_TOUGHNESS, " Armor Toughness"),
	LUCK(org.bukkit.attribute.Attribute.GENERIC_LUCK, " Luck"),
	HORSE_JUMP_STRENGTH(org.bukkit.attribute.Attribute.HORSE_JUMP_STRENGTH, " Horse Jump Strength"),
	ZOMBIE_SPAWN_REINFORCEMENTS(org.bukkit.attribute.Attribute.ZOMBIE_SPAWN_REINFORCEMENTS, " Zombie Spawn Reinforcements");

	private final org.bukkit.attribute.@Nullable Attribute mBukkitAttribute;
	private final @Nullable BaseAttribute mCustomAttributeClass;
	private final String mReadableStringFormat;

	Attribute(org.bukkit.attribute.Attribute bukkitAttribute, String s) {
		this.mBukkitAttribute = bukkitAttribute;
		this.mCustomAttributeClass = null;
		this.mReadableStringFormat = s;
	}

	Attribute(BaseAttribute customAttribute) {
		this.mBukkitAttribute = null;
		this.mCustomAttributeClass = customAttribute;
		this.mReadableStringFormat = customAttribute.getProperty();
	}

	@EnsuresNonNullIf(expression = "getBukkitAttribute()", result = true)
	public boolean isCustom() {
		return this.mCustomAttributeClass != null;
	}

	public String getReadableStringFormat() {
		return this.mReadableStringFormat;
	}

	public org.bukkit.attribute.@Nullable Attribute getBukkitAttribute() {
		return this.mBukkitAttribute;
	}

	public static String[] valuesAsStringArray() {
		ArrayList<String> out = new ArrayList<>();
		for (Attribute s : Attribute.values()) {
			out.add(s.toString().toLowerCase());
		}
		return out.toArray(new String[0]);
	}
}
