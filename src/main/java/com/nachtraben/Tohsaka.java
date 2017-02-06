package com.nachtraben;

import com.nachtraben.command.CmdBase;
import com.nachtraben.command.commands.AdminCommands;
import com.nachtraben.command.commands.AudioCommands;
import com.nachtraben.command.commands.MiscCommands;
import com.nachtraben.command.sender.ConsoleCommandSender;
import com.nachtraben.events.GuildMessageEvents;
import com.nachtraben.events.PrivateMessageEvents;
import com.nachtraben.log.JDALogListener;
import com.nachtraben.log.LogManager;
import com.nachtraben.utils.Reference;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.utils.SimpleLog;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.Collections;

import static com.nachtraben.utils.Utils.format;

/**
 * Created by NachtRaben on 1/19/2017.
 */
public class Tohsaka {

    public static Tohsaka instance;
    private int shardcount;
    private ArrayList<JDA> jdas;

    public boolean debug;

    public static CmdBase commandHandler;

    private Thread console;

    public boolean running = true;

    public Tohsaka(int shards, boolean debug) {
        this.debug = debug;
        this.shardcount = shards;
        jdas = new ArrayList<>();
        LogManager.TOHSAKA.info("Initializing Tohsaka.");
        SimpleLog.addListener(new JDALogListener());
        instance = this;
        commandHandler = new CmdBase();
        if (debug) CmdBase.COMMAND_INIT_CHARS = new ArrayList<>(Collections.singletonList('-'));

        LogManager.TOHSAKA.info("Registering base commands.");
        commandHandler.registerCommands(new AdminCommands());
        commandHandler.registerCommands(new AudioCommands());
        commandHandler.registerCommands(new MiscCommands());

        registerJDAs();

        console = new Thread(ConsoleCommandSender.getInstance());
        console.start();
    }

    public void registerJDAs() {
        for (int i = 0; i < shardcount; i++) {
            LogManager.TOHSAKA.info(format("Creating shard { %s }.", i));
            try {
                JDABuilder jdab = new JDABuilder(AccountType.BOT)
                        .setToken(Reference.CLIENT_SECRET)
                        .setBulkDeleteSplittingEnabled(true)
                        .addListener(new GuildMessageEvents())
                        .addListener(new PrivateMessageEvents());
                if (shardcount > 1) {
                    jdab.useSharding(i, shardcount);
                    jdab.setGame(Game.of(format("Shard %s/%s", i, shardcount)));
                }
                jdas.add(i, jdab.buildAsync());
            } catch (LoginException e) {
                LogManager.TOHSAKA.error(format("Failed to log in shard { %s }!", i), e);
            } catch (RateLimitedException e) {
                LogManager.TOHSAKA.error(format("Failed to log in shard { %s } due to a rate limit exception! We should probably retry...", i), e);
            }
        }
    }

    public JDA getJDA(int shard) {
        if (shard > shardcount) {
            return null;
        } else
            return jdas.get(shard);
    }

    public void stop() {
        running = false;
        for (JDA jda : jdas) {
            jda.shutdownNow(true);
        }
        try {
            console.join(250);
        } catch (InterruptedException e) {
            LogManager.TOHSAKA.warn("Console did not shut down gracefully!");
        }
        System.exit(0);
    }


    public static void main(String[] args) {
        new Tohsaka(1, false);
    }

}
