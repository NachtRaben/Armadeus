package dev.armadeus.discord.moderation.util;

import dev.armadeus.bot.api.command.DiscordCommandIssuer;
import dev.armadeus.discord.moderation.objects.CommandActionData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static dev.armadeus.discord.moderation.util.FindGuildMember.multiSearch;

public class CommandAction {
    private static void printErrors( DiscordCommandIssuer issuer, String error ) {
        printErrors( issuer, new ArrayList<>( List.of( error ) ) );
    }

    private static void printErrors( DiscordCommandIssuer issuer, ArrayList<String> errors ) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor( Color.RED );
        embed.setAuthor( issuer.getGuild().getName() );
        embed.setThumbnail( issuer.getGuild().getIconUrl() );
        embed.setTitle( "Error Preforming Command." );
        errors.forEach( ( er ) -> embed.addField( er, " ", false ) );
        issuer.getUser().openPrivateChannel().queue( ch -> {
            ch.sendMessageEmbeds( embed.build() ).queue( msg -> msg.delete().queueAfter( 12L, TimeUnit.SECONDS ) );
        } );
    }

    private static void issue( CommandActionData actionData, ArrayList<Member> targets, BiConsumer<Member, String> consumer, Runnable fail ) {
        ArrayList<String> errors = new ArrayList<>();

        DiscordCommandIssuer issuer = actionData.getIssuer();
        if ( issuer == null ) return;

        if ( actionData.getConsume() ) {
            try {
                consumer.accept( null, null );
            } catch ( Exception e ) {
                e.printStackTrace();
                errors.add( actionData.getErrorStringOrElse( e.getMessage() ) );
                printErrors( issuer, errors );
            }
        }

        if ( targets == null ) { fail.run(); return; }
        if ( targets.isEmpty() ) { fail.run(); return; }

        targets.forEach( target -> {
            if ( target == null ) return;

            if ( !actionData.getCanTargetSelf() ) {
                if ( issuer.getUser().equals( target.getUser() ) ) {
                    errors.add( String.format( "%s skipped: You can't target yourself.\n", target.getAsMention() ) );
                    return;
                }
            }

            if ( !issuer.getMember().canInteract( target ) ) {
                errors.add( String.format( "%s skipped: You can't target that user.\n", target.getAsMention() ) );
                return;
            }

            String guildName = target.getGuild().getName();

            if ( actionData.getNotify() ) {
                try {
                    String reason = actionData.getReason();
                    EmbedBuilder embedBuilder = new EmbedBuilder();
                    embedBuilder.setTitle( String.format( actionData.getDescriptor(), guildName ) );
                    embedBuilder.setAuthor( guildName );
                    embedBuilder.setColor( Color.RED );

                    if ( reason != null && !reason.isEmpty() ) {
                        actionData.setReason( String.format( actionData.getReason(), target.getEffectiveName() ) );
                        embedBuilder.appendDescription( actionData.getReason() );
                    }

                    target.getUser().openPrivateChannel().flatMap( channel -> channel.sendMessageEmbeds( embedBuilder.build() ) )
                            .queue( ( e ) -> consumer.accept( target, actionData.getReason() ) );
                } catch ( Exception e ) {
                    e.printStackTrace();
                    errors.add( String.format( "%s skipped: Failed to notify users.\n", target.getAsMention() ) );
                }
            } else {
                consumer.accept( target, actionData.getReason() );
            }
        } );

        if ( errors.isEmpty() ) return;
        printErrors( issuer, errors );
    }

    private static void noUserFound( DiscordCommandIssuer issuer ) {
        printErrors( issuer, "No Target(s) Found." );
    }

    public static void issue( CommandActionData actionData, BiConsumer<Member, String> biConsumer ) {
        DiscordCommandIssuer issuer = actionData.getIssuer();
        String search = actionData.getSearch();
        boolean keywords = actionData.getKeywordsAllowed();
        issue( actionData, multiSearch( issuer, search, keywords ), biConsumer, () -> noUserFound( issuer ) );
    }

    public static void issue( CommandActionData actionData, Consumer<Member> consumer ) {
        DiscordCommandIssuer issuer = actionData.getIssuer();
        String search = actionData.getSearch();
        boolean keywords = actionData.getKeywordsAllowed();
        issue( actionData, multiSearch( issuer, search, keywords ), ( m, s ) -> consumer.accept( m ), () -> noUserFound( issuer ) );
    }
}
