package dev.armadeus.discord.moderation.util;

import com.electronwill.nightconfig.core.Config;
import dev.armadeus.discord.moderation.ArmaModeration;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static dev.armadeus.bot.api.config.ArmaConfig.logger;
import static dev.armadeus.discord.moderation.ArmaModeration.httpClient;

public class MessageAction {
    private Config config = null;
    private final Map<Guild, List<Member>> mutedMembersInGuild = new ConcurrentHashMap<>();

    public void setGuild( Guild guild ) {
        config = ArmaModeration.get().getConfig( guild );
        if ( config == null ) return;
        if ( !mutedMembersInGuild.containsKey( guild ) ) {
            mutedMembersInGuild.put( guild, new ArrayList<>() );
        }
    }

    public void profanityCheckMessage( Message msg, boolean edited ) {
        Guild guild = msg.getGuild();
        Member member = msg.getMember();
        TextChannel textChannel = guild.getTextChannelById( config.get( "msgChannel" ) );

        if ( member == null ) return;
        if ( textChannel == null ) return;
        if ( member.getUser().isBot() || member.getUser().isSystem() ) return;

        HttpRequest request = HttpRequest.newBuilder()
                .POST(  HttpRequest.BodyPublishers.ofString("data=" + msg.getContentDisplay() ) )
                .uri( URI.create("http://127.0.0.1:5000/") )
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build();
        try {
            int sureness;

            HttpResponse<?> re = httpClient.send( request, HttpResponse.BodyHandlers.ofString() );
            if ( re.statusCode() != 200 ) throw new Exception("Status Code Check Failed");

            try {
                sureness = (int) (Double.parseDouble( re.body().toString() ) * 100);
            } catch ( NumberFormatException e ) {
                sureness = 0;
            }

            if ( sureness > 70 ) {
                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setTitle( String.format("Sureness: %d%%", sureness) );
                embedBuilder.setAuthor( member.getUser().getAsTag(), msg.getJumpUrl(), member.getUser().getEffectiveAvatarUrl() );
                embedBuilder.setFooter( member.getUser().getId() );
                embedBuilder.setTimestamp( msg.getTimeCreated() );
                if ( edited ) embedBuilder.setDescription( "(edited)" );

                MessageBuilder messageBuilder = new MessageBuilder();
                messageBuilder.setEmbeds( embedBuilder.build() );
                messageBuilder.append( "```" ).append( msg.getContentDisplay() ).append( "```" );

                textChannel.sendMessage( messageBuilder.build() ).queue();
            }
        } catch (Exception e) {
            e.printStackTrace();
            textChannel.sendMessage(
                    String.format( "ERROR: Couldn't Parse Message for <@%s>", member.getUser().getId() )
            ).queue();
        }
    }

    public void muteMemberInGuild( Message msg, long muteTime ) {
        Guild guild = msg.getGuild();
        Member member = msg.getMember();
        String muteMsg = "**Spam Detected** in %s#%s!\nYou have been muted for **%d** Seconds.";
        List<Member> mutedMembers = mutedMembersInGuild.getOrDefault( guild, null );

        if ( member == null ) return;
        if ( config == null ) return;
        if ( mutedMembers == null ) return;

        Role muteRole = guild.getRoleById( config.get( "muteRoleID" ) );
        if ( muteRole == null ) return;

        member.getUser().openPrivateChannel().queue( pc ->
                pc.sendMessage( String.format( muteMsg, guild.getName(), msg.getChannel().getName(), muteTime ) )
                        .queue( e -> e.delete().queueAfter( muteTime, TimeUnit.SECONDS ) )
        );

        if ( mutedMembers.contains( member ) ) return;
        guild.addRoleToMember( member, muteRole ).queue( e -> {
            mutedMembers.add( member );
            logger.info( String.format("%s( %s ) has been muted for %d seconds.", member.getUser().getName(), member.getUser(), muteTime) );
            guild.removeRoleFromMember( member, muteRole ).queueAfter( muteTime, TimeUnit.SECONDS, x -> mutedMembers.remove( member ) );
        });
    }

    public void removeMutedMembers( Guild guild ) {
        if ( config == null ) return;

        Role muteRole = guild.getRoleById( config.get( "muteRoleID" ) );
        if ( muteRole == null ) return;

        List<Member> mutedMembers = mutedMembersInGuild.getOrDefault( guild, null );

        if ( mutedMembers != null ) {
            mutedMembers.forEach( member -> guild.removeRoleFromMember( member, muteRole ).queue() );
            mutedMembers.clear();
        }

        mutedMembersInGuild.remove( guild );
    }
}
