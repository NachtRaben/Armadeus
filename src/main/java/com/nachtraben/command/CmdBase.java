package com.nachtraben.command;

import com.nachtraben.command.sender.CommandSender;
import com.nachtraben.log.LogManager;
import com.nachtraben.utils.Utils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;

public class CmdBase {
    private Map<String, List<FmtCommand>> CMDS = new HashMap<>();
    private Map<String, Map<String, Integer>> REGEXS = new HashMap<>();
    public static ArrayList<Character> COMMAND_INIT_CHARS = new ArrayList<>(Arrays.asList('.', ','));

    // Constructor
    public CmdBase(){

    }

    //Utility
    public void registerCommands(Object o) {
        for(Method me : o.getClass().getMethods()) {
            if(me.isAnnotationPresent(Cmd.class)) {
                Cmd cmd = me.getAnnotation(Cmd.class);
                // Gets commands under parent command
                List<FmtCommand> commands = this.CMDS.get(cmd.name());

                if(commands == null) {
                    // Initialize parent command and command sublist if not present
                    commands = new ArrayList<>();
                    this.CMDS.put(cmd.name().toLowerCase(), commands);
                    if(cmd.aliases().length >= 0) {
                        String[] cmdaliases = new String[1 + cmd.aliases().length];
                        for(int i = 1; i < cmdaliases.length; i++) {
                            // Take aliases from annotation and add them to the array
                            cmdaliases[i] = cmd.aliases()[i-1];
                            // TODO: Command Registration
                        }
                    } else {
                        // TODO: Command Registration
                    }
                }

                // Init FmtCommand instance;
                FmtCommand formCommand = new FmtCommand(cmd, me, o);
                String cmdRegex = formCommand.getRegex();
                Pattern patt = Pattern.compile(cmdRegex, 0);

                if(!formCommand.realCmd) {
                    LogManager.TOHSAKA.info(Utils.colorString(String.format("Command#g#[ name:#r#%s, #g#format: #r#%s #g#] #R#failed creation in Class#g#[ #r#%s #g#]#R#.", cmd.name(), cmd.format(), o.getClass().getSimpleName())));
                    continue;
                } else {
                   LogManager.TOHSAKA.info(Utils.colorString(String.format("Command#g#[ #r#name: #g#%s, #r#format: #g#%s, #r#regex: #g#%s, #r#permission #g#%s, #r#description: #g#%s] #R#was registered from Class#g#[ #r#%s #g#]#R#.", cmd.name(), cmd.format(), patt.toString(), cmd.permission().toString(), cmd.description(), o.getClass().getSimpleName())));
                }

                // Match list position with regex position to prevent recompiling
                int spot = commands.size();
                if(commands.add(formCommand)) {
                    Map<String, Integer> stuffs = REGEXS.computeIfAbsent(cmd.name().toLowerCase(), k -> new HashMap<>());
                    stuffs.put(patt.pattern(), spot);
                }
            }
        }
    }

    public void process(CommandSender cs, String command, String[] args) {
        List<FmtCommand> matches = getBestMatches(command.toLowerCase(), args);
        if(matches != null) {
            for (FmtCommand inst : matches) {
                if (inst.call(cs, command, args)) {
                }
            }
        }
    }

    // Setters

    // Getters
    public String getBuilt(String[] args) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(args[i]);
        }
        return sb.toString();
    }

    public List<FmtCommand> getBestMatches(String cmd, String[] args) {
        String sts = getBuilt(args);
        List<FmtCommand> instances = CMDS.get(cmd);
        Map<String, Integer> rgs = REGEXS.get(cmd);
        if(instances != null && rgs != null) {
            List<FmtCommand> instance1 = new ArrayList<>();
            List<FmtCommand> instance2 = new ArrayList<>();
            for(Map.Entry<String, Integer> reg : rgs.entrySet()) {
                if(sts.matches(reg.getKey())) {
                    FmtCommand cominst = instances.get(reg.getValue());
                    if(cominst.getArgCount() == args.length) {
                        instance1.add(cominst);
                    } else {
                        instance2.add(cominst);
                    }
                }
            }
            return instance1.size() > 0 ? instance1 : instance2.size() > 0 ? instance2 : null;
        }
        return null;
    }

    public Map<String, List<FmtCommand>> getCMDS() {
        return new HashMap<>(CMDS);
    }

}
