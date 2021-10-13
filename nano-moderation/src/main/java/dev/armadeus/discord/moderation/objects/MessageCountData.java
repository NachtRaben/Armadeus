package dev.armadeus.discord.moderation.objects;

import net.dv8tion.jda.api.entities.Message;
import java.time.OffsetDateTime;

public class MessageCountData {
    private int numberOfMessages;
    private OffsetDateTime firstMessageTime;
    private OffsetDateTime lastMessageTime;
    private String lastMessageRaw;

    public MessageCountData( Message message ){
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
