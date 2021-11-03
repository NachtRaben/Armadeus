package dev.armadeus.discord.moderation.util;

import com.electronwill.nightconfig.core.Config;
import dev.armadeus.discord.moderation.ArmaModeration;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

import static dev.armadeus.bot.api.config.ArmaConfig.logger;

public class GuildLogger {
    public enum LogType {
        DEFAULT( "\u2139 " ),
        WARNING( "\u26A0 " ),
        LINK( "\uD83D\uDD17 " ),
        BAN( "\u2692 " ),
        ERROR( "\u2049 " );

        private final String value;

        LogType( String value ) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public static void log( Guild guild, String data, LogType type, boolean printOut ) {
        Config config = ArmaModeration.get().getConfig( guild );
        if ( config == null ) return;

        long channelId = config.getLongOrElse( "logsChannel", 0 );
        if ( channelId == 0 ) return;

        TextChannel textChannel = guild.getTextChannelById( channelId );
        if ( textChannel == null ) return;

        if ( data.isEmpty() || data.isBlank() ) return;
        data = data.replaceAll( "[\n\r]", "; " );

        StringBuilder sb = new StringBuilder();
        sb.append( type.getValue() ).append( data );
        textChannel.sendMessage( sb.toString() ).allowedMentions( List.of() ).queue();

        if ( printOut ) logger.info( data );
    }

    public static void log( Guild guild, String data ) {
        log( guild, data, LogType.DEFAULT, true );
    }
}
