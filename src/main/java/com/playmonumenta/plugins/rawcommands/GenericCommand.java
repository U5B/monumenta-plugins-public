package com.playmonumenta.plugins.rawcommands;

import java.util.Collection;
import java.util.LinkedHashMap;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import io.github.jorelali.commandapi.api.CommandAPI;
import io.github.jorelali.commandapi.api.CommandPermission;
import io.github.jorelali.commandapi.api.arguments.Argument;
import io.github.jorelali.commandapi.api.arguments.EntitySelectorArgument;
import io.github.jorelali.commandapi.api.arguments.EntitySelectorArgument.EntitySelector;

public class GenericCommand {
	@FunctionalInterface
	protected interface PlayerCommandExecutor {
		/**
		 * Called on each player targeted by the command
		 *
		 * @param sender Sender of the command
		 * @param player Target of the command
		 */
		void run(CommandSender sender, Player player);
	}

	@FunctionalInterface
	protected interface EntityCommandExecutor {
		/**
		 * Called on each player targeted by the command
		 *
		 * @param sender Sender of the command
		 * @param entity Target of the command
		 */
		void run(CommandSender sender, Entity entity);
	}

	@SuppressWarnings("unchecked")
	protected static void registerPlayerCommand(String command, String permission, PlayerCommandExecutor exec) {
		CommandPermission perms = CommandPermission.fromString(permission);

		LinkedHashMap<String, Argument> arguments = new LinkedHashMap<>();
		arguments.put("players", new EntitySelectorArgument(EntitySelector.MANY_PLAYERS));
		CommandAPI.getInstance().register(command,
		                                  perms,
		                                  arguments,
		                                  (sender, args) -> {
		                                      for (Player player : (Collection<Player>)args[0]) {
												  exec.run(sender, player);
		                                      }
		                                  }
		);
	}

	@SuppressWarnings("unchecked")
	protected static void registerEntityCommand(String command, String permission, EntityCommandExecutor exec) {
		CommandPermission perms = CommandPermission.fromString(permission);

		LinkedHashMap<String, Argument> arguments = new LinkedHashMap<>();
		arguments.put("entities", new EntitySelectorArgument(EntitySelector.MANY_ENTITIES));
		CommandAPI.getInstance().register(command,
		                                  perms,
		                                  arguments,
		                                  (sender, args) -> {
		                                      for (Entity entity : (Collection<Entity>)args[0]) {
												  exec.run(sender, entity);
		                                      }
		                                  }
		);
	}

	protected static void error(CommandSender sender, String msg) {
		sender.sendMessage(ChatColor.RED + msg);
	}
}
