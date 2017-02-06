package com.nachtraben.commandrework;

import com.nachtraben.command.CmdSender;
import com.nachtraben.command.sender.CommandSender;
import com.nachtraben.log.LogManager;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.nachtraben.utils.Utils.format;

/**
 * Created by NachtRaben on 2/4/2017.
 */
public class CommandBase {

    private Map<String, List<FormattedCommand>> COMMANDS = new HashMap<>();
    private Map<String, String> ALIASES = new HashMap<>();

    public CommandBase() {
    }

    public void registerCommands(Object origin) {
        for (Method method : origin.getClass().getMethods()) {
            if (method.isAnnotationPresent(Command.class)) {
                Command command = method.getAnnotation(Command.class);
                List<FormattedCommand> commands = this.COMMANDS.computeIfAbsent(command.name(), k -> new ArrayList<>());

                FormattedCommand fmtcommand = new FormattedCommand(command, method, origin);
                if (!fmtcommand.buildAndValidate()) {
                    LogManager.TOHSAKA.error(format("Failed to form command %s in %s.", command.name(), command.format()));
                    //TODO: Handle command validation failure.
                    continue;
                }

                //TODO: Check for command overlaps that are probably unwanted.
                commands.add(fmtcommand);
                //TODO: Handle overlapping aliases for commands.
                for(String s : command.aliases()) {
                    ALIASES.put(s, command.name());
                }
                LogManager.TOHSAKA.info(format("Registered command { %s } in  { %s } >> %s", command.name(), origin.getClass().getSimpleName(), fmtcommand.toString()));
            }
        }
    }

    public CommandResult process(CommandSender sender, String command, String[] args) {
        CommandResult result = new CommandResult();
        ArrayList<String> finalflags = new ArrayList<>();
        ArrayList<String> finalargs = new ArrayList<>();

        for (String s : args) {
            if (s.startsWith("-") || s.startsWith("--")) finalflags.add(s);
            else finalargs.add(s);
        }

        args = new String[finalargs.size()];
        finalargs.toArray(args);
        String[] flags = new String[finalflags.size()];
        finalflags.toArray(flags);

        long start = System.nanoTime();
        FormattedCommand match = getNonRegexMatch(sender, command, args);
        long end = System.nanoTime();
        LogManager.TOHSAKA.debug(format("%s took %sns to match. %sms.", command.toUpperCase(), end - start, TimeUnit.NANOSECONDS.toMillis(end - start)));
        if (match != null) {
            result = match.call(sender, args, flags);
        } else {
            result.setResult(CommandResult.Result.UNKNOWN_COMMAND);
        }
        return result;
    }

    private FormattedCommand getNonRegexMatch(CommandSender sender, String cmd, String[] args) {
        List<FormattedCommand> results1 = new ArrayList<>();
        List<FormattedCommand> results2 = new ArrayList<>();

        List<FormattedCommand> instances = COMMANDS.get(cmd);
        if(instances == null) instances = COMMANDS.get(ALIASES.get(cmd));

        if (instances != null) {
            for (FormattedCommand command : instances) {
                int length = command.getArgCount();
                CommandArg[] arguments = command.getArguments();
                if (CmdSender.valueOf(sender).equals(command.getCommandTarget()) || command.getCommandTarget().equals(CmdSender.BOTH)) {
                    if (args.length == length) {
                        boolean matches = true;
                        for (int i = 0; i < length; i++) {
                            CommandArg arg = arguments[i];
                            if (!arg.isDynamic && !args[i].toLowerCase().equals(arg.name.toLowerCase())) {
                                matches = false;
                                break;
                            }
                        }
                        if (matches)
                            results1.add(command);
                    } else {
                        if (args.length >= args.length - 1) {
                            boolean matches = true;
                            for (int i = 0; i < length - 1; i++) {
                                CommandArg arg = arguments[i];
                                if (!arg.isDynamic && !args[i].toLowerCase().equals(arg.name.toLowerCase())) {
                                    matches = false;
                                    break;
                                }
                            }
                            if (matches) {
                                if (command.endsAsOptional && args.length == length - 1) {
                                    results2.add(command);
                                }
                                if (command.endsAsRest && args.length >= length) {
                                    results2.add(command);
                                }
                            }
                        }
                    }
                }
                if (results1.size() > 1 || results2.size() > 1) {
                    LogManager.TOHSAKA.error(format("Command { %s } has multiple instances where overlapped matching occurs, this is considered bad!\n\t%s\n\t%s", cmd, Arrays.toString(results1.toArray()), Arrays.toString(results2.toArray())));
                }
                return results1.size() > 0 ? results1.get(0) : results2.size() > 0 ? results2.get(0) : null;
            }
        }
        return null;
    }

}
