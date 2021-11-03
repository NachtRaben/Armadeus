package dev.armadeus.discord.moderation.objects;

import net.dv8tion.jda.api.entities.Message;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.regex.Pattern;

import static dev.armadeus.discord.moderation.util.MessageAction.getMentionedHumans;

public class MessageCountData {
    private int groupPings;
    private int numberOfMessages;
    private OffsetDateTime firstMessageTime;
    private OffsetDateTime lastMessageTime;
    private OffsetDateTime lastGroupMessageTime;
    private String lastMessageRaw;
    private final Pattern RegexAtEveryone = Pattern.compile("@everyone");

    public MessageCountData( Message message ){
        groupPings = 0;
        numberOfMessages = 0;
        firstMessageTime = message.getTimeCreated();
        lastMessageTime = firstMessageTime;
        lastMessageRaw = message.getContentRaw();
    }

    public int increment( Message msg, int limit ) {
        int messageCount = numberOfMessages++;
        lastMessageTime = msg.getTimeCreated();
        lastMessageRaw = msg.getContentRaw();

        if ( messageCount >= limit ) {
            numberOfMessages = 1;
            firstMessageTime = msg.getTimeCreated();
            return 0;
        }

        return messageCount;
    }

    public int getGroupPings() {
        return groupPings;
    }

    public void setGroupPings( int value ) {
        groupPings = value;
    }

    public boolean getGroupPing( Message msg ) {
        List<?> mentions = getMentionedHumans(msg);
        if ( lastGroupMessageTime != null ) {
            if ( lastGroupMessageTime.plusMinutes( 1L ).compareTo( msg.getTimeCreated() ) < 0 ) groupPings = 0;
        }
        if ( msg.mentionsEveryone() || !msg.getMentionedRoles().isEmpty() ) {
            groupPings++;
            lastGroupMessageTime = msg.getTimeCreated();
            return true;
        }
        if ( RegexAtEveryone.matcher( msg.getContentRaw() ).find() ) {
            groupPings++;
            lastGroupMessageTime = msg.getTimeCreated();
            return true;
        }
        if ( mentions.isEmpty() ) {
            return false;
        }
        if ( mentions.size() > 6 ) {
            groupPings++;
            return true;
        }
        return false;
    }

    public OffsetDateTime getFirstMessageTime() {
        return firstMessageTime;
    }

    public OffsetDateTime getLastMessageTime() {
        return lastMessageTime;
    }

    public String getLastMessageRaw() {
        return lastMessageRaw;
    }
}
