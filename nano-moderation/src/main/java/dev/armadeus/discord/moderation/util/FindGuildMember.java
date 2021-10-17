package dev.armadeus.discord.moderation.util;

import dev.armadeus.bot.api.command.DiscordCommandIssuer;
import dev.armadeus.discord.moderation.objects.CommandActionData;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class FindGuildMember {
    private static ArrayList<Member> search( DiscordCommandIssuer issuer, Guild guild, String search, boolean allowKeywords ) {
        if ( search.isEmpty() || search.isBlank() ) return null;

        try {
            if ( search.matches( "\\d+" ) ) {
                Member memberById = guild.getMemberById( search );
                if ( memberById != null ) return new ArrayList<>( List.of( memberById ) );
            }

            Member memberByTag = guild.getMemberByTag( search );
            if ( memberByTag != null ) return new ArrayList<>( List.of( memberByTag ) );

            ArrayList<Member> membersByName = (ArrayList<Member>) guild.getMembersByEffectiveName( search, true );
            if ( !membersByName.isEmpty() ) return membersByName;
        } catch ( IllegalArgumentException ignored ) {
        }

        if ( allowKeywords ) {
            if ( search.equals( "*" ) ) {
                return guild.getMembers().stream().filter( Objects::nonNull ).collect( Collectors.toCollection( ArrayList::new ) );
            }
            if ( search.equals( "^" ) ) {
                return new ArrayList<>( List.of( issuer.getMember() ) );
            }
            if ( search.startsWith( "?" ) ) {
                String regex = search.substring( 1 ).toLowerCase();
                Optional<Member> oMember = guild.getMembers().stream()
                        .filter( m -> m.getEffectiveName().toLowerCase().matches( regex ) )
                        .collect( Collectors.toList() ).stream().findFirst();
                return oMember.map( member -> new ArrayList<>( List.of( member ) ) ).orElse( null );
            }
        }

        return null;
    }

    private static ArrayList<Member> search( DiscordCommandIssuer issuer, Guild guild, String search, Message message, boolean allowKeywords ) {
        ArrayList<Member> members = search( issuer, guild, search, allowKeywords );
        if ( members != null ) return members;

        List<IMentionable> mentions = message.getMentions( Message.MentionType.USER );
        if ( mentions.isEmpty() ) return null;

        List<Member> memberMentions = mentions.stream().filter( m -> m instanceof Member ).map( Member.class::cast ).collect( Collectors.toList() );
        if ( !memberMentions.isEmpty() ) return (ArrayList<Member>) memberMentions;

        return null;
    }

    private static ArrayList<Member> multiSearch( DiscordCommandIssuer issuer, Guild guild, String search, Message message, boolean allowKeywords ) {
        ArrayList<Member> results = new ArrayList<>();
        String[] split = search.split( "," );

        Arrays.stream( split ).forEach( tag -> {
            List<Member> searchList = search( issuer, guild, tag.trim(), message, allowKeywords );
            if ( searchList == null ) return;
            results.addAll( searchList );
        } );

        return results;
    }

    @Nullable
    public static ArrayList<Member> search( DiscordCommandIssuer issuer, String search, boolean allowKeywords ) {
        return search( issuer, issuer.getGuild(), search, issuer.getMessage(), allowKeywords );
    }

    @Nullable
    public static ArrayList<Member> search( CommandActionData actionData ) {
        return search( actionData.getIssuer(), actionData.getIssuer().getGuild(), actionData.getSearch(),
                actionData.getIssuer().getMessage(), actionData.getKeywordsAllowed() );
    }

    @NotNull
    public static ArrayList<Member> multiSearch( DiscordCommandIssuer issuer, String search, boolean allowKeywords ) {
        return multiSearch( issuer, issuer.getGuild(), search, issuer.getMessage(), allowKeywords );
    }

    @NotNull
    public static ArrayList<Member> multiSearch( CommandActionData actionData ) {
        return multiSearch( actionData.getIssuer(), actionData.getIssuer().getGuild(), actionData.getSearch(),
                actionData.getIssuer().getMessage(), actionData.getKeywordsAllowed() );
    }
}
