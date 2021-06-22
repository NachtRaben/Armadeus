package dev.armadeus.core.util;

import co.aikar.commands.CommandHelp;
import co.aikar.commands.CommandParameter;
import co.aikar.commands.HelpEntry;
import co.aikar.commands.JDARootCommand;
import co.aikar.commands.RootCommand;
import co.aikar.commands.annotation.CommandAlias;
import dev.armadeus.bot.api.util.StringUtils;
import dev.armadeus.core.ArmaCoreImpl;
import dev.armadeus.core.command.NullCommandIssuer;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SlashCommandsUtil {

    private static final Logger logger = LogManager.getLogger();
    private ArmaCoreImpl core;

    public SlashCommandsUtil(ArmaCoreImpl core) {
        this.core = core;
    }

    private void registerRootCommand(Map<String, CommandData> commandMap, JDARootCommand root) {
        String name = root.getCommandName();
        String description = StringUtils.getOrDefault(root.getDescription(), "Unspecified Description");
        CommandData rootData = commandMap.computeIfAbsent(name, i -> new CommandData(name, description));
        List<String> aliases = new ArrayList<>();
        aliases.add(root.getCommandName());
        if (root.getDefaultRegisteredCommand() != null) {
            Annotation annotation = root.getDefaultRegisteredCommand().getAnnotation(CommandAlias.class);
            if (annotation == null)
                logger.error("Missing annotation on " + root.getCommandName());

            if (annotation != null)
                aliases.addAll(Arrays.asList(((CommandAlias) annotation).value().split("\\|")));
        }
        logger.warn("Root: {} {}", rootData.getName(), aliases);

        Map<String, SubcommandGroupData> subCommandGroups = new HashMap<>();
        Map<String, SubcommandData> subCommands = new HashMap<>();
        CommandHelp help = root.getCommandHelp(NullCommandIssuer.INSTANCE, new String[0]);
        List<HelpEntry> entries = help.getHelpEntries();
        for (HelpEntry entry : entries) {
            // Do this so we don't map aliases of root commands
            if (aliases.contains(entry.getCommand())) {
                for (CommandParameter param : Arrays.copyOfRange(entry.getParameters(), 1, entry.getParameters().length)) {
                    OptionType type = OptionType.STRING;
                    if (Boolean.class.isAssignableFrom(type.getClass()))
                        type = OptionType.BOOLEAN;
                    if (Integer.class.isAssignableFrom(type.getClass()))
                        type = OptionType.INTEGER;
                    String paramDescription = StringUtils.getOrDefault(param.getDescription(), "Unspecified Description");
                    OptionData option = new OptionData(type, param.getName(), paramDescription, !param.isOptional());
                    logger.warn("\tOption: {} <> {} <> {}", option.getName(), option.isRequired(), option.getDescription());
                    rootData.addOptions(option);
                }
                continue;
            }

            List<String> tokens = new ArrayList<>(Arrays.asList(entry.getCommand().split("\\s+")));
            tokens.removeIf(aliases::contains);
            logger.error(tokens);
            if (tokens.size() == 1) {
                registerSubCommand(subCommands, tokens.get(0), entry);
            } else {
                registerSubCommandGroup(subCommandGroups, entry, tokens.toArray(String[]::new));
            }
        }
        if (!subCommandGroups.isEmpty())
            rootData.addSubcommandGroups(subCommandGroups.values());
        if (!subCommands.isEmpty())
            rootData.addSubcommands(subCommands.values());
    }

    private void registerSubCommandGroup(Map<String, SubcommandGroupData> subMap, HelpEntry entry, String... tokens) {
        if (tokens.length > 3)
            throw new IllegalArgumentException("Unable to register command to discord: " + entry.getCommand());
        if (tokens.length == 3)
            tokens = Arrays.copyOfRange(tokens, 1, tokens.length);
        String name = tokens[0];
        String sub = tokens[1];
        String description = StringUtils.getOrDefault(entry.getDescription(), "Unspecified Description");
        SubcommandGroupData groupData = subMap.computeIfAbsent(name, i -> new SubcommandGroupData(name, description));
        logger.warn("\tGroup: {}", name);
        SubcommandData subData = new SubcommandData(sub, description);
        logger.warn("\t\tSub: {}", sub);
        for (CommandParameter param : Arrays.copyOfRange(entry.getParameters(), 1, entry.getParameters().length)) {
            OptionType type = OptionType.STRING;
            if (Boolean.class.isAssignableFrom(type.getClass()))
                type = OptionType.BOOLEAN;
            if (Integer.class.isAssignableFrom(type.getClass()))
                type = OptionType.INTEGER;
            String paramDescription = StringUtils.getOrDefault(param.getDescription(), "Unspecified Description");
            OptionData option = new OptionData(type, param.getName(), paramDescription, !param.isOptional());
            logger.warn("\t\t\tOption: {} <> {} <> {}", option.getName(), option.isRequired(), option.getDescription());
            subData.addOptions(option);
        }
        groupData.addSubcommands(subData);
    }

    private void registerSubCommand(Map<String, SubcommandData> subMap, String name, HelpEntry entry) {
        String description = StringUtils.getOrDefault(entry.getDescription(), "Unspecified Description");
        SubcommandData data = subMap.computeIfAbsent(name, i -> new SubcommandData(name, description));
        logger.warn("Sub: {}", name);
        if (entry.getParameters().length == 1)
            return;
        for (CommandParameter param : Arrays.copyOfRange(entry.getParameters(), 1, entry.getParameters().length)) {
            OptionType type = OptionType.STRING;
            if (Boolean.class.isAssignableFrom(type.getClass()))
                type = OptionType.BOOLEAN;
            if (Integer.class.isAssignableFrom(type.getClass()))
                type = OptionType.INTEGER;
            String paramDescription = StringUtils.getOrDefault(param.getDescription(), "Unspecified Description");
            OptionData option = new OptionData(type, param.getName(), paramDescription, !param.isOptional());
            logger.warn("\t\t\tOption: {} <> {} <> {}", option.getName(), option.isRequired(), option.getDescription());
            data.addOptions(option);
        }
    }

    public Collection<CommandData> generateCommandData() {
        Map<String, CommandData> commandMap = new HashMap<>();
        for (RootCommand rootCommand : core.commandManager().getRegisteredRootCommands()) {
            registerRootCommand(commandMap, (JDARootCommand) rootCommand);
        }
        return commandMap.values();
    }
}
