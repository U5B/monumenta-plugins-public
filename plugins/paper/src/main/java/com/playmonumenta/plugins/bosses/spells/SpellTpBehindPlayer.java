package com.playmonumenta.plugins.bosses.spells;

import com.playmonumenta.plugins.particle.PartialParticle;
import com.playmonumenta.plugins.utils.AbilityUtils;
import com.playmonumenta.plugins.utils.EntityUtils;
import com.playmonumenta.plugins.utils.FastUtils;
import com.playmonumenta.plugins.utils.PlayerUtils;
import com.playmonumenta.plugins.utils.ZoneUtils;
import com.playmonumenta.plugins.utils.ZoneUtils.ZoneProperty;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public class SpellTpBehindPlayer extends Spell {

	private static final int TP_STUN_CREEPER_INCREASED = 20; // Increased time for creepers
	private static final int DISTANCE_TO_PLAYER = 2 * 4; // How many half-blocks behind the player maximum
	private static final int VERTICAL_DISTANCE_TO_PLAYER = 3;

	private final Plugin mPlugin;
	protected final LivingEntity mLauncher;
	private final int mCooldown;
	private final int mRange;
	private final int mTPDelay;
	private final int mTPStun;
	private final boolean mRandom;


	public SpellTpBehindPlayer(Plugin plugin, LivingEntity launcher, int cooldown) {
		this(plugin, launcher, cooldown, 20, 50, 10, false);
	}

	public SpellTpBehindPlayer(Plugin plugin, LivingEntity launcher, int cooldown, int range, int delay, int stun, boolean random) {
		mPlugin = plugin;
		mLauncher = launcher;
		mCooldown = cooldown;
		mRange = range;
		mTPDelay = delay;
		mTPStun = stun;
		mRandom = random;
	}

	@Override
	public void run() {
		if (mLauncher instanceof Mob) {
			Player targetPlayer = null;
			if (mRandom) {
				List<Player> players = PlayerUtils.playersInRange(mLauncher.getLocation(), mRange, false);
				while (!players.isEmpty()) {
					LivingEntity target = players.get(FastUtils.RANDOM.nextInt(players.size()));
					/* Do not teleport to players in safezones */
					if (ZoneUtils.hasZoneProperty(target, ZoneProperty.RESIST_5) || ZoneUtils.hasZoneProperty(target, ZoneProperty.LOOTROOM)) {
						/* This player is in a safe area - don't tp to them */
						players.remove(target);
					} else {
						targetPlayer = (Player) target;
						break;
					}
				}
			} else {
				LivingEntity target = ((Mob) mLauncher).getTarget();
				if (target instanceof Player && target.getLocation().distance(mLauncher.getLocation()) < mRange
					    && !ZoneUtils.hasZoneProperty(target, ZoneProperty.RESIST_5)) {
					targetPlayer = (Player) target;
				}
			}
			if (targetPlayer != null) {
				launch(targetPlayer, mRange);
				animation(targetPlayer);
			}
		}
	}

	@Override
	public int cooldownTicks() {
		return mCooldown;
	}

	protected void launch(Player target, double maxRange) {
		BukkitRunnable runnable = new BukkitRunnable() {
			@Override
			public void run() {
				Location loc = target.getLocation();
				// We need a second check when the teleport is actually about to occur
				if (loc.distance(mLauncher.getLocation()) > maxRange || ZoneUtils.hasZoneProperty(target, ZoneProperty.RESIST_5)) {
					return;
				}

				World world = target.getWorld();
				loc.setY(loc.getY() + 0.1f);
				Vector shift = loc.getDirection();
				shift.setY(0).normalize().multiply(-0.5);

				// Check from farthest horizontally to closest, lowest vertically to highest
				for (int horizontalShift = DISTANCE_TO_PLAYER; horizontalShift > 0; horizontalShift--) {
					for (int verticalShift = 0; verticalShift <= VERTICAL_DISTANCE_TO_PLAYER; verticalShift++) {
						Location locTest = loc.clone().add(shift.clone().multiply(horizontalShift));
						locTest.setY(locTest.getY() + verticalShift);
						if (canTeleport(locTest)) {
							loc.add(0, mLauncher.getHeight() / 2, 0);
							new PartialParticle(Particle.SPELL_WITCH, loc, 30, 0.25, 0.45, 0.25, 1).spawnAsEntityActive(mLauncher);
							new PartialParticle(Particle.SMOKE_LARGE, loc, 12, 0, 0.45, 0, 0.125).spawnAsEntityActive(mLauncher);

							EntityUtils.teleportStack(mLauncher, locTest);
							if (mLauncher instanceof Mob mob && !AbilityUtils.isStealthed(target)) {
								mob.setTarget(target);
								// For some reason just setting the target doesn't seem to be enough, so try again a tick later
								Bukkit.getScheduler().runTaskLater(mPlugin, () -> {
									mob.setTarget(target);
								}, 1);
							}
							onTeleport(target);

							locTest.add(0, mLauncher.getHeight() / 2, 0);
							new PartialParticle(Particle.SPELL_WITCH, locTest, 30, 0.25, 0.45, 0.25, 1).spawnAsEntityActive(mLauncher);
							new PartialParticle(Particle.SMOKE_LARGE, locTest, 12, 0, 0.45, 0, 0.125).spawnAsEntityActive(mLauncher);
							world.playSound(locTest, Sound.ENTITY_ENDERMAN_TELEPORT, SoundCategory.HOSTILE, 3f, 0.7f);

							// The mPlugin here is of the incorrect type for some reason
							if (mLauncher instanceof Creeper) {
								EntityUtils.applyCooling(com.playmonumenta.plugins.Plugin.getInstance(), mTPStun + TP_STUN_CREEPER_INCREASED, mLauncher);
							} else {
								EntityUtils.applyCooling(com.playmonumenta.plugins.Plugin.getInstance(), mTPStun, mLauncher);
							}

							// Janky way to break out of nested loop
							horizontalShift = 0;
							break;
						}
					}
				}
			}
		};

		runnable.runTaskLater(mPlugin, mTPDelay);
		mActiveRunnables.add(runnable);
	}

	protected void onTeleport(Player player) {
		// Does nothing, can be overridden by extending spells
	}

	protected void animation(Player target) {
		target.getWorld().playSound(target.getLocation(), Sound.ENTITY_WITCH_AMBIENT, SoundCategory.HOSTILE, 1.4f, 0.5f);

		BukkitRunnable runnable = new BukkitRunnable() {
			int mTicks = 0;

			@Override
			public void run() {
				mTicks++;
				Location particleLoc = mLauncher.getLocation().add(new Location(mLauncher.getWorld(), -0.5f, 0f, 0.5f));
				new PartialParticle(Particle.PORTAL, particleLoc, 10, 1, 1, 1, 0.03).spawnAsEntityActive(mLauncher);

				if (mTicks > mTPDelay) {
					this.cancel();
				}
			}
		};
		runnable.runTaskTimer(mPlugin, 0, 1);
		mActiveRunnables.add(runnable);
	}

	private boolean canTeleport(Location loc) {
		World world = loc.getWorld();

		// Move a bounding box to the target location
		BoundingBox box = mLauncher.getBoundingBox();
		box.shift(loc.clone().subtract(mLauncher.getLocation()));

		// Check the 8 corners of the bounding box and the location itself for blocks
		if (isObstructed(loc, box)
			    || isObstructed(new Location(world, box.getMinX(), box.getMinY(), box.getMinZ()), box)
			    || isObstructed(new Location(world, box.getMinX(), box.getMinY(), box.getMaxZ()), box)
			    || isObstructed(new Location(world, box.getMinX(), box.getMaxY(), box.getMinZ()), box)
			    || isObstructed(new Location(world, box.getMinX(), box.getMaxY(), box.getMaxZ()), box)
			    || isObstructed(new Location(world, box.getMaxX(), box.getMinY(), box.getMinZ()), box)
			    || isObstructed(new Location(world, box.getMaxX(), box.getMinY(), box.getMaxZ()), box)
			    || isObstructed(new Location(world, box.getMaxX(), box.getMaxY(), box.getMinZ()), box)
			    || isObstructed(new Location(world, box.getMaxX(), box.getMaxY(), box.getMaxZ()), box)) {
			return false;
		}

		return true;
	}

	private boolean isObstructed(Location loc, BoundingBox box) {
		Block block = loc.getBlock();
		return block.getBoundingBox().overlaps(box) && !block.isLiquid();
	}

}
