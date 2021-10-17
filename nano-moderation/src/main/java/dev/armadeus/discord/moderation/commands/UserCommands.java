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
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Role;

import java.awt.*;
import java.util.ArrayList;

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
        messageBuilder.append( "\n------------" );

        user.getUser().openPrivateChannel().queue( ch -> ch.sendMessage( messageBuilder.build() ).queue() );
    }

    @CommandAlias( "iam" )
    @Description( "Get self assignable role." )
    @CommandPermission("armadeus.iam")
    public void iam( DiscordCommandIssuer user, String roleName ) {
        Config config = ArmaModeration.get().getConfig(user.getGuild());
        if ( config == null ) return;

        ArrayList<Long> assignableRoles = config.get( "assignableRoles" );
        Role role = user.getGuild().getRolesByName( roleName, true ).get( 0 );

        if ( role == null ) {
            user.sendMessage( "Role not found.", 7000L );
        } else {
            if ( !assignableRoles.contains( role.getIdLong() ) ){
                user.sendMessage( "Role not found.", 7000L );
                return;
            }
            user.getGuild().addRoleToMember( user.getMember(), role ).queue();

            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setAuthor( "Role Manager", "https://mirai.red/", "https://mirai.red/resource/roleicon.png" );
            embedBuilder.setTitle( "Role Granted!", "https://mirai.red/" );
            embedBuilder.appendDescription( user.getUser().getAsMention() );
            embedBuilder.appendDescription( " now has the " ).appendDescription( role.getAsMention() ).appendDescription( " role!" );
            embedBuilder.setColor( Color.GREEN );
            embedBuilder.setFooter( user.getUser().getAsTag(), user.getUser().getEffectiveAvatarUrl() );

            user.getChannel().sendMessageEmbeds( embedBuilder.build() ).queue();
        }
    }

    @CommandAlias( "iamn" )
    @Description( "Remove self assignable role." )
    @CommandPermission("armadeus.iamn")
    public void iamnot( DiscordCommandIssuer user, String roleName ) {
        Config config = ArmaModeration.get().getConfig(user.getGuild());
        if ( config == null ) return;

        ArrayList<Long> assignableRoles = config.get( "assignableRoles" );
        Role role = user.getGuild().getRolesByName( roleName, true ).get( 0 );

        if ( role == null ) {
            user.sendMessage( "Role not found.", 20L );
        } else {
            if ( !assignableRoles.contains( role.getIdLong() ) ){
                user.sendMessage( "Role not found.", 20L );
                return;
            }

            if ( !user.getMember().getRoles().contains( role ) ) {
                user.sendMessage( "You don't have that role.", 20L );
                return;
            }

            user.getTextChannel().getLatestMessageId();
            user.getGuild().removeRoleFromMember( user.getMember(), role ).queue();

            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setAuthor( "Role Manager", "https://mirai.red/", "https://mirai.red/resource/roleicon.png" );
            embedBuilder.setTitle( "Role Removed!", "https://mirai.red/" );
            embedBuilder.appendDescription( user.getUser().getAsMention() );
            embedBuilder.appendDescription( " no longer has the " ).appendDescription( role.getAsMention() ).appendDescription( " role!" );
            embedBuilder.setColor( Color.RED );
            embedBuilder.setFooter( user.getUser().getAsTag(), user.getUser().getEffectiveAvatarUrl() );

            user.getChannel().sendMessageEmbeds( embedBuilder.build() ).queue();
        }
    }
}
