package dev.armadeus.core.util;

import dev.armadeus.core.command.DiscordUser;
import net.dv8tion.jda.api.EmbedBuilder;

public class EmbedUtil {

    public static EmbedBuilder newBuilder(DiscordUser user) {
        EmbedBuilder builder = new EmbedBuilder();
        if(user.isFromGuild()) {
            builder.setColor(user.getGuild().getSelfMember().getRoles().get(0).getColor());
        } else {
            builder.setColor(Utils.randomColor());
        }
        return builder;
    }

}
