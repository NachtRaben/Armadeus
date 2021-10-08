package co.aikar.commands;

import co.aikar.commands.annotation.DiscordPermission;
import co.aikar.commands.apachecommonslang.ApacheCommonsExceptionUtil;
import com.google.common.base.Preconditions;
import com.velocitypowered.proxy.plugin.util.DummyPluginContainer;
import dev.armadeus.bot.api.command.DiscordCommand;
import dev.armadeus.bot.api.command.DiscordCommandIssuer;
import dev.armadeus.bot.api.config.GuildConfig;
import dev.armadeus.bot.api.events.CommandPreExecuteEvent;
import dev.armadeus.core.ArmaCoreImpl;
import dev.armadeus.core.command.NullCommandIssuer;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.entities.ApplicationInfo;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j2
public class JDACommandManager extends ArmaCommandManager<
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
    private Set<Long> botOwner = new HashSet<>();

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
            JDACommandEvent jce = context.getIssuer();
            if (!getBotOwnerId().contains(jce.getUser().getIdLong())) {
                throw new ConditionFailedException("Only the bot owner can use this command."); // TODO: MessageKey
            }
        });

        getCommandConditions().addCondition("guildonly", context -> {
            JDACommandEvent jce = context.getIssuer();
            if (jce.getChannel().getType() != ChannelType.TEXT) {
                throw new ConditionFailedException("This command must be used in guild chat."); // TODO: MessageKey
            }
        });

        getCommandConditions().addCondition("privateonly", context -> {
            JDACommandEvent jce = context.getIssuer();
            if (jce.getChannel().getType() != ChannelType.PRIVATE) {
                throw new ConditionFailedException("This command must be used in private chat."); // TODO: MessageKey
            }
        });

        getCommandConditions().addCondition("grouponly", context -> {
            JDACommandEvent jce = context.getIssuer();
            if (jce.getChannel().getType() != ChannelType.GROUP) {
                throw new ConditionFailedException("This command must be used in group chat."); // TODO: MessageKey
            }
        });

        setHelpFormatter(new JDAHelpFormatter(this));
    }

    void initializeBotOwner() {
        if (botOwner.isEmpty()) {
            if (shardManager.getShards().get(0).getAccountType() == AccountType.BOT) {
                ApplicationInfo app = shardManager.retrieveApplicationInfo().complete();
                if (app.getTeam() != null) {
                    botOwner.add(app.getTeam().getOwnerIdLong());
                    botOwner.addAll(app.getTeam().getMembers().stream().map(m -> m.getUser().getIdLong()).collect(Collectors.toList()));
                } else {
                    botOwner.add(shardManager.retrieveApplicationInfo().complete().getOwner().getIdLong());
                }
            } else {
                botOwner.add(shardManager.getShards().get(0).getSelfUser().getIdLong());
            }
        }
    }

    public Set<Long> getBotOwnerId() {
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
        Preconditions.checkArgument(DiscordCommand.class.isAssignableFrom(command.getClass()), "All commands must implement DiscordCommand.class");

        // Process annotations first
        Annotations annotations = getAnnotations();
        Class<? extends BaseCommand> self = command.getClass();
        if (annotations.hasAnnotation(self, DiscordPermission.class)) {
            DiscordPermission anno = annotations.getAnnotationFromClass(self, DiscordPermission.class);
            String additional = Arrays.stream(anno.value()).map(p -> p.name().toLowerCase(Locale.ENGLISH).replaceAll("_", "-")).collect(Collectors.joining(", "));
            log.warn("Found DiscordPermission annotation on {} with values {}", command.getClass().getSimpleName(), additional);
            log.warn("Initial {}", command.permission);
            if (command.permission == null || command.permission.isEmpty()) {
                command.permission = additional;
            } else {
                command.permission = command.permission + "," + additional;
            }
            log.warn("Modified {}", command.permission);
        }

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

    @Override
    public RegisteredCommand createRegisteredCommand(BaseCommand command, String cmdName, Method method, String prefSubCommand) {
        RegisteredCommand cmd = new RegisteredCommand(command, cmdName, method, prefSubCommand);
        DiscordPermission anno = cmd.getAnnotation(DiscordPermission.class) != null ? (DiscordPermission) cmd.getAnnotation(DiscordPermission.class) : null;
        if (anno != null) {
            String additional = Arrays.stream(anno.value()).map(p -> p.name().toLowerCase(Locale.ENGLISH).replaceAll("_", "-")).collect(Collectors.joining(", "));
            log.warn("Found DiscordPermission annotation on {} with values {}", cmd.command, additional);
            log.warn("Initial {}", cmd.permission);
            if (cmd.permission == null || cmd.permission.isEmpty()) {
                cmd.permission = additional;
            } else {
                cmd.permission = cmd.permission + "," + additional;
            }
            log.warn("Modified {}", cmd.permission);
            cmd.computePermissions();
        }
        return cmd;
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
        if (issuer instanceof MessageReceivedEvent) {
            return new CommandSenderImpl(core, this, (MessageReceivedEvent) issuer);
        } else if (issuer instanceof SlashCommandEvent) {
            return new CommandSenderImpl(core, this, (SlashCommandEvent) issuer);
        } else {
            throw new IllegalArgumentException(issuer.getClass().getName() + " is not a valid event type");
        }
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
        List<String> largs = new ArrayList<>(List.of(event.getCommandPath().split("/")));
        for (OptionMapping option : event.getOptions()) {
            largs.add(option.getAsString());
        }

        String[] args = largs.toArray(new String[0]);

        String cmd = args[0].toLowerCase(Locale.ENGLISH);
        JDARootCommand rootCommand = this.commands.get(cmd);
        if (rootCommand == null) {
            return;
        }

        // Hacky way to allow newlines right next to the command
        if (ACFPatterns.NEWLINE.matcher(args[0]).find()) {
            args = Stream.concat(Arrays.stream(ACFPatterns.NEWLINE.split(args[0])), Arrays.stream(Arrays.copyOfRange(args, 1, args.length))).toArray(String[]::new);
        }

        args = args.length > 1 ? Arrays.copyOfRange(args, 1, args.length) : new String[0];
        if (!devCheck(event))
            return;
        event.deferReply().setEphemeral(true).complete();
        CommandSenderImpl sender = (CommandSenderImpl) this.getCommandIssuer(event);
        DiscordCommandIssuer issuer = (DiscordCommandIssuer) this.getCommandIssuer(event);
        try {
            if (core.eventManager().fire(new CommandPreExecuteEvent(issuer, rootCommand)).get().isAllowed()) {
                String[] finalArgs = args;
                ForkJoinPool.commonPool().execute(() -> {
                    rootCommand.execute(sender, cmd, finalArgs);
                    if (!sender.isSlashAcked()) {
                        event.getHook().sendMessage("Success :heavy_check_mark:").queue();
                    }
                });
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
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

        String[] args = msg.substring(prefixFound.length()).split("[\\n\\r\\s]+", -1);
        log.warn("Before: {}", (Object) args);
        if (args.length == 0) {
            return;
        }
        if(args.length > 1) {
            List<String> reprocessed = new ArrayList<>(Collections.singleton(args[0]));
            reprocessed.addAll(Arrays.stream(ACFPatterns.SPACE.split(msg.substring(prefixFound.length()).replace(args[0], ""), -1)).filter(s -> !s.isEmpty()).collect(Collectors.toList()));
            log.warn("Processed: {}", reprocessed);
            args = reprocessed.toArray(new String[0]);
        }
        log.warn("After: {}", (Object) args);

        String cmd = args[0].toLowerCase(Locale.ENGLISH);
        JDARootCommand rootCommand = this.commands.get(cmd);
        if (rootCommand == null) {
            return;
        }
        args = args.length > 1 ? Arrays.copyOfRange(args, 1, args.length) : new String[0];
        if (!devCheck(event))
            return;

        DiscordCommandIssuer issuer = (DiscordCommandIssuer) this.getCommandIssuer(event);
        String[] finalArgs = args;
        ForkJoinPool.commonPool().execute(() -> {
            try {
                if (core.eventManager().fire(new CommandPreExecuteEvent(issuer, rootCommand)).get().isAllowed()) {
                    rootCommand.execute(issuer, cmd, finalArgs);
                }
            } catch (InterruptedException | ExecutionException e) {
                log.error(e);
            }
        });
    }

    public List<String> getAnnotationValues(AnnotatedElement object, Class<? extends Annotation> annoClass, int options) {
        String[] values = getAnnotations().getAnnotationValues(object, annoClass, ACFPatterns.PIPE, options);
        return values == null ? Collections.emptyList() : List.of(values);
    }

    private boolean devCheck(Event e) {
        if (core.instanceManager() != null && core.instanceManager().isDevActive()) {
            Guild guild = e instanceof MessageReceivedEvent ? ((MessageReceivedEvent) e).getGuild() : ((SlashCommandEvent) e).getGuild();
            if (guild == null) {
                return true;
            }
            GuildConfig gc = core.guildManager().getConfigFor(guild);
            if (gc.isDevGuild() && !core.armaConfig().isDevMode()) {
                // Prod Bot
                logger.warning("Ignoring command message in " + guild.getName() + " because a dev instance is active");
                return false;
            }
            if (!gc.isDevGuild() && core.armaConfig().isDevMode()) {
                // Dev Bot
                logger.warning("Ignoring command message in " + guild.getName() + " because it is not a dev guild");
                return false;
            }
        }
        return true;
    }

    public CommandConfig getCommandConfig(MessageReceivedEvent event) {
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
        if (issuer.equals(NullCommandIssuer.INSTANCE))
            return "";
        MessageReceivedEvent event = ((JDACommandEvent) issuer).getEvent();
        CommandConfig commandConfig = getCommandConfig(event);
        List<String> prefixes = commandConfig.getCommandPrefixes();
        return prefixes.isEmpty() ? "" : prefixes.get(0);
    }
}
