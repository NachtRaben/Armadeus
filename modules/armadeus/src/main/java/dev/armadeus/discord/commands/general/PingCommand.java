package dev.armadeus.discord.commands.general;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import dev.armadeus.bot.api.command.DiscordCommand;
import dev.armadeus.bot.api.command.DiscordCommandIssuer;
import dev.armadeus.discord.util.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;

public class PingCommand extends DiscordCommand {

    @Default
    @CommandAlias("ping")
    @CommandPermission("armadeus.ping")
    public void ping(DiscordCommandIssuer user) {
        user.getUser().getJDA().getRestPing().queue(ping -> {
            EmbedBuilder eb = EmbedUtils.newBuilder(user);
            eb.setTitle("Ping: ");
            eb.addField("API :ping_pong:", ping + " ms.", true);
            eb.addField("WebSocket :musical_note:", user.getJda().getGatewayPing() + " ms.", true);
            user.sendMessage(eb.build());
        });
    }

}

