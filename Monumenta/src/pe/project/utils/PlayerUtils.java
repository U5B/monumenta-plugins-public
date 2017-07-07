package pe.project.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import net.md_5.bungee.api.ChatColor;
import pe.project.Constants;

public class PlayerUtils {
	public static boolean isCritical(Player player) {
		return player.getFallDistance() > 0.0F &&
        !player.isOnGround() &&
        !player.isInsideVehicle() &&
        !player.hasPotionEffect(PotionEffectType.BLINDNESS) &&
        player.getLocation().getBlock().getType() != Material.LADDER &&
        player.getLocation().getBlock().getType() != Material.VINE;
	}
	
	public static boolean hasLineOfSight(Player player, LivingEntity mob) {
		//	We want to check against two things since the players view isn't square,
		//	first we'll test against the X (Yaw).
		final double xDotMax = 0.65;
		
		Vector forwardX = player.getEyeLocation().getDirection().setY(0).normalize();
		Vector toMobX = mob.getEyeLocation().subtract(player.getEyeLocation()).toVector().setY(0).normalize();
		
		double xz = toMobX.dot(forwardX);
		boolean yawLos = xz > xDotMax;
		
		//	Then we'll test against the Y (Pitch)
		final double yDotMax = 0.70;
		final double yDotMin = -0.55;
		
		Vector forwardY = player.getEyeLocation().getDirection().normalize();
		Vector toMobY = mob.getEyeLocation().subtract(player.getEyeLocation()).toVector().normalize();
		
		double valY = toMobY.subtract(forwardY).getY();
		
		boolean pitchLos = valY < yDotMax && valY > yDotMin;
		
		return yawLos && pitchLos;
	}
	
	public static void awardStrike(Player player, String reason) {
		int strikes = ScoreboardUtils.getScoreboardValue(player, "Strikes");
		strikes++;
		ScoreboardUtils.setScoreboardValue(player, "Strikes", strikes);
		
		Location loc = player.getLocation();
		String OOBLoc = "[" + (int)loc.getX() + ", " + (int)loc.getY() + ", " + (int)loc.getZ() + "]";
		
		player.sendMessage(ChatColor.RED + "You've recieved a strike for "  + reason + " Location " + OOBLoc);
		player.sendMessage(ChatColor.YELLOW + "If you feel that this strike is unjustified feel free to send a message and screenshot of this to a moderator on the Discord.");
		
		player.teleport(new Location(player.getWorld(), Constants.SPAWN_POINT.mX, Constants.SPAWN_POINT.mY, Constants.SPAWN_POINT.mZ));
	}
}
