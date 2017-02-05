package com.nachtraben.command;

import com.nachtraben.command.sender.CommandSender;
import com.nachtraben.command.sender.ConsoleCommandSender;
import com.nachtraben.command.sender.UserCommandSender;
import com.nachtraben.log.LogManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.nachtraben.utils.Utils.format;

public class FmtCommand {

    private Cmd cmd;
    private Method method;
    private Object methodHolder;

    public CmdArg[] args;
    public boolean realCmd = true;

    private String name;
    public int requiredArgCount = 0;
    public CmdSender target = CmdSender.BOTH;

    private static final String requiredRegex = "\\S+";
    private static final String optionalRegex = "(\\s+\\S)?";
    private static final String restRegex = "(?!\\s*$).+";
    private static final String flagsRegex = "(\\s-\\S+)?";
    private static final String flagRegex = "(\\s--\\s+)?";

    // Constructor
    public FmtCommand(Cmd anotCmd, Method method, Object origin) {
        this.cmd = anotCmd;
        this.methodHolder = origin;
        this.method = method;

        // Figure out if this is a proper method(CommandSender sender, Map<String, String>) method
        if (method.getParameterTypes().length == 2) {
            // Figure out who we're aiming for
            Class[] params = method.getParameterTypes();
            if (params[0].getName().equals(UserCommandSender.class.getName())) target = CmdSender.USER;
            else if (params[0].getName().equals(ConsoleCommandSender.class.getName())) target = CmdSender.CONSOLE;
            else if (params[0].getName().equals(CommandSender.class.getName())) target = CmdSender.BOTH;
            // Figure out if they meet the method requirements
            if (!params[1].isAssignableFrom(Map.class)) {
                realCmd = false;
                return;
            }
        } else {
            realCmd = false;
            return;
        }

        // Set up CommandArgs array and determine arg types
        String format = cmd.format();
        if (CmdBase.COMMAND_INIT_CHARS.contains(format.charAt(0))) format = format.substring(1);
        String[] formSplit = format.split(" ");
        name = formSplit[0].toLowerCase();
        if (formSplit.length > 1) {
            args = new CmdArg[formSplit.length - 1];
            for (int i = 1; i < formSplit.length; i++) {
                String arName = formSplit[i];
                CmdArg arg = new CmdArg();
                if (arName.startsWith("<")) {
                    arName = arName.replace("<", "").replace(">", "");
                    arg.argName = arName;
                    arg.dynamic = true;
                    arg.required = true;
                    requiredArgCount++;
                } else if (arName.startsWith("[")) {
                    arName = arName.replace("[", "").replace("]", "");
                    arg.argName = arName;
                    arg.dynamic = true;
                    arg.required = false;
                    if (i != formSplit.length - 1) {
                        realCmd = false;
                        return;
                    }
                } else if (arName.startsWith("{")) {
                    arName = arName.replace("{", "").replace("}", "");
                    arg.argName = arName;
                    arg.dynamic = true;
                    arg.rest = true;
                    arg.required = true;
                    requiredArgCount++;
                    if (i != formSplit.length - 1) {
                        realCmd = false;
                        return;
                    }
                } else {
                    arg.argName = arName;
                    arg.dynamic = false;
                    arg.required = true;
                    requiredArgCount++;
                }
                args[i - 1] = arg;
            }
        }
    }

    // Utility
    public boolean call(CommandSender sender, String command, String[] args) {
        if (!this.realCmd) return false;
        Map<String, String> arguments = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            CmdArg carg = this.args[i];
            if (!carg.dynamic) {
                if (!carg.argName.toLowerCase().equals(args[i].toLowerCase())) return false;
            } else {
                if (carg.rest) {
                    arguments.put(carg.argName.toLowerCase(), argsToString(Arrays.copyOfRange(args, i, args.length)));
                    break;
                } else {
                    arguments.put(carg.argName.toLowerCase(), args[i]);
                }
            }
        }
        CmdSender sendee = sender instanceof UserCommandSender ? CmdSender.USER : sender instanceof ConsoleCommandSender ? CmdSender.CONSOLE : CmdSender.BOTH;
        if (sendee.equals(CmdSender.BOTH)) {
            LogManager.TOHSAKA.warn("Command executed from unidentifiable sender of Type[%s].", sender.getClass().getSimpleName());
            return false;
        }
        if (target != CmdSender.BOTH) {
            if (!sendee.equals(target)) {
                sender.sendMessage("Hold it Marco, that command can only be executed as a " + target.name() + ".");
                return false;
            }
        }
        if (sendee.equals(CmdSender.USER)) {
            if (!sender.hasPermission(this.cmd.permission())) {
                sender.sendMessage("Sorry Jim, you are not competent enough for that command.");
                return false;
            }
        }
        LogManager.TOHSAKA.info("%s issued bot command %s.", sender.getName(), this.name);
        try {
            Object o = method.invoke(methodHolder, sender, arguments);
            if (o != null) {
                if (o instanceof Boolean) {
                    if (((boolean) o)) return true;
                    else {
                        // TODO: Send command usage help
                        LogManager.TOHSAKA.info("%s failed to run bot command %s with args %s.", sender.getName(), this.name, arguments.toString());
                        return false;
                    }
                }
            }
        } catch (InvocationTargetException | IllegalAccessException e) {
            LogManager.TOHSAKA.error(format("Exception thrown when %s attempted to run command { %s } with args { %s }.", sender.getName(), this.getCmd().format(), argsToString(args)), e);
            return false;
        }
        return true;
    }

    public String argsToString(String[] args) {
        if (args.length >= 1) {
            StringBuilder sb = new StringBuilder();
            for (String s : args) {
                sb.append(s).append(" ");
            }
            sb.replace(sb.length() - 1, sb.length(), "");
            return sb.toString();
        } else {
            return null;
        }
    }

    // Setters

    // Getters
    public String toString() {
        return cmd.format() + " : " + cmd.description();
    }

    public String getRegex() {
        if (args == null) args = new CmdArg[0];
        StringBuilder sb = new StringBuilder();
        boolean lastRequirement = false;

        for (int i = 0; i < args.length; i++) {
            CmdArg ca = args[i];
            if (lastRequirement) {
                if (!ca.required) {
                    continue;
                }
                //TODO: Only happens if invalid formatting.
                LogManager.TOHSAKA.debug("Reach a last requirement that wasn't required, this should be an error!");
                lastRequirement = false;
            } else {
                if (!ca.required) {
                    //Parse a [] arg
                    lastRequirement = true;
                    sb.append(optionalRegex);
                } else {
                    if (i > 0) {
                        //Since not fist argument, add a space and prepare for next statement
                        sb.append("\\s+");
                    }
                    if (ca.dynamic) {
                        if (ca.rest) {
                            //Parse a {} arg
                            sb.append(restRegex);
                        } else {
                            //Parse a <> arg
                            sb.append(requiredRegex);
                        }
                    } else {
                        sb.append("").append(ca.argName).append("");
                    }
                }
            }
        }
        return sb.toString();
    }

    public Cmd getCmd() {
        return cmd;
    }

    public int getArgCount() {
        return this.args.length;
    }

}
