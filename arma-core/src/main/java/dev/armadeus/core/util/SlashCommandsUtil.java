package dev.armadeus.core.util;

import co.aikar.commands.ACFPatterns;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandParameter;
import co.aikar.commands.HelpEntry;
import co.aikar.commands.JDACommandExecutionContext;
import co.aikar.commands.JDARootCommand;
import co.aikar.commands.RegisteredCommand;
import dev.armadeus.bot.api.util.StringUtils;
import dev.armadeus.core.ArmaCoreImpl;
import dev.armadeus.core.command.NullCommandIssuer;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
@Log4j2
public class SlashCommandsUtil {

    private final ArmaCoreImpl core;

    public SlashCommandsUtil(ArmaCoreImpl core) {
        this.core = core;
    }

    private void registerRootCommand(Map<String, CommandData> commandMap, JDARootCommand root) {
        // Get Command Information
        String name = root.getCommandName();
        String description = StringUtils.getOrDefault(root.getDescription(), "Unspecified Description");

        // Prepare payload Data
        CommandData rootData = commandMap.computeIfAbsent(name, i -> new CommandData(name, description));

        // Generate Command Help
        List<HelpEntry> entries = new ArrayList<>(root.getCommandHelp(NullCommandIssuer.INSTANCE, new String[0]).getHelpEntries());
        if(entries.size() == 0)
            return;

        // Register Sub Commands and Groups
        log.info("- Root: {} ({})", rootData.getName(), rootData.getDescription());
        Map<String, SubcommandGroupData> subCommandGroupMap = new HashMap<>();
        for (HelpEntry entry : entries) {
            if (entry.getParameters().length == 1)
                continue;

            String[] tokens = entry.getCommand().split(ACFPatterns.SPACE.pattern());
            if (tokens.length > 0)
                tokens = Arrays.copyOfRange(tokens, 1, tokens.length);

            String subDescription = StringUtils.getOrDefault(entry.getDescription(), "Unspecified Description");
            CommandParameter[] parameters = Arrays.copyOfRange(entry.getParameters(), 1, entry.getParameters().length);
//            log.warn(Arrays.stream(parameters).map(param -> param.getName()).collect(Collectors.toList()));
            if (tokens.length == 0) { // Option
                for (CommandParameter param : parameters) {
                    String paramDesc = StringUtils.getOrDefault(param.getDescription(), param.getName());
                    rootData.addOption(getType(param), param.getName(), paramDesc, !param.isOptional());
                    log.info("\t- Option: {}[{}] ({})", param.getType().getSimpleName(), param.getName(), paramDesc);
                }
            } else if (tokens.length == 1) { // SubCommand
                SubcommandData subData = new SubcommandData(tokens[0], subDescription);
                log.info("\t- SubCmd: {} ({})", tokens[0], subDescription);
                for (CommandParameter param : parameters) {
                    String paramDesc = StringUtils.getOrDefault(param.getDescription(), param.getName());
                    subData.addOption(getType(param), param.getName(), paramDesc, !param.isOptional());
                    log.info("\t\t- Option: {}[{}] ({})", param.getType().getSimpleName(), param.getName(), paramDesc);
                }
                rootData.addSubcommands(subData);
            } else if(tokens.length == 2) {
                SubcommandGroupData subGroupData = subCommandGroupMap.computeIfAbsent(tokens[0], scg -> new SubcommandGroupData(scg, subDescription));
                log.warn("Root [{}]", rootData.getSubcommandGroups().stream().map(scg -> scg.getName() + " <> " + scg.getSubcommands().size()).collect(Collectors.joining(", ")));
                log.warn("Sub [{}]", subGroupData.getSubcommands().stream().map(sc -> sc.getName()).collect(Collectors.joining(", ")));
                log.info("\t- SubGroup: {} ({})", tokens[0], subDescription);
                SubcommandData subData = new SubcommandData(tokens[1], subDescription);
                log.info("\t\t- SubCmd: {} ({})", tokens[1], subDescription);
                for (CommandParameter param : parameters) {
                    String paramDesc = StringUtils.getOrDefault(param.getDescription(), param.getName());
                    subData.addOption(getType(param), param.getName(), paramDesc, !param.isOptional());
                    log.info("\t\t\t- Option: {}[{}] ({})", param.getType().getSimpleName(), param.getName(), paramDesc);
                }
                subGroupData.addSubcommands(subData);
            } else {
                log.warn("\t- {} {} ({})", String.join(" ", tokens), Arrays.stream(parameters).map(p -> String.format("[%s](%s)", p.getType().getSimpleName(), p.getName())).collect(Collectors.joining(" ")), subDescription);
            }
        }
        if(!subCommandGroupMap.isEmpty())
            rootData.addSubcommandGroups(subCommandGroupMap.values());

        // If we have no options, don't publish
//        if(rootData.getOptions().isEmpty() && rootData.getSubcommandGroups().isEmpty() && rootData.getSubcommands().isEmpty()) {
//            log.error("- REMOVED Root: {} ({})", rootData.getName(), rootData.getDescription());
//            commandMap.remove(rootData.getName());
//        }
    }

    private OptionType getType(CommandParameter param) {
        Class<?> type = param.getType();
        if (String.class.isAssignableFrom(type)) {
            return OptionType.STRING;
        } else if (int.class.isAssignableFrom(type) || Integer.class.isAssignableFrom(type)) {
            return OptionType.INTEGER;
        } else if (Boolean.class.isAssignableFrom(type) || boolean.class.isAssignableFrom(type)) {
            return OptionType.BOOLEAN;
        } else if (Number.class.isAssignableFrom(type)) {
            return OptionType.NUMBER;
        } else if (User.class.isAssignableFrom(type)) {
            return OptionType.USER;
        } else if (MessageChannel.class.isAssignableFrom(type)) {
            return OptionType.CHANNEL;
        } else if (Role.class.isAssignableFrom(type)) {
            return OptionType.ROLE;
        } else if (IMentionable.class.isAssignableFrom(type)) {
            return OptionType.MENTIONABLE;
        } else {
            return OptionType.STRING;
        }
    }

    private static List<CommandParameter<JDACommandExecutionContext>> getParameters(RegisteredCommand command) {
        try {
            Field field = command.getClass().getDeclaredField("parameters");
            field.setAccessible(true);
            return new ArrayList<>(List.of((CommandParameter<JDACommandExecutionContext>[]) field.get(command)));
        } catch (IllegalAccessException | NoSuchFieldException e) {
            return Collections.emptyList();
        }
    }

    private static RegisteredCommand<JDACommandExecutionContext> getRegisteredCommand(HelpEntry entry) {
        try {
            Field field = entry.getClass().getDeclaredField("command");
            field.setAccessible(true);
            return (RegisteredCommand<JDACommandExecutionContext>) field.get(entry);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            return null;
        }
    }

    private static BaseCommand getScope(RegisteredCommand<JDACommandExecutionContext> command) {
        try {
            Field field = command.getClass().getDeclaredField("scope");
            field.setAccessible(true);
            return (BaseCommand) field.get(command);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            return null;
        }
    }

    private void registerSubCommandGroup(Map<String, SubcommandGroupData> subMap, HelpEntry entry, String... tokens) {
//        if (tokens.length > 3)
//            throw new IllegalArgumentException("Unable to register command to discord: " + entry.getCommand());
//        if (tokens.length == 3)
//            tokens = Arrays.copyOfRange(tokens, 1, tokens.length);
//        String name = tokens[0];
//        String sub = tokens[1];
//        String description = StringUtils.getOrDefault(entry.getDescription(), "Unspecified Description");
//        SubcommandGroupData groupData = subMap.computeIfAbsent(name, i -> new SubcommandGroupData(name, description));
//        log.warn("\tGroup: {}", name);
//        SubcommandData subData = new SubcommandData(sub, description);
//        log.warn("\t\tSub: {}", sub);
//        for (CommandParameter param : Arrays.copyOfRange(entry.getParameters(), 1, entry.getParameters().length)) {
//            OptionType type = OptionType.STRING;
//            if (Boolean.class.isAssignableFrom(type.getClass()))
//                type = OptionType.BOOLEAN;
//            if (Integer.class.isAssignableFrom(type.getClass()))
//                type = OptionType.INTEGER;
//            String paramDescription = StringUtils.getOrDefault(param.getDescription(), "Unspecified Description");
//            OptionData option = new OptionData(type, param.getName(), paramDescription, !param.isOptional());
//            log.warn("\t\t\tOption: {} <> {} <> {}", option.getName(), option.isRequired(), option.getDescription());
//            subData.addOptions(option);
//        }
//        groupData.addSubcommands(subData);
    }

    private void registerSubCommand(Map<String, SubcommandData> subMap, String name, HelpEntry entry) {
//        String description = StringUtils.getOrDefault(entry.getDescription(), "Unspecified Description");
//        SubcommandData data = subMap.computeIfAbsent(name, i -> new SubcommandData(name, description));
//        log.warn("Sub: {}", name);
//        if (entry.getParameters().length == 1)
//            return;
//        for (CommandParameter param : Arrays.copyOfRange(entry.getParameters(), 1, entry.getParameters().length)) {
//            OptionType type = OptionType.STRING;
//            if (Boolean.class.isAssignableFrom(type.getClass()))
//                type = OptionType.BOOLEAN;
//            if (Integer.class.isAssignableFrom(type.getClass()))
//                type = OptionType.INTEGER;
//            String paramDescription = StringUtils.getOrDefault(param.getDescription(), "Unspecified Description");
//            OptionData option = new OptionData(type, param.getName(), paramDescription, !param.isOptional());
//            log.warn("\t\t\tOption: {} <> {} <> {}", option.getName(), option.isRequired(), option.getDescription());
//            data.addOptions(option);
//        }
    }

    public Collection<CommandData> generateCommandData() {
        Map<String, CommandData> commandMap = new HashMap<>();
        Collection<JDARootCommand> commands = core.commandManager().getRegisteredRootCommands().stream()
                .map(JDARootCommand.class::cast)
                .sorted(Comparator.comparing(JDARootCommand::getCommandName))
                .collect(Collectors.toList());
        for (JDARootCommand rootCommand : commands) {
            registerRootCommand(commandMap, rootCommand);
        }
        return commandMap.values();
    }
}
