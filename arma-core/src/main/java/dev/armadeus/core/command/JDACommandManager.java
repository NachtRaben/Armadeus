package dev.armadeus.core.command;

import co.aikar.commands.*;
import co.aikar.commands.apachecommonslang.ApacheCommonsExceptionUtil;
import com.velocitypowered.proxy.plugin.util.DummyPluginContainer;
import dev.armadeus.bot.api.config.GuildConfig;
import dev.armadeus.core.ArmaCoreImpl;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class JDACommandManager extends CommandManager<
        MessageReceivedEvent,
        JDACommandEvent,
        String,
        MessageFormatter<String>,
        JDACommandExecutionContext,
        JDAConditionContext
        > {

    private final ArmaCoreImpl core;
    private ShardManager shardManager;
    protected JDACommandCompletions completions;
    protected JDACommandContexts contexts;
    protected JDALocales locales;
    protected Map<String, JDARootCommand> commands = new HashMap<>();
    private Logger logger;
    private CommandConfig defaultConfig;
    private CommandConfigProvider configProvider;
    private CommandPermissionResolver permissionResolver;
    private long botOwner = 0L;

    public JDACommandManager(ArmaCoreImpl core) {
        this.core = core;
    }

    public void initialize(JDAOptions options) {
        this.shardManager = core.shardManager();
        if (options == null) {
            options = new JDAOptions();
        }
        this.permissionResolver = options.permissionResolver;
        core.eventManager().register(DummyPluginContainer.VELOCITY, new JDAListener(this));
        this.configProvider = options.configProvider;
        this.defaultFormatter = new JDAMessageFormatter();
        this.completions = new JDACommandCompletions(this);
        this.logger = Logger.getLogger(this.getClass().getSimpleName());

        getCommandConditions().addCondition("owneronly", context -> {
            if (context.getIssuer().getEvent().getAuthor().getIdLong() != getBotOwnerId()) {
                throw new ConditionFailedException("Only the bot owner can use this command."); // TODO: MessageKey
            }
        });

        getCommandConditions().addCondition("guildonly", context -> {
            if (context.getIssuer().getEvent().getChannelType() != ChannelType.TEXT) {
                throw new ConditionFailedException("This command must be used in guild chat."); // TODO: MessageKey
            }
        });

        getCommandConditions().addCondition("privateonly", context -> {
            if (context.getIssuer().getEvent().getChannelType() != ChannelType.PRIVATE) {
                throw new ConditionFailedException("This command must be used in private chat."); // TODO: MessageKey
            }
        });

        getCommandConditions().addCondition("grouponly", context -> {
            if (context.getIssuer().getEvent().getChannelType() != ChannelType.GROUP) {
                throw new ConditionFailedException("This command must be used in group chat."); // TODO: MessageKey
            }
        });
    }

    void initializeBotOwner() {
        if (botOwner == 0L) {
            if (shardManager.getShards().get(0).getAccountType() == AccountType.BOT) {
                botOwner = shardManager.retrieveApplicationInfo().complete().getOwner().getIdLong();
            } else {
                botOwner = shardManager.getShards().get(0).getSelfUser().getIdLong();
            }
        }
    }

    public long getBotOwnerId() {
        // Just in case initialization on ReadyEvent fails.
        initializeBotOwner();
        return botOwner;
    }

    public ShardManager getShardManager() {
        return shardManager;
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public CommandConfig getDefaultConfig() {
        return defaultConfig;
    }

    public void setDefaultConfig(@NotNull CommandConfig defaultConfig) {
        this.defaultConfig = defaultConfig;
    }

    public CommandConfigProvider getConfigProvider() {
        return configProvider;
    }

    public void setConfigProvider(CommandConfigProvider configProvider) {
        this.configProvider = configProvider;
    }

    public CommandPermissionResolver getPermissionResolver() {
        return permissionResolver;
    }

    public void setPermissionResolver(CommandPermissionResolver permissionResolver) {
        this.permissionResolver = permissionResolver;
    }

    @Override
    public CommandContexts<?> getCommandContexts() {
        if (this.contexts == null) {
            this.contexts = new JDACommandContexts(this);
        }
        return this.contexts;
    }

    @Override
    public CommandCompletions<?> getCommandCompletions() {
        return this.completions;
    }

    @Override
    public void registerCommand(BaseCommand command) {
        command.onRegister(this);
        for (Map.Entry<String, RootCommand> entry : command.getRegisteredCommandsMap().entrySet()) {
            String commandName = entry.getKey().toLowerCase(Locale.ENGLISH);
            JDARootCommand cmd = (JDARootCommand) entry.getValue();
            if (!cmd.isRegistered) {
                cmd.isRegistered = true;
                commands.put(commandName, cmd);
            }
        }
    }

    public void unregisterCommand(BaseCommand command) {
        for (Map.Entry<String, RootCommand> entry : command.getRegisteredCommandsMap().entrySet()) {
            String jdaCommandName = entry.getKey().toLowerCase(Locale.ENGLISH);
            JDARootCommand jdaCommand = (JDARootCommand) entry.getValue();
            jdaCommand.getSubCommands().values().removeAll(command.getSubCommands().values());
            if (jdaCommand.isRegistered && jdaCommand.getSubCommands().isEmpty()) {
                jdaCommand.isRegistered = false;
                commands.remove(jdaCommandName);
            }
        }
    }

    @Override
    public boolean hasRegisteredCommands() {
        return !this.commands.isEmpty();
    }

    @Override
    public boolean isCommandIssuer(Class<?> type) {
        return JDACommandEvent.class.isAssignableFrom(type);
    }

    @Override
    public JDACommandEvent getCommandIssuer(Object issuer) {
        if (!(issuer instanceof MessageReceivedEvent)) {
            throw new IllegalArgumentException(issuer.getClass().getName() + " is not a Message Received Event.");
        }
        return new CommandSenderImpl(core, this, (MessageReceivedEvent) issuer);
    }

    @Override
    public RootCommand createRootCommand(String cmd) {
        return new JDARootCommand(this, cmd);
    }

    @Override
    public Collection<RootCommand> getRegisteredRootCommands() {
        return Collections.unmodifiableCollection(commands.values());
    }

    @Override
    public Locales getLocales() {
        if (this.locales == null) {
            this.locales = new JDALocales(this);
            this.locales.loadLanguages();
        }
        return this.locales;
    }

    @Override
    public CommandExecutionContext createCommandContext(RegisteredCommand command, CommandParameter parameter, CommandIssuer sender, List<String> args, int i, Map<String, Object> passedArgs) {
        return new JDACommandExecutionContext(command, parameter, (JDACommandEvent) sender, args, i, passedArgs);
    }

    @Override
    public CommandCompletionContext createCompletionContext(RegisteredCommand command, CommandIssuer sender, String input, String config, String[] args) {
        // Not really going to be used;
        //noinspection unchecked
        return new CommandCompletionContext(command, sender, input, config, args);
    }

    @Override
    public void log(LogLevel level, String message, Throwable throwable) {
        Level logLevel = level == LogLevel.INFO ? Level.INFO : Level.SEVERE;
        logger.log(logLevel, LogLevel.LOG_PREFIX + message);
        if (throwable != null) {
            for (String line : ACFPatterns.NEWLINE.split(ApacheCommonsExceptionUtil.getFullStackTrace(throwable))) {
                logger.log(logLevel, LogLevel.LOG_PREFIX + line);
            }
        }
    }

    void dispatchSlash(SlashCommandEvent event) {
        StringBuilder sb = new StringBuilder();
        sb.append("SlashEvent:");
        sb.append("\nName: ").append(event.getName());
        sb.append("\nOptions: ").append(event.getOptions());
        sb.append("\nSubName: ").append(event.getSubcommandName());
        sb.append("\nSubGroup: ").append(event.getSubcommandGroup());
        System.out.println(sb.toString());
        event.reply(event.getOption("content").getAsString()).queue();
    }

    void dispatchEvent(MessageReceivedEvent event) {
        Message message = event.getMessage();
        if (message.getAuthor().isBot())
            return;
        String msg = message.getContentRaw();

        CommandConfig config = getCommandConfig(event);

        String prefixFound = null;
        for (String prefix : config.getCommandPrefixes()) {
            if (msg.startsWith(prefix)) {
                prefixFound = prefix;
                break;
            }
        }
        if (prefixFound == null) {
            return;
        }

        String[] args = ACFPatterns.SPACE.split(msg.substring(prefixFound.length()), -1);
        if (args.length == 0) {
            System.out.println("No Tokens");
            return;
        }
        // Hacky way to allow newlines right next to the command
        if (ACFPatterns.NEWLINE.matcher(args[0]).find()) {
            args = Stream.concat(Arrays.stream(ACFPatterns.NEWLINE.split(args[0])), Arrays.stream(Arrays.copyOfRange(args, 1, args.length))).toArray(String[]::new);
        }

        String cmd = args[0].toLowerCase(Locale.ENGLISH);
        JDARootCommand rootCommand = this.commands.get(cmd);
        if (rootCommand == null) {
            System.out.println("No Command");
            return;
        }
        if (args.length > 1) {
            args = Arrays.copyOfRange(args, 1, args.length);
        } else {
            args = new String[0];
        }
        String[] finalArgs = args;

        if(event.isFromGuild() && core.isDevActive()) {
            GuildConfig gc = core.guildManager().getConfigFor(event.getGuild());
            if(gc.isDevGuild() && !core.armaConfig().isDevMode()){
                // Prod Bot
                logger.warning("Ignoring command message in " + event.getGuild().getName() + " because a dev instance is active");
            }
            if(!gc.isDevGuild() && core.armaConfig().isDevMode()) {
                // Dev Bot
                logger.warning("Ignoring command message in " + event.getGuild().getName() + " because it is not a dev guild");
                return;
            }
        }
        CompletableFuture.runAsync(() -> rootCommand.execute(this.getCommandIssuer(event), cmd, finalArgs));
    }

    private CommandConfig getCommandConfig(MessageReceivedEvent event) {
        CommandConfig config = this.defaultConfig;
        if (this.configProvider != null) {
            CommandConfig provided = this.configProvider.provide(event);
            if (provided != null) {
                config = provided;
            }
        }
        return config;
    }

    @Override
    public String getCommandPrefix(CommandIssuer issuer) {
        MessageReceivedEvent event = ((JDACommandEvent) issuer).getEvent();
        CommandConfig commandConfig = getCommandConfig(event);
        List<String> prefixes = commandConfig.getCommandPrefixes();
        return prefixes.isEmpty() ? "" : prefixes.get(0);
    }
}
