package dev.armadeus.discord.moderation.util;

import dev.armadeus.bot.api.command.DiscordCommandIssuer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;

import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static dev.armadeus.bot.api.config.ArmaConfig.logger;
import static dev.armadeus.discord.moderation.util.FindGuildMember.multiSearch;

public class CommandAction {
    private static void issue( DiscordCommandIssuer user, ArrayList<Member> targets, boolean notify, String reason, String actionTitle, BiConsumer<Member, String> consumer ) {
        ArrayList<String> errors = new ArrayList<>();

        if ( targets == null ) return;
        if ( reason == null ) reason = "Unknown Reason";
        if ( actionTitle == null ) actionTitle = "Unknown Action";

        String nonNullReason = reason;
        String nonNullActionTitle = actionTitle;

        targets.forEach( target -> {
            if ( target == null ) return;

            if ( user.getUser().equals( target.getUser() ) ) {
                errors.add( String.format( "%s skipped: You can't target yourself.\n", target.getAsMention() ) );
                return;
            }

            if ( !user.getMember().canInteract( target ) ) {
                errors.add( String.format("%s skipped: You can't target that user.\n", target.getAsMention()) );
                return;
            }

            String finalReason = String.format( nonNullReason, target.getEffectiveName() );
            String guildName = target.getGuild().getName();

            if ( notify ) {
                try {
                    EmbedBuilder embedBuilder = new EmbedBuilder();
                    embedBuilder.setTitle( String.format( nonNullActionTitle, guildName ) );
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
        errors.forEach( ( er ) -> embed.addField( er, " ", false ) );
        user.getUser().openPrivateChannel().queue( ch -> {
            ch.sendMessageEmbeds( embed.build() ).queue( msg -> msg.delete().queueAfter( 12L, TimeUnit.SECONDS ) );
        } );
    }
    public static void issue( DiscordCommandIssuer user, String searchKey, boolean allowKeywords, boolean notify, String reason, String actionTitle, BiConsumer<Member, String> biConsumer ) {
        issue(user, multiSearch(user, searchKey, allowKeywords), notify, reason, actionTitle, biConsumer);
    }
    public static void issue( DiscordCommandIssuer user, String searchKey, boolean allowKeywords, boolean notify, String reason, String actionTitle, Consumer<Member> consumer ) {
        issue(user, multiSearch(user, searchKey, allowKeywords), notify, reason, actionTitle, ( m, s ) -> consumer.accept( m ));
    }
    public static void issue( DiscordCommandIssuer user, String searchKey, boolean allowKeywords, BiConsumer<Member, String> biConsumer ) {
        issue( user, FindGuildMember.multiSearch( user, searchKey, true ), false, null, null, biConsumer );
    }
    public static void issue( DiscordCommandIssuer user, String searchKey, boolean allowKeywords, Consumer<Member> consumer ) {
        issue( user, FindGuildMember.multiSearch( user, searchKey, true ), false, null, null, ( m, s ) -> consumer.accept(m) );
    }
}
