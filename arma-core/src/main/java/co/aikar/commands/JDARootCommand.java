package co.aikar.commands;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JDARootCommand implements RootCommand {

    private final String name;
    boolean isRegistered = false;
    private JDACommandManager manager;
    private BaseCommand defCommand;
    private SetMultimap<String, RegisteredCommand> subCommands = HashMultimap.create();
    private List<BaseCommand> children = new ArrayList<>();

    JDARootCommand(JDACommandManager manager, String name) {
        this.manager = manager;
        this.name = name;
    }

    @Override

    public void addChild(BaseCommand command) {
        if (this.defCommand == null || !command.getSubCommands().get(BaseCommand.DEFAULT).isEmpty()) {
            this.defCommand = command;
        }
        addChildShared(this.children, this.subCommands, command);
    }

    @Override
    public CommandManager getManager() {
        return this.manager;
    }

    @Override
    public SetMultimap<String, RegisteredCommand> getSubCommands() {
        return this.subCommands;
    }

    @Override
    public List<BaseCommand> getChildren() {
        return this.children;
    }

    @Override
    public String getCommandName() {
        return this.name;
    }

    @Override
    public BaseCommand getDefCommand() {
        return defCommand;
    }

    public CommandHelp getCommandHelp(CommandIssuer issuer, String[] args) {
        CommandHelp help = getManager().generateCommandHelp(issuer, this);
        if(args.length != 0)
            help.setSearch(Arrays.asList(args));
        return help;
    }

    public boolean isRegistered() {
        return isRegistered;
    }

    public void setRegistered(boolean registered) {
        isRegistered = registered;
    }
}
