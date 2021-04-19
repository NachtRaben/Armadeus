package dev.armadeus.bot.commands.general;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import dev.armadeus.core.command.DiscordCommand;
import dev.armadeus.core.command.DiscordUser;
import dev.armadeus.core.util.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;

public class PingCommand extends DiscordCommand {

    @Default
    @CommandAlias("ping")
    public void ping(DiscordUser user) {
        user.getUser().getJDA().getRestPing().queue(ping -> {
            EmbedBuilder eb = EmbedUtils.newBuilder(user);
            eb.setTitle("Ping: ");
            eb.addField("API :ping_pong:", ping + " ms.", true);
            eb.addField("WebSocket :musical_note:", user.getJda().getGatewayPing() + " ms.", true);
            user.sendMessage(eb.build());
        });
    }

}

