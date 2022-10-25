package com.playmonumenta.plugins.bosses.bosses.bluestrike;

import com.playmonumenta.plugins.bosses.SpellManager;
import com.playmonumenta.plugins.bosses.bosses.BossAbilityGroup;
import com.playmonumenta.plugins.bosses.spells.Spell;
import com.playmonumenta.plugins.bosses.spells.SpellBlockBreak;
import com.playmonumenta.plugins.particle.PartialParticle;
import com.playmonumenta.plugins.utils.BossUtils;
import com.playmonumenta.plugins.utils.EntityUtils;
import com.playmonumenta.plugins.utils.LocationUtils;
import com.playmonumenta.plugins.utils.MMLog;
import com.playmonumenta.plugins.utils.MovementUtils;
import com.playmonumenta.plugins.utils.PlayerUtils;
import com.playmonumenta.plugins.utils.ScoreboardUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public class LavaCannonBoss extends BossAbilityGroup {
	private static final Particle.DustOptions ORANGE = new Particle.DustOptions(Color.fromRGB(255, 128, 0), 1.0f);
	public static final int BULLET_DURATION = 2 * 20;

	public static final String identityTag = "boss_lavacannon";
	private LivingEntity mSamwellMob;
	private Samwell mSamwell;
	private int mPhase;
	private Player mTarget;

	private boolean mShot;
	private List<Player> mHitMap;

	private PartialParticle mPWarning;

	private String SPELL_NAME = "Lava Cannon";

	public static BossAbilityGroup deserialize(Plugin plugin, LivingEntity boss) throws Exception {
		return new LavaCannonBoss(plugin, boss);
	}

	public LavaCannonBoss(Plugin plugin, LivingEntity boss) {
		super(plugin, identityTag, boss);
		Team darkRedTeam = ScoreboardUtils.getExistingTeamOrCreate("DarkRed", NamedTextColor.DARK_RED);
		darkRedTeam.addEntry(boss.getUniqueId().toString());
		boss.setGlowing(true);

		// Get nearest entity called Samwell.
		List<LivingEntity> witherSkeletons = EntityUtils.getNearbyMobs(mBoss.getLocation(), 100, EnumSet.of(EntityType.WITHER_SKELETON));
		for (LivingEntity mob : witherSkeletons) {
			if (mob.getScoreboardTags().contains(Samwell.identityTag)) {
				mSamwellMob = mob;
				mSamwell = BossUtils.getBossOfClass(mSamwellMob, Samwell.class);
				break;
			}
		}

		if (mSamwell == null) {
			MMLog.warning("LavaCannonBoss: Samwell wasn't found! (This is a bug)");
			return;
		}

		mPhase = mSamwell.mPhase;
		mTarget = EntityUtils.getNearestPlayer(mBoss.getLocation(), 100);
		mPWarning = new PartialParticle(Particle.REDSTONE, mBoss.getLocation(), 50, 1, 1, 1, 0.1, ORANGE);
		mHitMap = new ArrayList<>();

		List<Spell> passives = Arrays.asList(
			new SpellBlockBreak(mBoss) // This is going to be interesting
		);

		super.constructBoss(SpellManager.EMPTY, passives, 100, null);

		new BukkitRunnable() {
			int mT = 0;
			Vector mVector;

			@Override public void run() {
				if (mT <= chargeTime(mPhase) * 0.6) {
					mVector = LocationUtils.getDirectionTo(mTarget.getLocation(), mBoss.getLocation());
					Location loc = mBoss.getLocation();
					loc.setYaw((float) (Math.atan2(mVector.getZ(), mVector.getX()) * 180.0 / Math.PI));
					mBoss.teleport(loc);
				}

				if (mT % (chargeTime(mPhase) / 6) == 0) {
					warningParticles(mBoss.getLocation(), mVector);
				}

				if (mT >= chargeTime(mPhase)) {
					mBoss.getWorld().playSound(mBoss.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 5, 0);
					mBoss.getWorld().playSound(mBoss.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 5, 1);
					mShot = true;
					launch(mVector);
					this.cancel();
					return;
				}

				if (mPhase != mSamwell.mPhase || mSamwell.mDefeated) {
					mBoss.remove();
					this.cancel();
				}

				mT++;
			}
		}.runTaskTimer(mPlugin, 0, 1);
	}

	private void launch(Vector dir) {
		List<Player> players = PlayerUtils.playersInRange(mBoss.getLocation(), 100, true);
		double hitbox = mBoss.getHeight() / 2;
		double velocity = 1.5;

		new BukkitRunnable() {
			int mT = 0;
			BoundingBox mBox = BoundingBox.of(LocationUtils.getEntityCenter(mBoss), hitbox, hitbox + 0.5, hitbox);

			@Override public void run() {
				mBox.shift(dir.clone().setY(0).normalize().multiply(velocity));
				Location loc = mBox.getCenter().toLocation(mBoss.getWorld());
				for (Player player : players) {
					if (player.getBoundingBox().overlaps(mBox)) {
						hit(loc, player);
					}
				}

				mT++;
				Location teleportLoc = loc.clone();
				teleportLoc.setY(mBoss.getLocation().getY());
				mBoss.teleport(teleportLoc);

				if (mT >= BULLET_DURATION || mPhase != mSamwell.mPhase || mSamwell.mDefeated) {
					mBoss.remove();
					this.cancel();
				}
			}
		}.runTaskTimer(mPlugin, 0, 1);
	}

	private void warningParticles(Location origin, Vector dir) {
		Location particleLoc = origin.clone();
		new BukkitRunnable() {
			int mT = 0;

			@Override public void run() {
				mT += 1;
				particleLoc.add(dir.clone().setY(0).normalize());
				mPWarning.location(particleLoc).spawnAsBoss();

				if (mT >= 300 || mShot || mPhase != mSamwell.mPhase) {
					this.cancel();
				}
			}
		}.runTaskTimer(mPlugin, 0, 1);
	}

	private void hit(Location loc, Player player) {
		if (!mHitMap.contains(player)) {
			MovementUtils.knockAway(loc, player, 3f, 0.5f);
			mBoss.getWorld().playSound(mBoss.getLocation(), Sound.ENTITY_WITHER_BREAK_BLOCK, 5, 0.5f);
			mBoss.getWorld().playSound(mBoss.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, 5, 0.5f);
			mHitMap.add(player);
			BossUtils.bossDamagePercent(mBoss, player, 0.55, null, false, SPELL_NAME);
		}
	}

	public static int chargeTime(int phase) {
		if (phase <= 3) {
			return 5 * 20;
		} else {
			return 70;
		}
	}
}
