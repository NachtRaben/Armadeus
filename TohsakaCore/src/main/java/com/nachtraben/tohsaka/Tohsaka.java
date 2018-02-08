package com.nachtraben.tohsaka;

import com.nachtraben.core.DiscordBot;
import com.nachtraben.core.command.GuildCommandSender;
import com.nachtraben.core.configuration.GuildConfig;
import com.nachtraben.core.util.TimeUtil;
import com.nachtraben.orangeslice.CommandResult;
import com.nachtraben.orangeslice.command.Cmd;
import com.nachtraben.orangeslice.command.Command;
import com.nachtraben.orangeslice.command.CommandTree;
import com.nachtraben.orangeslice.event.CommandEventListener;
import com.nachtraben.orangeslice.event.CommandExceptionEvent;
import com.nachtraben.orangeslice.event.CommandPostProcessEvent;
import com.nachtraben.orangeslice.event.CommandPreProcessEvent;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Tohsaka extends DiscordBot implements CommandEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(Tohsaka.class);

    private static Tohsaka instance;

    private ConcurrentHashMap<Long, Map<Long, Long>> cooldowns = new ConcurrentHashMap<>();

    public Tohsaka(String[] args, boolean debugging) {
        super(args);
        instance = this;

        setDebugging(debugging);
        long start = System.currentTimeMillis();
        LOGGER.debug("Took " + (System.currentTimeMillis() - start) + "ms to load all shards.");
        registerCommands();
        getCommandBase().registerEventListener(this);
        getShardManager().connectAllShards();
        postStart();
    }

    private void registerCommands() {
        Reflections reflections = new Reflections(
                new ConfigurationBuilder()
                        .setUrls(ClasspathHelper.forPackage("com.nachtraben.tohsaka.commands"))
                        .setScanners(
                                new SubTypesScanner(),
                                new MethodAnnotationsScanner()
                        ).filterInputsBy(new FilterBuilder().includePackage("com.nachtraben.tohsaka.commands"))
        );
        Set<Class<?>> classes = new HashSet<>();
        for (Method m : reflections.getMethodsAnnotatedWith(Cmd.class)) {
            if (!classes.contains(m.getDeclaringClass())) {
                classes.add(m.getDeclaringClass());
                try {
                    getCommandBase().registerCommands(m.getDeclaringClass().getConstructor().newInstance());
                } catch (InstantiationException | IllegalAccessException e) {
                    LOGGER.error("Failed to register command class, " + m.getDeclaringClass() + ".", e);
                } catch (NoSuchMethodException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
        for (Class s : reflections.getSubTypesOf(Command.class)) {
            try {
                if (!s.isSynthetic() && !s.isAnonymousClass() && !Modifier.isAbstract(s.getModifiers()))
                    getCommandBase().registerCommands(s.getConstructor().newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                LOGGER.error("Failed to instantiate command class, " + s.getSimpleName() + ".", e);
            } catch (NoSuchMethodException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        for (Class s : reflections.getSubTypesOf(CommandTree.class)) {
            try {
                getCommandBase().registerCommands(s.getConstructor().newInstance());
            } catch (IllegalAccessException | InstantiationException e) {
                LOGGER.error("Failed to instantiate command class, " + s.getSimpleName() + ".", e);
            } catch (NoSuchMethodException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    public static Tohsaka getInstance() {
        return instance;
    }

    public static void main(String[] args) {
        new Tohsaka(args, false);
    }

    @Override
    public void onCommandPreProcess(CommandPreProcessEvent e) {
        if (e.getSender() instanceof GuildCommandSender) {
            GuildCommandSender sendee = (GuildCommandSender) e.getSender();
            GuildConfig config = sendee.getGuildConfig();
            LOGGER.debug(String.format("CommandPreProcess >> Sender: %s#{%s}, Args: %s, Flags: %s, Command: %s", sendee.getMember().getEffectiveName(), sendee.getGuild().getName(), e.getArgs(), e.getFlags(), e.getCommand().getName()));
            if (sendee.getMember().isOwner() || sendee.getMember().hasPermission(Permission.ADMINISTRATOR) || getConfig().getOwnerIDs().contains(sendee.getUserID()) || getConfig().getDeveloperIDs().contains(sendee.getUserID()))
                return;

            Map<String, Set<Long>> blacklistedCommands = config.getDisabledCommands();
            if (blacklistedCommands.containsKey(e.getCommand().getName())) {
                Set<Long> blacklistedRoles = blacklistedCommands.get(e.getCommand().getName());
                boolean hasRole = blacklistedRoles.contains(-1L) || sendee.getMember().getRoles().stream().anyMatch(role -> blacklistedRoles.contains(role.getIdLong()));
                if (config.isBlacklist() && hasRole) {
                    LOGGER.info(sendee.getName() + " was denied access cause they had the role.");
                    sendee.sendPrivateMessage("Sorry but `" + sendee.getGuild().getName() + " doesn't have that command enabled for your roles.");
                    e.setCancelled();
                } else if (!config.isBlacklist() && !hasRole) {
                    LOGGER.info(sendee.getName() + " was denied access cause they didn't have the role.");
                    sendee.sendPrivateMessage("Sorry but `" + sendee.getGuild().getName() + " doesn't have that command enabled for your roles.");
                    e.setCancelled();
                }
            }

            if (config.hasCooldown()) {
                Map<Long, Long> times = cooldowns.computeIfAbsent(sendee.getGuildId(), map -> new HashMap<>());
                if (!times.containsKey(sendee.getUserID())) {
                    times.put(sendee.getUserID(), System.currentTimeMillis() + config.getCooldown());
                } else {
                    long reset = times.get(sendee.getUserID());
                    if (System.currentTimeMillis() > reset) {
                        times.replace(sendee.getUserID(), System.currentTimeMillis() + config.getCooldown());
                    } else {
                        LOGGER.info(sendee.getName() + " was denied the ability to run command cause he was on cooldown for " + TimeUtil.fromLong(reset - System.currentTimeMillis(), TimeUtil.FormatType.STRING));
                        sendee.sendPrivateMessage("Sorry, but you are currently under cooldown in `" + sendee.getGuild().getName() + "` for `" + TimeUtil.fromLong(reset - System.currentTimeMillis(), TimeUtil.FormatType.STRING) + "`.");
                        e.setCancelled();
                        return;
                    }
                }
            }

        } else {
            LOGGER.debug(String.format("CommandPreProcess >> Sender: %s, Args: %s, Flags: %s, Command: %s", e.getSender().getName(), e.getArgs(), e.getFlags(), e.getCommand().getName()));
        }
    }

    @Override
    public void onCommandPostProcess(CommandPostProcessEvent e) {
        if (e.getResult().equals(CommandResult.UNKNOWN_COMMAND))
            return;
        if (e.getSender() instanceof GuildCommandSender) {
            GuildCommandSender sendee = (GuildCommandSender) e.getSender();
            LOGGER.debug(String.format("CommandPostProcess >> Sender: %s#{%s}, Args: %s, Flags:%s, Command: %s, Result: %s", sendee.getMember().getEffectiveName(), sendee.getGuild().getName(), e.getArgs(), e.getFlags(), e.getCommand().getName(), e.getResult()));
            Member bot = sendee.getGuild().getMember(sendee.getGuild().getJDA().getSelfUser());
            if (sendee.getGuildConfig().shouldDeleteCommands() && bot.hasPermission(Permission.MESSAGE_MANAGE)) {
                try {
                    if (sendee.getMessage() != null)
                        sendee.getMessage().delete().reason("Command message.").queue();
                } catch (Exception ignored) {
                }
            }
        } else {
            LOGGER.debug(String.format("CommandPostProcess >> Sender: %s, Args: %s, Flags:%s, Command: %s, Result: %s", e.getSender().getName(), e.getArgs(), e.getFlags(), e.getCommand().getName(), e.getResult()));
        }
        if (e.getResult().equals(CommandResult.INVALID_FLAGS))
            e.getSender().sendMessage("Sorry, but you provided invalid flags for that command. {`" + e.getException().getMessage() + "`}");
    }

    @Override
    public void onCommandException(CommandExceptionEvent e) {
        e.getSender().sendMessage("Unfortunately an error has occurred with your request. The bot author has been notified.");
        LOGGER.error("An error occurred during command execution.", e.getException());
    }
}
