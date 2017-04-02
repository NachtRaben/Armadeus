package com.nachtraben.commandapi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by NachtRaben on 2/4/2017.
 */
public class CommandBase {

	private static final Logger logger = LoggerFactory.getLogger(CommandBase.class);

	private Map<String, List<Command>> COMMANDS;
	private ExecutorService executor;

	private List<CommandEventHandler> eventHandlers;

	public CommandBase() {
		COMMANDS = new HashMap<>();
		eventHandlers = new ArrayList<>();
		ThreadGroup group = new ThreadGroup("Command Threads");
		executor = Executors.newCachedThreadPool(r -> new Thread(group, r, "CommandThread-" + group.activeCount()));
	}

	public void registerCommands(Object object) {
		if (object instanceof Command) {
			Command command = (Command) object;
			List<Command> commands = COMMANDS.computeIfAbsent(command.name, list -> new ArrayList<>());
			// TODO: Command overlapping.
			commands.add(command);
			for (String alias : command.aliases) {
				List<Command> aliasedCommands = COMMANDS.computeIfAbsent(alias, list -> new ArrayList<>());
				aliasedCommands.add(command);
			}
			logger.info("Added command, " + command.toString());
		}

		// Process any annotated commands
		for (Method method : object.getClass().getMethods()) {
			if (method.isAnnotationPresent(Cmd.class)) {
				Cmd cmd = method.getAnnotation(Cmd.class);
				AnnotatedCommand command = new AnnotatedCommand(cmd, object, method);
				List<Command> commands = COMMANDS.computeIfAbsent(command.name, list -> new ArrayList<>());
				// TODO: Command overlapping.
				commands.add(command);
				for (String alias : command.aliases) {
					List<Command> aliasedCommands = COMMANDS.computeIfAbsent(alias, list -> new ArrayList<>());
					aliasedCommands.add(command);
				}
				logger.info("Added command, " + command.toString());
			}
		}
	}

	public Future<CommandEvent> execute(final CommandSender sender, final String command, final String[] arguments) {
		return executor.submit(() -> {
			CommandEvent event;
			Map<String, String> flags = new HashMap<>();
			ArrayList<String> processedArgs = new ArrayList<>();

			for (String s : arguments) {
				if (Command.flagsRegex.matcher(s).find()) {
					for (char c : s.substring(1).toCharArray()) flags.put(String.valueOf(c), null);
				} else if (Command.flagRegex.matcher(s).find()) {
					flags.put(s.substring(2), null);
				} else if (Command.flagWithValue.matcher(s).find()) {
					flags.put(s.substring(2, s.indexOf("=")), s.substring(s.indexOf("=")));
				} else processedArgs.add(s);
			}

			String[] args = new String[processedArgs.size()];
			processedArgs.toArray(args);

			Command canidate = getCommandMatch(sender, command, args);
			if (canidate != null) {
				//TODO: Flag validation
				//TODO: Variable conversion
				Map<String, String> mapargs = canidate.processArgs(args);
				try {
					canidate.run(sender, mapargs, flags);
					event = new CommandEvent(sender, canidate, CommandEvent.Result.SUCCESS);
				} catch (Exception e) {
					event = new CommandEvent(sender, canidate, CommandEvent.Result.EXCEPTION, e);
				}
			} else {
				event = new CommandEvent(sender, null, CommandEvent.Result.COMMAND_NOT_FOUND);
			}
			for(CommandEventHandler handler : eventHandlers) {
				handler.handle(event);
			}
			return event;
		});
	}

	private Command getCommandMatch(CommandSender sender, String command, String[] arguments) {
		List<Command> canidates = this.COMMANDS.get(command);
		if (canidates != null) {
			for (Command canidate : canidates) {
				if (canidate.pattern.matcher(arrayToString(arguments)).find())
					return canidate;
			}
		}
		return null;
	}

	public void addEventListener(CommandEventHandler handler) {
		this.eventHandlers.add(handler);
	}

	private String arrayToString(String[] args) {
		if (args.length == 0) return "";
		StringBuilder sb = new StringBuilder();
		for (String s : args) {
			sb.append(s).append(" ");
		}
		sb.replace(sb.length() - 1, sb.length(), "");
		return sb.toString();
	}

	public Map<String, List<Command>> getCommands() {
		return new HashMap<>(COMMANDS);
	}

}
