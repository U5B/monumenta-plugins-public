package com.playmonumenta.plugins.commands;

import com.playmonumenta.plugins.utils.ItemStatUtils;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ColossalifyHeldItem extends GenericCommand {
	public static void register() {
		registerPlayerCommand("colossalifyhelditem", "monumenta.command.colossalifyhelditem", ColossalifyHeldItem::run);
	}

	private static void run(CommandSender sender, Player player) throws WrapperCommandSyntaxException {
		ItemStatUtils.addInfusion(player.getItemInHand(), ItemStatUtils.InfusionType.COLOSSAL, 1, player.getUniqueId());
		ItemStatUtils.generateItemStats(player.getItemInHand());
	}
}
