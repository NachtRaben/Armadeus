package dev.armadeus.discord.moderation.listeners;

import com.electronwill.nightconfig.core.Config;
import dev.armadeus.bot.api.ArmaCore;
import dev.armadeus.discord.moderation.ArmaModeration;
import dev.armadeus.discord.moderation.objects.MessageCountData;
import dev.armadeus.discord.moderation.util.MessageAction;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static dev.armadeus.bot.api.config.ArmaConfig.logger;

public class ModerationListener extends ListenerAdapter {
    private final ArmaCore core;

    public ModerationListener( ArmaCore core) {
        this.core = core;
    }
    private static final MessageAction msgAction = new MessageAction();
    private static final Map<Member, MessageCountData> memberMessages = new ConcurrentHashMap<>();

    @Override
    public void onButtonClick( ButtonClickEvent event ) {
        Guild guild = event.getGuild();
        if ( guild == null ) return;

        Config config = ArmaModeration.get().getConfig(guild);
        if ( config == null ) return;

        Role verifiedRole = guild.getRoleById( config.get("verifiedRoleID") );
        if ( verifiedRole == null ) return;

        Member clicker = event.getMember();
        if ( clicker == null ) return;

        switch ( event.getComponentId() ){
            case "ACCEPT_RULES":
                guild.addRoleToMember( clicker, verifiedRole ).queue( e -> {
                    event.reply( "Welcome to `Mirai.Red`!" ).setEphemeral( true ).queue();
                });
                break;
            case "DECLINE_RULES":
                clicker.kick("Declined Rules.").queue();
                break;
        }
    }

    @Override
    public void onGuildLeave( @NotNull GuildLeaveEvent event ) {
        msgAction.removeMutedMembers( event.getGuild() );
    }

    @Override
    public void onGuildMessageUpdate( @NotNull GuildMessageUpdateEvent event ) {
        Guild guild = event.getGuild();
        Message msg = event.getMessage();

        msgAction.setGuild( guild );
        msgAction.profanityCheckMessage( msg, true );
    }

    @Override
    public void onGuildMessageReceived( @NotNull GuildMessageReceivedEvent event ) {
        Member member = event.getMember();
        Message msg = event.getMessage();
        Guild guild = event.getGuild();

        Config config = ArmaModeration.get().getConfig( guild );

        if ( config == null ) return;
        if ( member == null ) return;

        msgAction.setGuild( guild );

        if ( member.getUser().isBot() || member.getUser().isSystem() ) return;
        if ( msg.getContentDisplay().isEmpty() ) return;

        if ( msg.getAttachments().stream().noneMatch( Message.Attachment::isImage ) ) return;
        msgAction.profanityCheckMessage( msg, false );

        MessageCountData msgData = memberMessages.getOrDefault( member, new MessageCountData(msg) );
        boolean isSameAsLast = msg.getContentRaw().equals( msgData.getLastMessageRaw() );
        int compare = msgData.getLastMessageTime().plusSeconds( 3L ).compareTo( msg.getTimeCreated() );
        int msgCount = msgData.increment( msg, 100 );

        if ( memberMessages.size() >= 100 ) memberMessages.clear();
        memberMessages.forEach( (member1, msgData1) -> {
            if ( msgData1.getLastMessageTime().isBefore( msg.getTimeCreated().plusMinutes( 3L ) ) ) {
                try {
                    memberMessages.remove( member1 ); }
                catch ( NullPointerException e ){
                    logger.info( "Failed to remove Member: " + member1.getUser() );
                }
            }
        } );

        memberMessages.put( member, msgData );

        if ( msgCount <= 1 ) return;

        long muteMsgCount = config.getLongOrElse("muteAfterXMessages", 3L );
        long slowmodeA = config.getLongOrElse( "slowmodeA", 7L );
        long slowmodeB = config.getLongOrElse( "slowmodeB", 60L );

        if ( !member.hasPermission( Permission.MESSAGE_MENTION_EVERYONE ) ) {
            if ( msg.mentionsEveryone() || msg.getMentions( Message.MentionType.USER ).size() >= 7 ) {
                msgAction.muteMemberInGuild( msg, slowmodeB );
                return;
            }
        }

        if ( ( msgCount > muteMsgCount || isSameAsLast ) && compare > 0 ) {
            msgAction.muteMemberInGuild( msg, slowmodeA );
            memberMessages.remove( member );
        }

        if ( msgCount >= 100 ) memberMessages.remove( member );
    }
}