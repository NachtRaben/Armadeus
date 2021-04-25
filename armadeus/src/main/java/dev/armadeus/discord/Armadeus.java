package dev.armadeus.discord;

import co.aikar.commands.CommandManager;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.PluginDescription;
import dev.armadeus.bot.api.ArmaCore;
import dev.armadeus.bot.api.command.DiscordCommand;
import dev.armadeus.discord.listeners.WelcomeListener;
import lombok.Getter;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Plugin(id = "armadeus", name = "Armadeus", version = "MASTER", url = "https://armadeus.com", description = "Utility Discord Bot", authors = {"NachtRaben"}, dependencies = {@Dependency (id = "arma-audio")})
public class Armadeus {

    private static final Logger logger = LoggerFactory.getLogger("Armadeus");

    private final ConcurrentHashMap<Long, Map<Long, Long>> cooldowns = new ConcurrentHashMap<>();
    @Getter
    private final ConcurrentHashMap<Long, Long> pings = new ConcurrentHashMap<>();

    private ArmaCore core;

    @Inject
    public Armadeus(ArmaCore core) {
        this.core = core;
    }

    @Subscribe
    public void registerJDAListeners(ShardManager manager) {
        manager.addEventListener(new WelcomeListener(core));
    }

    @Subscribe
    public void registerCommands(CommandManager manager) {
        logger.warn(getClass().getClassLoader().getClass().getName());
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .addClassLoader(getClass().getClassLoader())
                .setUrls(ClasspathHelper.forPackage("dev.armadeus.discord", getClass().getClassLoader()))
        );
        Set<Class<? extends DiscordCommand>> commandClazzes = reflections.getSubTypesOf(DiscordCommand.class);
        commandClazzes.forEach(clazz -> {
            try {
                if (!clazz.isSynthetic() && !clazz.isAnonymousClass() && !Modifier.isAbstract(clazz.getModifiers())) {
                    logger.info("Registering command class {}", clazz.getSimpleName());
                    manager.registerCommand(clazz.getConstructor().newInstance());
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
