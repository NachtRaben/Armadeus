package dev.armadeus.core.command;

import co.aikar.commands.CommandExecutionContext;
import co.aikar.commands.CommandParameter;
import co.aikar.commands.RegisteredCommand;

import java.util.List;
import java.util.Map;

public class JDACommandExecutionContext extends CommandExecutionContext<JDACommandExecutionContext, JDACommandEvent> {
    JDACommandExecutionContext(RegisteredCommand cmd, CommandParameter param, JDACommandEvent sender, List<String> args, int index, Map<String, Object> passedArgs) {
        super(cmd, param, sender, args, index, passedArgs);
    }
}
