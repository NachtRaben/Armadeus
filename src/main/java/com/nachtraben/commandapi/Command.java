package com.nachtraben.commandapi;

import com.nachtraben.core.utils.StringUtils;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by NachtRaben on 3/7/2017.
 */
public abstract class Command {

    String name;
    String format;
    String description;
    String[] aliases;
    String[] flags;
    CommandArg[] commandArgs;
    CommandFlag[] commandFlags;
    Pattern pattern;

    private static final String requiredRegex = "\\S+";

    private static final String optionalRegex = "(\\s+\\S+)?";
    private static final String firstOptionalRegex = "(\\S+)?";

    private static final String restRegex = "(?!\\s*$).+";
    private static final String optionalRestRegex = "(\\s+" + restRegex + ")?";
    private static final String firstOptionalRestRegex = "(" + restRegex + ")?";

    static final Pattern flagsRegex = Pattern.compile("^-\\w+$", 0);
    static final Pattern flagRegex = Pattern.compile("^--\\w+$");
    static final Pattern flagWithValue = Pattern.compile("^--\\w+=\\S+$", 0);

    public Command(String name, String format) {
        this.name = name;
        this.format = format;
        aliases = new String[]{};
        flags = new String[]{};
        commandArgs = new CommandArg[]{};
        validateFormat();
        buildPattern();
        buildFlags();
    }

    private void validateFormat() throws CommandCreationException {
        String[] tokens = format.split(" ");
        tokens = Arrays.stream(tokens).filter(s -> (!s.isEmpty())).toArray(String[]::new);
        if (tokens.length > 0 && tokens[0].equals(name)) {
            tokens = Arrays.copyOfRange(tokens, 1, tokens.length);
        }
        if (tokens.length > 0) {
            commandArgs = new CommandArg[tokens.length];
            for (int i = 0; i < tokens.length; i++) {
                String arg = tokens[i];
                if (arg.charAt(0) == '<' && arg.charAt(arg.length() - 1) == '>') {
                    commandArgs[i] = new CommandArg(arg.substring(1, arg.length() - 1), true, true, false);
                } else if (arg.charAt(0) == '[' && arg.charAt(arg.length() - 1) == ']') {
                    commandArgs[i] = new CommandArg(arg.substring(1, arg.length() - 1), true, false, false);
                    if (i != tokens.length - 1) {
                        throw new CommandCreationException(this, "[] statements can only be at the end of the format.");
                    }
                } else if (arg.charAt(0) == '{' && arg.charAt(arg.length() - 1) == '}') {
                    commandArgs[i] = new CommandArg(arg.substring(1, arg.length() - 1), true, true, true);
                    if (i != tokens.length - 1) {
                        throw new CommandCreationException(this, "{} statements can only be at the end of the format.");
                    }
                } else if (arg.charAt(0) == '(' && arg.charAt(arg.length() - 1) == ')') {
                    commandArgs[i] = new CommandArg(arg.substring(1, arg.length() - 1), true, false, true);
                    if (i != tokens.length - 1) {
                        throw new CommandCreationException(this, "() statements can only be at the end of the format.");
                    }
                } else {
                    commandArgs[i] = new CommandArg(arg, false, true, false);
                }
            }
        }
    }

    private void buildPattern() {
        StringBuilder sb = new StringBuilder();
        sb.append("^"); // Append line start
        if (commandArgs.length >= 1) {
            for (int i = 0; i < commandArgs.length; i++) {
                CommandArg arg = commandArgs[i];
                if (!arg.isRequired) {
                    if (i == 0) {
                        if(arg.isRest) {
                            sb.append(firstOptionalRestRegex);
                        } else {
                            sb.append(firstOptionalRegex);
                        }
                    } else if (arg.isRest) {
                        sb.append(optionalRestRegex);
                    } else {
                        sb.append(optionalRegex); // Process [] tag
                    }
                } else {
                    if (i > 0) {
                        sb.append("\\s+"); // Add space if not first tag
                    }
                    if (arg.isDynamic) {
                        if (arg.isRest) {
                            sb.append(restRegex); // Process {} tag
                        } else {
                            sb.append(requiredRegex); // Process <> tag
                        }
                    } else {
                        sb.append(arg.name);
                    }
                }
            }
        }
        sb.append("$");
        pattern = Pattern.compile(sb.toString(), 0);
    }

    private void buildFlags() {
        List<CommandFlag> temp = new ArrayList<>();
        for (String s : flags) {
            if (flagsRegex.matcher(s).find()) {
                for (char c : s.substring(1).toCharArray()) {
                    temp.add(new CommandFlag(String.valueOf(c), false));
                }
            } else if (flagRegex.matcher(s).find()) {
                temp.add(new CommandFlag(s.substring(2), false));
            } else if (flagWithValue.matcher(s).find()) {
                temp.add(new CommandFlag(s.substring(2, s.indexOf("=")), true));
            }
        }

        commandFlags = temp.toArray(new CommandFlag[temp.size()]);
    }

    public Map<String, String> processArgs(String[] args) {
        Map<String, String> result = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            CommandArg cmdarg = commandArgs[i];
            if (cmdarg.isDynamic) {
                if (cmdarg.isRest) {
                    result.put(cmdarg.name.toLowerCase(), StringUtils.arrayToString(Arrays.copyOfRange(args, i, args.length)));
                    return result;
                } else {
                    result.put(cmdarg.name.toLowerCase(), args[i]);
                }
            }
        }
        return result;
    }

    public abstract void run(CommandSender sender, Map<String, String> args, Map<String, String> flags);

    public String helpString() {
        return "**Name**: " + name + "\t**Usage**: " + name + " " + format + "\t**Description**: " + description;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(" +
                "name=" + name + ", " +
                "format=" + format + ", " +
                "description=" + description + ", " +
                "aliases=" + Arrays.toString(aliases) + ", " +
                "flags=" + Arrays.toString(flags) +
                ")";
    }

    public String getName() {
        return name;
    }

    public String getFormat() {
        return format;
    }

    public String getDescription() {
        return description;
    }

    public String[] getAliases() {
        return aliases;
    }

    public String[] getFlags() {
        return flags;
    }

    class CommandArg {
        String name = "";
        boolean isDynamic = true;
        boolean isRequired = false;
        boolean isRest = false;

        CommandArg(String name, boolean isDynamic, boolean isRequired, boolean isRest) {
            this.name = name;
            this.isDynamic = isDynamic;
            this.isRequired = isRequired;
            this.isRest = isRest;
        }
    }

    class CommandFlag {
        String name = "";
        boolean needsValue = false;

        CommandFlag(String name, boolean needsValue) {
            this.name = name;
            this.needsValue = needsValue;
        }
    }
}
