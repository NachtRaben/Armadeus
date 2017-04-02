package com.nachtraben.core;

import com.nachtraben.commandapi.CommandBase;
import com.nachtraben.core.configuration.BotConfig;
import com.nachtraben.core.listeners.CommandListener;
import com.nachtraben.core.listeners.JDALogListener;
import com.nachtraben.tohsaka.Tohsaka;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class JDABot {

    private static final Logger logger = LoggerFactory.getLogger(JDABot.class);

    private static JDABot instance;

    private CommandBase cmdBase;
	private BotConfig config;
	private List<JDA> jdas;

	private TextChannel globalLogChannel;

	protected JDABot() {
		// TODO: Phase this out at some point for multiple bots from 1 program?
        if(instance != null) throw new RuntimeException("There may only be 1 instance of JDABot!");
        instance = this;
        new JDALogListener();
        config = new BotConfig().load();
        cmdBase = new CommandBase();
    }

    protected void loadJDAs(Object... listeners) {
        jdas = new ArrayList<>();
        if(config.getShardCount() == 1) {
            try {
                jdas.add(0, new JDABuilder(AccountType.BOT).setToken(config.getToken()).addListener(CommandListener.instance).addListener(listeners).buildAsync());
            } catch (LoginException | RateLimitedException e) {
                e.printStackTrace();
            }
        } else {
            JDABuilder builder = new JDABuilder(AccountType.BOT).setToken(config.getToken()).addListener(CommandListener.instance);
            for(int i = 0; i < config.getShardCount(); i++) {
                try {
                    jdas.add(i, builder.useSharding(i, config.getShardCount()).addListener(listeners).setGame(Game.of(i + "/" + config.getShardCount())).buildAsync());
                } catch (LoginException | RateLimitedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public User getUser(String id) {
        User user;
        for(JDA jda : jdas) {
            if((user = jda.getUserById(id)) != null) return user;
        }
        return null;
    }

    public CommandBase getCommandHandler() {
        return cmdBase;
    }

    public Guild getGuildById(String id) {
        Guild guild;
        for(JDA jda : jdas) {
            if((guild = jda.getGuildById(id)) != null) return guild;
        }
        return null;
    }

    public List<JDA> getJDAs() {
        return jdas;
    }

    public void shutdown() {
		config.save();
        for(JDA jda : jdas) {
            jda.shutdown();
        }
        System.exit(0);
    }

    public static JDABot getInstance() {
        return instance;
    }

    public TextChannel getGlobalLogChannel() {
        if(globalLogChannel != null) return globalLogChannel;
        if(config.getGlobalLogChannel() != null) {
            TextChannel chan = null;
            for(JDA jda : jdas) {
                if(jda != null)
                    chan = jda.getTextChannelById(config.getGlobalLogChannel());
                if(chan != null) break;
            }
            return chan;
        }
        return null;
    }

    public List<String> getDefaultCommandPrefixes() {
		if(Tohsaka.debug) return Collections.singletonList("-");
		return config.getDefaultCommandPrefixes();
	}

    public void setGlobalLogChannel(TextChannel channel) {
        config.setGlobalLogChannel(channel.getId());
        config.save();
        globalLogChannel = channel;
    }

}
