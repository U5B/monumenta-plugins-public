package com.playmonumenta.plugins.server.reset;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.server.properties.ServerProperties;
import com.playmonumenta.plugins.utils.ScoreboardUtils;

public class DailyReset {
	private static final String DAILY_PLAYER_CHANGES_COMMAND = "execute as @S at @s run function monumenta:mechanisms/daily_player_changes";
	private static BukkitRunnable mRunnable = null;

	public static void startTimer(Plugin plugin) {
		if (mRunnable == null || mRunnable.isCancelled()) {
			new BukkitRunnable() {
				int mCurVers = getDailyVersion();

				@Override
				public void run() {
					int newVers = getDailyVersion();
					if (newVers != mCurVers) {
						for (Player player : plugin.getServer().getOnlinePlayers()) {
							handle(plugin, player);
						}

						mCurVers = newVers;
					}
				}
			}.runTaskTimer(plugin, 0, 1200);
		}
	}

	private static int getDailyVersion() {
		return (int)((System.currentTimeMillis() - 25200000) / 86400000);
	}

	public static void handle(Plugin plugin, Player player) {
		if (ServerProperties.getDailyResetEnabled() && player != null) {
			//  Test to see if the player's Daily version is different than the servers.
			int dailyVersion = ScoreboardUtils.getScoreboardValue(player, "DailyVersion");
			if (dailyVersion != getDailyVersion()) {
				//  If so reset some scoreboards and message the player.
				ScoreboardUtils.setScoreboardValue(player, "DailyVersion", getDailyVersion());

				String commandStr = DAILY_PLAYER_CHANGES_COMMAND.replaceAll("@S", player.getName());
				plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), commandStr);
			}
		}
	}
}
