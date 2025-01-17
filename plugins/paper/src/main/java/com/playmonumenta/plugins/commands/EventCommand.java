package com.playmonumenta.plugins.commands;


import com.playmonumenta.plugins.events.MonumentaEvent;
import com.playmonumenta.plugins.seasonalevents.MonumentaContent;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.TextArgument;
import dev.jorel.commandapi.executors.CommandExecutor;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class EventCommand extends GenericCommand {
	public static final String COMMAND = "event";

	public static void register() {
		CommandPermission perms = CommandPermission.fromString("monumenta.command.event");

		List<String> labels = new ArrayList<>();
		for (MonumentaContent content : MonumentaContent.values()) {
			labels.add(content.getLabel());
		}
		String[] arr = labels.toArray(new String[labels.size()]);
		Argument<?> contentArgs = new TextArgument("event").replaceSuggestions(ArgumentSuggestions.strings((info) -> arr));

		List<Argument<?>> arguments = new ArrayList<>();
		arguments.add(new EntitySelectorArgument.OnePlayer("player"));
		arguments.add(contentArgs);
		new CommandAPICommand(COMMAND)
			.withPermission(perms)
			.withArguments(arguments)
			.executes((sender, args) -> {
				Bukkit.getPluginManager().callEvent(new MonumentaEvent((Player) args[0], (String) args[1]));
			})
			.register();
	}
}
