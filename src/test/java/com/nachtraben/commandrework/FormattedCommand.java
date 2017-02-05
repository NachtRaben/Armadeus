package com.nachtraben.commandrework;

import com.nachtraben.command.CmdSender;
import com.nachtraben.command.sender.CommandSender;
import com.nachtraben.command.sender.ConsoleCommandSender;
import com.nachtraben.command.sender.UserCommandSender;
import com.nachtraben.log.LogManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.nachtraben.utils.Utils.format;

/**
 * Created by NachtRaben on 2/4/2017.
 */
public class FormattedCommand {

    private Command command;

    private MethodType methodType;
    private Method method;
    private Object methodHolder;

    private CmdSender target = CmdSender.BOTH;

    private CommandArg[] arguments;
    private ArrayList<String> flags;

    public boolean endsAsRest = false;
    public boolean endsAsOptional = false;


    public FormattedCommand(Command command, Method method, Object origin) {
        this.command = command;
        this.method = method;
        this.methodHolder = origin;
        this.flags = getFlagNames(this.command.flags());
    }

    boolean buildAndValidate() {
        if (!validateMethod()) {
            //TODO: Error handling, not a valid method for this command system.
            return false;
        }

        if (!validateFormat()) {
            //TODO: Error handling, not a valid format for a command.
            return false;
        }
        return true;
    }

    public CommandResult call(CommandSender sender, String[] args, String[] flags) {
        CommandResult result = new CommandResult();
        result.setArgsAndFlags(args, flags);
        long start = System.nanoTime();
        Map<String, String> filteredFlags = new HashMap<>();
        if (!processFlags(flags, filteredFlags)) {
            result.setResult(CommandResult.Result.INVALID_FLAGS);
            return result;
        }
        Map<String, String> filteredArguments = new HashMap<>();
        if (!processArgs(args, filteredArguments)) {
            result.setResult(CommandResult.Result.INVALID_ARGS);
            return result;
        }
        long end = System.nanoTime();
        LogManager.TOHSAKA.debug(format("%s took %sns to process flags and args. %sms.", command.name().toUpperCase(), end - start, TimeUnit.NANOSECONDS.toMillis(end - start)));
        //TODO: Process CommandSender requirement

        try {
            Object o = null;
            switch (methodType) {
                case SENDER_ARGS_FLAGS:
                    o = method.invoke(methodHolder, sender, filteredArguments, filteredFlags);
                    break;
                case SENDER_ARGS:
                    o = method.invoke(methodHolder, sender, filteredArguments);
                    break;
                case SENDER:
                    o = method.invoke(methodHolder, sender);
                    break;
            }
            if (o != null) {
                /* Process boolean return type */
                if (o instanceof Boolean) {
                    if ((boolean) o) {
                        result.setResult(CommandResult.Result.SUCCESS);
                    } else {
                        result.setResult(CommandResult.Result.FAILURE);
                    }
                }
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            result.setStack(e);
            result.setResult(CommandResult.Result.EXCEPTION);
        }
        return result;

    }


    private boolean processArgs(String[] args, Map<String, String> dest) {
        for (int i = 0; i < args.length; i++) {
            CommandArg cmdarg = arguments[i];
            if (!cmdarg.isDynamic) {
                if (!args[i].toLowerCase().equals(cmdarg.name.toLowerCase())) {
                    return false;
                }
            } else {
                if (cmdarg.isRest) {
                    dest.put(cmdarg.name.toLowerCase(), Arrays.toString(Arrays.copyOfRange(args, i, args.length)));
                    return true;
                } else {
                    dest.put(cmdarg.name.toLowerCase(), args[i]);
                }
            }
        }
        return true;
    }

    private boolean processFlags(String[] args, Map<String, String> dest) {
        for (String s : args) {
            if (s.startsWith("--") && s.contains("=")) {
                String temp = s.replace("--", "");
                String[] tokens = temp.split("=");
                if (flags.contains(tokens[0]) && tokens.length >= 2) {
                    dest.put(tokens[0], argsToString(Arrays.copyOfRange(tokens, 1, tokens.length)));
                } else {
                    return false;
                }
            } else if (s.startsWith("--")) {
                String temp = s.replace("--", "");
                if (flags.contains(temp)) {
                    dest.put(temp, null);
                } else {
                    return false;
                }
            } else if (s.startsWith("-")) {
                String temp = s.replace("-", "");
                for (char c : temp.toCharArray()) {
                    if (flags.contains(String.valueOf(c))) {
                        dest.put(String.valueOf(c), null);
                    } else {
                        return false;
                    }
                }
            }
        }
        return true;
    }


    private ArrayList<String> getFlagNames(String[] args) {
        ArrayList<String> result = new ArrayList<>();
        for (String s : args) {
            /* Process '--flagasword' */
            if (s.startsWith("--") && s.contains("=")) {
                String temp = s.replace("--", "");
                String[] tokens = temp.split("=");
                result.add(tokens[0]);
            }
            /* Process '--flagwith=value' */
            else if (s.startsWith("--")) {
                String temp = s.replace("--", "");
                result.add(temp);
            }
            /* Process '-fbga' */
            else if (s.startsWith("-")) {
                String temp = s.replace("-", "");
                for (char c : temp.toCharArray()) {
                    result.add(String.valueOf(c));
                }
            }
        }
        return result;
    }

    private String argsToString(String[] args) {
        if (args.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        for (String s : args) {
            sb.append(s).append(" ");
        }
        sb.replace(sb.length() - 1, sb.length(), "");
        return sb.toString();
    }

    private boolean validateMethod() {
        Class[] parameters = method.getParameterTypes();
        if (parameters.length == 1) {
            if (parameters[0].isAssignableFrom(CommandSender.class)) {
                methodType = MethodType.SENDER;
            } else {
                System.out.println(parameters[0].getName());
                System.out.println(CommandSender.class.getName());
                return false;
            }
        } else if (parameters.length == 2) {
            if (parameters[0].isAssignableFrom(CommandSender.class) && parameters[1].isAssignableFrom(Map.class)) {
                methodType = MethodType.SENDER_ARGS;
            } else {
                return false;
            }
        } else if (parameters.length == 3) {
            if (parameters[0].isAssignableFrom(CommandSender.class) && parameters[1].isAssignableFrom(Map.class) && parameters[2].isAssignableFrom(Map.class)) {
                methodType = MethodType.SENDER_ARGS_FLAGS;
            } else {
                return false;
            }
        }
        String senderName = parameters[0].getName();
        if(senderName.equals(UserCommandSender.class.getName())) target = CmdSender.USER;
        else if(senderName.equals(ConsoleCommandSender.class.getName())) target = CmdSender.CONSOLE;
        return true;
    }

    private boolean validateFormat() {
        /**
         * Rules:
         *      There can only be 1 optional arg on the end
         *      There can only be 1 rest arg on the end
         */
        String[] args = command.format().split(" ");
        int start = args[0].toLowerCase().equals(command.name().toLowerCase()) ? 1 : 0;
        arguments = new CommandArg[start == 1 ? args.length - 1 : args.length];
        if (args.length >= 1) {
            for (int i = start; i < args.length; i++) {
                String name = args[i];
                CommandArg arg;
                if (name.startsWith("<") && name.endsWith(">")) {
                    name = name.replace("<", "").replace(">", "");
                    arg = new CommandArg(name, true, true, false);
                } else if (name.startsWith("[") && name.endsWith("]")) {
                    name = name.replace("[", "").replace("]", "");
                    arg = new CommandArg(name, true, false, false);
                    endsAsOptional = true;
                    if (i != args.length - 1) {
                        /* Error checking to see if [] arg is not the last arg. */
                        return false;
                    }
                } else if (name.startsWith("{") && name.endsWith("}")) {
                    name = name.replace("{", "").replace("}", "");
                    arg = new CommandArg(name, true, true, true);
                    endsAsRest = true;
                    if (i != args.length - 1) {
                        /* Error checking to see if {} is not the last arg. */
                        return false;
                    }
                } else {
                    arg = new CommandArg(name, false, true, false);
                }
                arguments[start == 1 ? i - 1 : i] = arg;
            }
        }
        return true;
    }

    public int getArgCount() {
        return arguments.length;
    }

    public CommandArg[] getArguments() {
        return arguments;
    }

    public CmdSender getCommandTarget() {
        return target;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("FormattedCommand( name::").append(command.name());
        if (command.aliases().length > 0) sb.append(", aliases::").append(Arrays.toString(command.aliases()));
        if (command.flags().length > 0) sb.append(", flags::").append(Arrays.toString(command.flags()));
        if (command.format().length() > 0) sb.append(", format::").append(command.format());
        if (command.description().length() > 0) sb.append(", description::").append(command.description());
        sb.append(", target::").append(target.toString());
        sb.append(" )");
        return sb.toString();
    }

}

enum MethodType {
    SENDER_ARGS_FLAGS,
    SENDER_ARGS,
    SENDER
}
