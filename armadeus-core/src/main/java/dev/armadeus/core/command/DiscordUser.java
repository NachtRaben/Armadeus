package dev.armadeus.core.command;

import dev.armadeus.core.DiscordBot;
import dev.armadeus.core.configuration.GuildConfig;
import dev.armadeus.core.util.DiscordReference;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkArgument;

@Getter
public class DiscordUser {

    private static final Logger logger = LogManager.getLogger();
    private static final int defaultMessageTimeout = 30;
    @Getter
    private static final Map<DiscordReference<Message>, CompletableFuture<?>> pendingDeletions = new ConcurrentHashMap<>();


    private static final ShardManager shardManager = DiscordBot.get().getShardManager();

    // Instance settings
    private final DiscordReference<Message> message;
    private final DiscordReference<User> user;
    private DiscordReference<MessageChannel> channel;
    private DiscordReference<Guild> guild;
    private DiscordReference<Member> member;

    public DiscordUser(MessageReceivedEvent event) {
        this.message = new DiscordReference<>(event.getMessage(), id -> channel.resolve().getHistory().getMessageById(id));
        this.user = new DiscordReference<>(event.getAuthor(), id -> shardManager.getUserById(id));
        this.channel = new DiscordReference<>(event.getChannel(), id -> {
            switch (event.getChannelType()) {
                case TEXT:
                    return shardManager.getTextChannelById(id);
                case PRIVATE:
                    return shardManager.getPrivateChannelById(id);
                default:
                    throw new IllegalArgumentException();
            }
        });
        if (event.isFromGuild()) {
            this.guild = new DiscordReference<>(event.getGuild(), id -> shardManager.getGuildById(id));
            this.member = new DiscordReference<>(event.getMember(), id -> guild.resolve().getMemberById(id));
            if(getGuildConfig().shouldDeleteCommands()) {
                purge(message.resolve(), 0);
            }
        }
    }

    public boolean isFromGuild() {
        return guild != null;
    }

    // Guild Specific
    public Guild getGuild() {
        return guild != null ? guild.resolve() : null;
    }

    public GuildConfig getGuildConfig() {
        return guild != null ? DiscordBot.get().getGuildManager().getConfigurationFor(guild.resolve()) : null;
    }

    public Member getMember() {
        return member != null ? member.resolve() : null;
    }

    public TextChannel getTextChannel() {
        MessageChannel ch = channel.resolve();
        return ch.getType() == ChannelType.TEXT ? (TextChannel) ch : null;
    }

    public VoiceChannel getVoiceChannel() {
        Member m = member.resolve();
        if(m != null && m.getVoiceState() != null) {
            return m.getVoiceState().getChannel();
        }
        return null;
    }

    // Ambiguous
    public MessageChannel getChannel() {
        return channel.resolve();
    }

    public Message getMessage() {
        return message.resolve();
    }

    public User getUser() {
        return user.resolve();
    }

    public JDA getJda() {
        return message.resolve().getJDA();
    }


    // Universal Senders
    public void sendMessage(String message) {
        sendMessage(message, 0);
    }

    public void sendMessage(String message, int purgeAfter) {
        checkArgument(message != null && !message.isBlank(), "Empty Message");
        MessageBuilder builder = new MessageBuilder(message);
        sendAndPurge(builder.build(), channel.resolve(), purgeAfter);
    }

    public void sendMessage(MessageEmbed embed) {
        sendMessage(embed, 0);
    }

    public void sendMessage(MessageEmbed embed, int purgeAfter) {
        checkArgument(message != null && embed.isSendable(), "Empty Message");
        MessageBuilder builder = new MessageBuilder(embed);
        sendAndPurge(builder.build(), channel.resolve(), purgeAfter);
    }

    // Private Messages
    public void sendPrivateMessage(String message) {
        sendPrivateMessage(message, -1);
    }

    public void sendPrivateMessage(String message, int purgeAfter) {
        checkArgument(message != null && !message.isBlank(), "Empty Message");
        MessageBuilder builder = new MessageBuilder(message);
        sendAndPurge(builder.build(), channel.resolve(), purgeAfter);
    }

    public void sendPrivateMessage(MessageEmbed embed) {
        sendPrivateMessage(embed, -1);
    }

    public void sendPrivateMessage(MessageEmbed embed, int purgeAfter) {
        checkArgument(message != null && embed.isSendable(), "Empty Message");
        MessageBuilder builder = new MessageBuilder(embed);
        sendAndPurge(builder.build(), channel.resolve(), purgeAfter);
    }

    public void sendPrivateMessage(Message message, int purgeAfter) {
        getUser().openPrivateChannel().queue(channel -> {
            sendAndPurge(message, channel, purgeAfter);
        });
    }

    private void sendAndPurge(Message message, MessageChannel channel, int purgeAfter) {
        if (channel.getType() == ChannelType.TEXT && purgeAfter == 0) {
            int guildMessageTimeout = getGuildConfig().getMessageTimeout();
            purgeAfter = guildMessageTimeout == 0 ? defaultMessageTimeout : guildMessageTimeout;
        }
        if (channel.getType() == ChannelType.TEXT && !((TextChannel) channel).canTalk()) {
            sendPrivateMessage(message, purgeAfter);
        }
        int finalPurgeAfter = purgeAfter;
        channel.sendMessage(message).submit()
                .thenAccept(m -> {
                    purge(m, finalPurgeAfter);
                })
                .exceptionally(throwable -> {
                    if (channel.getType() == ChannelType.TEXT) {
                        sendPrivateMessage(message, finalPurgeAfter);
                    } else {
                        logger.warn(String.format("Failed to send private message to %s with text %s", getUser().getName(), message), throwable);
                    }
                    return null;
                });
    }

    private void purge(Message message, int purgeAfter) {
        if (purgeAfter == -1)
            return;

        boolean purge = false;
        if (message.isFromGuild() && message.getGuild().getSelfMember().hasPermission(message.getTextChannel(), Permission.MESSAGE_MANAGE))
            purge = true;
        else if (message.getAuthor().equals(message.getJDA().getSelfUser()))
            purge = true;

        if (purge) {
            synchronized (pendingDeletions) {
                DiscordReference<Message> reference = new DiscordReference<>(message, id -> channel.resolve().getHistory().getMessageById(id));
                CompletableFuture<?> future = message.delete().submitAfter(purgeAfter, TimeUnit.SECONDS);
                if(purgeAfter > 3) {
                    pendingDeletions.put(reference, future);
                    future.thenAccept(a -> pendingDeletions.remove(reference));
                }
            }
        }
    }
}
