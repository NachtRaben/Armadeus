package dev.armadeus.discord.moderation.util;

import dev.armadeus.bot.api.command.DiscordCommandIssuer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import static dev.armadeus.bot.api.config.ArmaConfig.logger;

public class NotifyAction {
    public void send( DiscordCommandIssuer user, String userID, boolean notify, String reason, String actionTitle, BiConsumer<Member, String> consumer ) {
        HashSet<Member> targets = new HashSet<>();
        ArrayList<String> errors = new ArrayList<>();

        String[] userIds = userID.split(",");

        Arrays.stream( userIds ).forEach( uid -> {
            try {
                Member target = user.getGuild().getMemberById( uid.trim() );
                if ( target != null ) targets.add( target );
            } catch ( NumberFormatException e ){
                logger.error( e.getMessage() );
            }
        } );

        targets.forEach( target -> {
            if ( user.getUser().equals( target.getUser() ) ) {
                errors.add( String.format( "%s skipped: You can't target yourself.\n", target.getAsMention() ) );
                return;
            }

            if ( !user.getMember().canInteract( target ) ) {
                errors.add( String.format("%s skipped: You can't target that user.\n", target.getAsMention()) );
                return;
            }

            String finalReason = String.format( reason, target.getEffectiveName() );
            String guildName = target.getGuild().getName();

            if ( notify ) {
                try {
                    EmbedBuilder embedBuilder = new EmbedBuilder();
                    embedBuilder.setTitle( String.format( actionTitle, guildName ) );
                    embedBuilder.setAuthor( guildName );
                    embedBuilder.setColor( Color.RED );
                    embedBuilder.appendDescription( finalReason );

                    target.getUser().openPrivateChannel().flatMap( channel -> channel.sendMessageEmbeds( embedBuilder.build() ) )
                            .queue( ( e ) -> consumer.accept( target, finalReason ) );
                } catch ( Exception e ) {
                    logger.error( e.getMessage(), e );
                    errors.add( String.format("%s skipped: Failed to notify users.\n", target.getAsMention()) );
                }
            } else {
                consumer.accept( target, finalReason );
            }
        });

        if ( errors.isEmpty() ) return;

        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor( Color.RED );
        embed.setAuthor( user.getGuild().getName() );
        embed.setThumbnail( user.getGuild().getIconUrl() );
        embed.setTitle( "Error Preforming Command." );
        errors.forEach( ( er ) -> embed.addField( " ", er, false ) );
        user.getUser().openPrivateChannel().queue( ch -> {
            ch.sendMessageEmbeds( embed.build() ).queue( msg -> msg.delete().queueAfter( 12L, TimeUnit.SECONDS ) );
        } );
    }
}
