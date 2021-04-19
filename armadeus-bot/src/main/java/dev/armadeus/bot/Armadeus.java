package dev.armadeus.bot;

import dev.armadeus.bot.listeners.WelcomeListener;
import dev.armadeus.core.DiscordBot;
import dev.armadeus.core.command.DiscordCommand;
import joptsimple.OptionSet;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class Armadeus extends DiscordBot {

    private static final Logger logger = LogManager.getLogger();

    private final ConcurrentHashMap<Long, Map<Long, Long>> cooldowns = new ConcurrentHashMap<>();
    @Getter
    private final ConcurrentHashMap<Long, Long> pings = new ConcurrentHashMap<>();

    public Armadeus(OptionSet options) {
        super(options);
    }

    @Override
    public void start() {
        super.start();
        getShardManager().addEventListener(new WelcomeListener(get()));
        long start = System.currentTimeMillis();
        logger.debug("Took " + (System.currentTimeMillis() - start) + "ms to load all shards.");
        registerACFCommands();
        postStart();
    }

    private void registerACFCommands() {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage("dev.armadeus.bot.commands"))
                .setScanners(new SubTypesScanner())
        );

        reflections.getSubTypesOf(DiscordCommand.class).forEach(clazz -> {
            try {
                if (!clazz.isSynthetic() && !clazz.isAnonymousClass() && !Modifier.isAbstract(clazz.getModifiers())) {
                    logger.info("Registering command class {}", clazz.getSimpleName());
                    getCommandManager().registerCommand(clazz.getConstructor().newInstance());
                }
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
                logger.error("Failed to register command class", e);
            }
        });

    }

//    @Override
//    public void onCommandPreProcess(CommandPreProcessEvent e) {
//        if (e.getSender() instanceof GuildCommandSender) {
//            GuildCommandSender sendee = (GuildCommandSender) e.getSender();
//            GuildConfig config = sendee.getGuildConfig();
//            logger.debug(String.format("CommandPreProcess >> Sender: %s#{%s}, Args: %s, Flags: %s, Command: %s", sendee.getMember().getEffectiveName(), sendee.getGuild().getName(), e.getArgs(), e.getFlags(), e.getCommand().getName()));
//            if (sendee.getMember().isOwner() || sendee.getMember().hasPermission(Permission.ADMINISTRATOR) || BotConfig.get().getOwnerIds().contains(sendee.getUser().getIdLong()) || BotConfig.get().getDeveloperIds().contains(sendee.getUser().getIdLong()))
//                return;
//
//            Map<String, Set<Long>> blacklistedCommands = config.getDisabledCommands();
//            if (blacklistedCommands.containsKey(e.getCommand().getName())) {
//                Set<Long> blacklistedRoles = blacklistedCommands.get(e.getCommand().getName());
//                boolean hasRole = blacklistedRoles.contains(-1L) || sendee.getMember().getRoles().stream().anyMatch(role -> blacklistedRoles.contains(role.getIdLong()));
//                if (config.isBlacklist() && hasRole) {
//                    logger.info(sendee.getName() + " was denied access cause they had the role.");
//                    sendee.sendPrivateMessage("Sorry but `" + sendee.getGuild().getName() + " doesn't have that command enabled for your roles.");
//                    e.setCancelled();
//                } else if (!config.isBlacklist() && !hasRole) {
//                    logger.info(sendee.getName() + " was denied access cause they didn't have the role.");
//                    sendee.sendPrivateMessage("Sorry but `" + sendee.getGuild().getName() + " doesn't have that command enabled for your roles.");
//                    e.setCancelled();
//                }
//            }
//
//            if (config.hasCooldown()) {
//                Map<Long, Long> times = cooldowns.computeIfAbsent(sendee.getGuild().getIdLong(), map -> new HashMap<>());
//                if (!times.containsKey(sendee.getUser().getIdLong())) {
//                    times.put(sendee.getUser().getIdLong(), System.currentTimeMillis() + config.getCooldown());
//                } else {
//                    long reset = times.get(sendee.getUser().getIdLong());
//                    if (System.currentTimeMillis() > reset) {
//                        times.replace(sendee.getUser().getIdLong(), System.currentTimeMillis() + config.getCooldown());
//                    } else {
//                        logger.info(sendee.getName() + " was denied the ability to run command cause he was on cooldown for " + TimeUtil.formatDifference(reset, System.currentTimeMillis()));
//                        sendee.sendPrivateMessage("Sorry, but you are currently under cooldown in `" + sendee.getGuild().getName() + "` for `" + TimeUtil.formatDifference(reset, System.currentTimeMillis()) + "`.");
//                        e.setCancelled();
//                    }
//                }
//            }
//
//        } else {
//            logger.debug(String.format("CommandPreProcess >> Sender: %s, Args: %s, Flags: %s, Command: %s", e.getSender().getName(), e.getArgs(), e.getFlags(), e.getCommand().getName()));
//        }
//    }
//
//    @Override
//    public void onCommandPostProcess(CommandPostProcessEvent e) {
//        if (e.getResult() == CommandResult.UNKNOWN_COMMAND)
//            return;
//        if (e.getSender() instanceof GuildCommandSender) {
//            GuildCommandSender sendee = (GuildCommandSender) e.getSender();
//            logger.debug(String.format("CommandPostProcess >> Sender: %s#{%s}, Args: %s, Flags:%s, Command: %s, Result: %s", sendee.getMember().getEffectiveName(), sendee.getGuild().getName(), e.getArgs(), e.getFlags(), e.getCommand().getName(), e.getResult()));
//            Member bot = sendee.getGuild().getMember(sendee.getGuild().getJDA().getSelfUser());
//            if (sendee.getGuildConfig().shouldDeleteCommands() && bot.hasPermission(Permission.MESSAGE_MANAGE)) {
//                try {
//                    if (sendee.getMessage() != null)
//                        sendee.getMessage().delete().reason("Command message.").queue();
//                } catch (Exception ignored) {
//                }
//            }
//        } else {
//            logger.debug(String.format("CommandPostProcess >> Sender: %s, Args: %s, Flags:%s, Command: %s, Result: %s", e.getSender().getName(), e.getArgs(), e.getFlags(), e.getCommand().getName(), e.getResult()));
//        }
//        if (e.getResult() == CommandResult.INVALID_FLAGS)
//            e.getSender().sendMessage("Sorry, but you provided invalid flags for that command. {`" + e.getException().getMessage() + "`}");
//    }
//
//    @Override
//    public void onCommandException(CommandExceptionEvent e) {
//        e.getSender().sendMessage("Unfortunately an error has occurred with your request. The bot author has been notified.");
//        logger.error("An error occurred during command execution.", e.getException());
//    }
}
