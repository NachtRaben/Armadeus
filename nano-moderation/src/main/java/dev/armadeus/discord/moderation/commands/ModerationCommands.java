package dev.armadeus.discord.moderation.commands;

import co.aikar.commands.annotation.*;
import com.electronwill.nightconfig.core.Config;
import dev.armadeus.bot.api.command.DiscordCommand;
import dev.armadeus.bot.api.command.DiscordCommandIssuer;
import dev.armadeus.discord.moderation.ArmaModeration;
import dev.armadeus.discord.moderation.util.CommandAction;
import dev.armadeus.discord.moderation.util.SqlManager;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;

import static dev.armadeus.discord.moderation.ArmaModeration.sqlManager;
import static dev.armadeus.discord.moderation.util.CommandAction.issue;

@Conditions( "guildonly" )
@DiscordPermission( Permission.KICK_MEMBERS )
public class ModerationCommands extends DiscordCommand {
    @Private
    @CommandAlias("kick")
    @DiscordPermission( Permission.KICK_MEMBERS )
    @Description("Kick a user from the server, optionally with a message.")
    public void kick( DiscordCommandIssuer user, String search, @Default("false") boolean notify, @Default String reason ) {
        issue(user, search, false, notify, reason, "Kicked from %s",
                ( target, finalReason ) -> target.kick( finalReason ).queue() );
    }

    @Private
    @CommandAlias("ban")
    @DiscordPermission( Permission.BAN_MEMBERS )
    @Description("Ban a user from the server, optionally with a message.")
    public void ban( DiscordCommandIssuer user, String search, @Default("0") boolean notify, @Default("1") int days, @Default String reason ) {
        CommandAction.issue(user, search, false, notify, reason, "Banned from %s",
                ( target, finalReason ) -> target.ban( days, finalReason ).queue() );
    }

    @Private
    @CommandAlias("tp")
    @DiscordPermission( Permission.VIEW_AUDIT_LOGS )
    @Description("Toggle \"hoisting\" for yourself.")
    @Conditions( "guildonly" )
    public void tp( DiscordCommandIssuer user ) {
        Guild guild = user.getGuild();
        if ( guild == null ) return;

        Config config = ArmaModeration.get().getConfig(guild);
        if ( config == null ) return;

        long staffRoleID = config.get("staffRoleID");
        Role staffRole = guild.getRoleById( staffRoleID );
        if ( staffRole == null ) return;

        if ( user.getMember().getRoles().contains( staffRole ) ) {
            guild.removeRoleFromMember( user.getMember(), staffRole ).queue();
        } else {
            guild.addRoleToMember( user.getMember(), staffRole ).queue();
        }
    }

    @Private
    @CommandAlias( "register" )
    @DiscordPermission( Permission.ADMINISTRATOR )
    @Description( "Register users in the database." )
    public void register( DiscordCommandIssuer user, String key ) {
        SqlManager sql = sqlManager.getConnection( user.getGuild() );
        CommandAction.issue( user, key, true, ( target ) -> sql.insertUserEntry( target.getUser() ) );
    }

    @Private
    @CommandAlias( "moderation.set" )
    @DiscordPermission( Permission.ADMINISTRATOR )
    @Description( "Change the servers moderation configuration." )
    public void editConfig( DiscordCommandIssuer user, String setting, String value ) {
        Config config = ArmaModeration.get().getConfig( user.getGuild() );
        if ( config == null ) return;

        if ( !config.contains( setting ) ) {
            user.sendMessage( String.format( "Couldn't set \"%s\" because it wasn't found.", setting ) );
            return;
        }

        if ( value.matches( "-?\\d+" ) ) {
            config.set( setting, Long.parseLong( value ) );
        } else {
            config.set( setting, value );
        }

        String oldValue = config.get( setting ).toString();
        user.sendMessage( String.format( "`%s[ %s ]` **>>** `%1$s[ %s ]`", setting, oldValue, value ) );
    }

    @Private
    @CommandAlias( "init-agreement" )
    @DiscordPermission( Permission.ADMINISTRATOR )
    @Description( "Initialized the server agreement." )
    public void agreement( DiscordCommandIssuer user ) {
        MessageBuilder msgBuilder = new MessageBuilder();
        msgBuilder
                .append( "By clicking AGREE I agree to the rules listed above, and/or any future changes made to the " )
                .append( "rules above. I agree that I will be notified when the rules change, and that I will immedia" )
                .append( "tely discontinue use of the server if at anypoint I do not agree with the rules above.\n**I" )
                .append( " also agree that my messages may be logged by the servers staff and/or bots for quality ass" )
                .append( "urance, and moderation purposes.** \nWe agree that you as a user have the right to be forgo" )
                .append( "tten and will anonymize and delete any logs pertaining to you __within 5 to 6 business days" )
                .append( "__, upon request." );
        msgBuilder.setActionRows(
                ActionRow.of(
                        Button.primary("ACCEPT_RULES", "I AGREE")
                                .withEmoji( Emoji.fromUnicode( "✅" ) ),
                        Button.primary( "DECLINE_RULES", "I REFUSE" )
                                .withEmoji( Emoji.fromUnicode( "\uD83D\uDEAB" ) )
                )
        );

        user.getChannel().sendMessage(msgBuilder.build()).queue();
    }
}
