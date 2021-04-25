package dev.armadeus.bot.api.util;

import dev.armadeus.bot.api.command.DiscordCommandIssuer;
import net.dv8tion.jda.api.EmbedBuilder;

public class EmbedUtils {

    public static EmbedBuilder newBuilder(DiscordCommandIssuer user) {
        EmbedBuilder builder = new EmbedBuilder();
        if (user.isFromGuild()) {
            builder.setColor(user.getGuild().getSelfMember().getRoles().get(0).getColor());
        }
        return builder;
    }
}
