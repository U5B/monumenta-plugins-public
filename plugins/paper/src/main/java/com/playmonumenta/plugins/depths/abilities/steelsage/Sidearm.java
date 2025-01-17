package com.playmonumenta.plugins.depths.abilities.steelsage;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.abilities.AbilityTrigger;
import com.playmonumenta.plugins.abilities.AbilityTriggerInfo;
import com.playmonumenta.plugins.classes.ClassAbility;
import com.playmonumenta.plugins.depths.DepthsTree;
import com.playmonumenta.plugins.depths.abilities.DepthsAbility;
import com.playmonumenta.plugins.depths.abilities.DepthsAbilityInfo;
import com.playmonumenta.plugins.depths.abilities.DepthsTrigger;
import com.playmonumenta.plugins.events.DamageEvent.DamageType;
import com.playmonumenta.plugins.particle.PartialParticle;
import com.playmonumenta.plugins.utils.DamageUtils;
import com.playmonumenta.plugins.utils.EntityUtils;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public class Sidearm extends DepthsAbility {

	public static final String ABILITY_NAME = "Sidearm";
	private static final int COOLDOWN = 4 * 20;
	private static final int KILL_COOLDOWN_REDUCTION = 3 * 20;
	private static final int[] DAMAGE = {12, 15, 18, 21, 24, 30};
	private static final int RANGE = 14;

	private static final Particle.DustOptions SIDEARM_COLOR = new Particle.DustOptions(Color.fromRGB(130, 130, 130), 1.0f);

	public static final DepthsAbilityInfo<Sidearm> INFO =
		new DepthsAbilityInfo<>(Sidearm.class, ABILITY_NAME, Sidearm::new, DepthsTree.STEELSAGE, DepthsTrigger.RIGHT_CLICK)
			.linkedSpell(ClassAbility.SIDEARM)
			.cooldown(COOLDOWN)
			.actionBarColor(TextColor.color(130, 130, 130))
			.addTrigger(new AbilityTriggerInfo<>("cast", "cast", Sidearm::cast,
				new AbilityTrigger(AbilityTrigger.Key.RIGHT_CLICK).sneaking(false), HOLDING_WEAPON_RESTRICTION))
			.displayItem(new ItemStack(Material.CROSSBOW))
			.descriptions(Sidearm::getDescription);

	public Sidearm(Plugin plugin, Player player) {
		super(plugin, player, INFO);
	}

	public void cast() {
		if (isOnCooldown()) {
			return;
		}
		putOnCooldown();
		Location loc = mPlayer.getEyeLocation();
		BoundingBox box = BoundingBox.of(loc, 0.75, 0.75, 0.75);
		Vector dir = loc.getDirection();
		List<LivingEntity> mobs = EntityUtils.getNearbyMobs(mPlayer.getLocation(), RANGE, mPlayer);
		World world = mPlayer.getWorld();
		new PartialParticle(Particle.SMOKE_NORMAL, loc, 50, 0, 0, 0, 0.125).spawnAsPlayerActive(mPlayer);

		world.playSound(mPlayer.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 1, 2);

		for (int i = 0; i < RANGE; i++) {
			box.shift(dir);
			Location bLoc = box.getCenter().toLocation(world);

			new PartialParticle(Particle.SMOKE_NORMAL, bLoc, 6, 0.05, 0.05, 0.05, 0.05).spawnAsPlayerActive(mPlayer);
			new PartialParticle(Particle.REDSTONE, bLoc, 18, 0.1, 0.1, 0.1, SIDEARM_COLOR).spawnAsPlayerActive(mPlayer);

			if (bLoc.getBlock().getType().isSolid()) {
				bLoc.subtract(dir.multiply(0.5));
				new PartialParticle(Particle.SQUID_INK, bLoc, 30, 0, 0, 0, 0.125).spawnAsPlayerActive(mPlayer);
				world.playSound(bLoc, Sound.ENTITY_FIREWORK_ROCKET_BLAST, SoundCategory.PLAYERS, 1, 0);
				break;
			}
			for (LivingEntity mob : mobs) {
				if (box.overlaps(mob.getBoundingBox())) {
					DamageUtils.damage(mPlayer, mob, DamageType.PROJECTILE_SKILL, DAMAGE[mRarity - 1], mInfo.getLinkedSpell());
					if (mob.isDead() || mob.getHealth() <= 0) {
						mPlugin.mTimers.addCooldown(mPlayer, ClassAbility.SIDEARM, getModifiedCooldown(COOLDOWN - KILL_COOLDOWN_REDUCTION));
					}

					mob.setVelocity(new Vector(0, 0, 0));

					new PartialParticle(Particle.SQUID_INK, bLoc, 30, 0, 0, 0, 0.125).spawnAsPlayerActive(mPlayer);
					world.playSound(bLoc, Sound.ENTITY_FIREWORK_ROCKET_BLAST, SoundCategory.PLAYERS, 1, 0);

					return;
				}
			}

			if (i == 5) {
				new PartialParticle(Particle.SQUID_INK, bLoc, 30, 0, 0, 0, 0.125).spawnAsPlayerActive(mPlayer);
				world.playSound(bLoc, Sound.ENTITY_FIREWORK_ROCKET_BLAST, SoundCategory.PLAYERS, 1, 0);
			}
		}

	}

	private static TextComponent getDescription(int rarity, TextColor color) {
		return Component.text("Right click while holding a weapon to fire a short range flintlock shot that goes up to " + RANGE + " blocks, stopping at the first enemy hit, dealing ")
			.append(Component.text(DAMAGE[rarity - 1], color))
			.append(Component.text(" projectile damage. If it kills a mob, the cooldown is reduced by " + KILL_COOLDOWN_REDUCTION / 20 + " seconds. Cooldown: " + COOLDOWN / 20 + "s."));
	}
}
