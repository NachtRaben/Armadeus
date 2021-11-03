package dev.armadeus.discord.moderation.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Description;
import com.electronwill.nightconfig.core.Config;
import com.google.common.base.Joiner;
import dev.armadeus.bot.api.command.DiscordCommand;
import dev.armadeus.bot.api.command.DiscordCommandIssuer;
import dev.armadeus.discord.moderation.ArmaModeration;
import dev.armadeus.discord.moderation.util.GuildLogger;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Conditions("guildonly")
public class UserCommands extends DiscordCommand {

    @CommandAlias( "lsar" )
    @Description( "List self assignable roles." )
    @CommandPermission("armadeus.lsar")
    public void lsar( DiscordCommandIssuer user ) {
        Config config = ArmaModeration.get().getConfig(user.getGuild());
        if ( config == null ) return;

        ArrayList<String> rolesAssignable = new ArrayList<>();
        ArrayList<Long> assignableRoles = config.get( "assignableRoles" );

        assignableRoles.forEach( roleId -> {
            Role role = user.getGuild().getRoleById( roleId );
            if ( role == null ) return;
            rolesAssignable.add( String.format("`%s`", role.getName()) );
        });

        MessageBuilder messageBuilder = new MessageBuilder();
        Joiner joiner = Joiner.on( ", " ).skipNulls();
        messageBuilder.append( "__**Assignable Roles**__\n" );
        if ( !rolesAssignable.isEmpty() ) {
            joiner.appendTo( messageBuilder.getStringBuilder(), rolesAssignable );
        } else {
            messageBuilder.append( "No Assignable Roles Found." );
        }

        if ( user.isSlash() ) {
            user.sendMessage( messageBuilder.build() );
            return;
        }

        try {
            user.sendPrivateMessage( messageBuilder.build() );
        } catch( ErrorResponseException ignore ) {}
    }

    private void processRoles( Color colour, String title, String description, String separator, DiscordCommandIssuer user, String role_names, Consumer<Role> consumer ){
        boolean breakMe = false;
        Config config = ArmaModeration.get().getConfig(user.getGuild());
        if ( config == null ) return;

        AtomicBoolean notFounds = new AtomicBoolean( false );
        ArrayList<Long> assignableRoles = config.get( "assignableRoles" );
        ArrayList<Role> roles = new ArrayList<>();

        String[] rolesByName = role_names.split(",");
        int amount = 0;

        for ( String role : rolesByName ) {
            if ( amount++ > 5 ) break;
            List<Role> roleList = user.getGuild().getRolesByName( role.trim(), true );

            if ( roleList.isEmpty() ) {
                breakMe = true;
                break;
            }

            Role foundRole = roleList.get( 0 );
            if ( foundRole != null ) roles.add( foundRole );
        }

        if ( breakMe ) {
            String msg = user.getUser().getAsMention() + ": Invalid role(s) specified. If you'd like to self-assign " +
            "multiple roles use commas (`,`) to separate them.\nie. `/iam role1, role2, role3`\n\nUse `/lsar` for a " +
            "list of self assignable roles." ;
            user.sendMessage( msg );
            return;
        }

        if ( roles.isEmpty() ) {
            user.sendMessage( user.getUser().getAsMention() + ": No assignable roles found. Try running `/lsar` for a list of assignable roles!" );
            return;
        }

        roles.forEach( role -> {
            if ( !assignableRoles.contains( role.getIdLong() ) ){
                roles.remove( role );
                notFounds.set( true );
                return;
            }
            consumer.accept( role );
        } );

        EmbedBuilder embedBuilder = new EmbedBuilder();
        Joiner joiner = Joiner.on( separator ).skipNulls();

        embedBuilder.setAuthor( "Role Manager", "https://mirai.red/", "https://mirai.red/resource/roleicon.png" );
        embedBuilder.setTitle( title, "https://mirai.red/" );
        embedBuilder.appendDescription( user.getUser().getAsMention() );
        embedBuilder.appendDescription( description + separator );

        joiner.appendTo( embedBuilder.getDescriptionBuilder(),
                roles.stream().flatMap( role -> Stream.of( role.getAsMention() ) ).collect( Collectors.toList() ) );

        if ( notFounds.get() ) {
            embedBuilder.appendDescription( "\n__Some roles were not found.__" );
        }

        embedBuilder.setColor( colour );
        embedBuilder.setFooter( user.getUser().getAsTag(), user.getUser().getEffectiveAvatarUrl() );

        StringBuilder logMessage = new StringBuilder( user.getUser().getAsTag() );
        logMessage.append( "; " ).append( title ).append( "; " );

        joiner = Joiner.on(", ").skipNulls();
        joiner.appendTo( logMessage,
                roles.stream().flatMap( role ->
                        Stream.of( String.format( "@%s", role.getName() ) ) ).collect( Collectors.toList() ) );

        GuildLogger.log( user.getGuild(), logMessage.toString() );
        user.sendMessage( embedBuilder.build() );
    }

    @CommandAlias( "iam" )
    @Description( "Request self assignable roles, comma separated." )
    @CommandPermission("armadeus.iam")
    public void iam( DiscordCommandIssuer user, String role_names ) {
        processRoles( Color.GREEN, "Roles Granted!", " now has the following roles!", "\n[+] ",
                user, role_names,  role -> user.getGuild().addRoleToMember( user.getMember(), role ).queue() );
    }

    @CommandAlias( "iamn" )
    @Description( "Remove self assignable roles, comma separated." )
    @CommandPermission("armadeus.iamn")
    public void iamnot( DiscordCommandIssuer user, String role_names ) {
        processRoles( Color.RED, "Roles Removed!", " no longer has the following roles!", "\n[-] ",
                user, role_names, role -> user.getGuild().removeRoleFromMember( user.getMember(), role ).queue() );
    }
}
