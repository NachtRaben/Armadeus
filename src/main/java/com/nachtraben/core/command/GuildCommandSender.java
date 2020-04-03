package com.nachtraben.core.command;

import com.nachtraben.core.DiscordBot;
import com.nachtraben.core.configuration.GuildConfig;
import com.nachtraben.core.util.ChannelTarget;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.internal.utils.cache.SnowflakeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class GuildCommandSender extends DiscordCommandSender implements Serializable {

    private transient static final Logger LOGGER = LoggerFactory.getLogger(GuildCommandSender.class);

    private final SnowflakeReference<Guild> guild;
    private final SnowflakeReference<TextChannel> textChannel;

    public GuildCommandSender(DiscordBot dbot, Message message) {
        super(dbot, message);
        if (!message.isFromType(ChannelType.TEXT))
            throw new IllegalArgumentException("Message must be from type " + ChannelType.TEXT + ".");

        guild = new SnowflakeReference<>(message.getGuild(), id -> dbot.getShardManager().getGuildById(id));
        textChannel = new SnowflakeReference<>(message.getTextChannel(), id -> dbot.getShardManager().getTextChannelById(id));
    }

    public Guild getGuild() {
        return guild.resolve();
    }

    public Member getMember() {
        Guild guild = getGuild();
        if(guild != null)
            return guild.getMemberById(getUserId());
        return null;
    }

    public TextChannel getTextChannel() {
        return textChannel.resolve();
    }

    public VoiceChannel getVoiceChannel() {
        Member m = getMember();
        if(m != null)
            return m.getVoiceState().getChannel();
        return null;
    }

    public GuildConfig getGuildConfig() {
        return getDbot().getGuildManager().getConfigurationFor(guild.resolve());
    }

    @Override
    public void sendMessage(String message) {
        getTextChannel().sendMessage(message).queue();
    }

    @Override
    public void sendMessage(ChannelTarget target, String message) {
        sendMessage(target, new MessageBuilder().append(message).build());
    }

    @Override
    public void sendMessage(ChannelTarget target, Message message) {
        TextChannel channel = getTargetChannel(target);
        if(channel == null)
            channel = getTextChannel();

        if(channel != null && channel.canTalk())
            channel.sendMessage(message).queue();
    }

    @Override
    public void sendMessage(ChannelTarget target, MessageEmbed embed) {
        TextChannel channel = getTargetChannel(target);
        if(channel == null)
            channel = getTextChannel();

        if(channel != null && channel.canTalk())
            channel.sendMessage(embed).queue();
    }

    public long getGuildId() {
        return guild.getIdLong();
    }

    public long getTextChannelId() {
        return textChannel.getIdLong();
    }

    private TextChannel getTargetChannel(ChannelTarget target) {
        TextChannel result = null;
        GuildConfig config = getGuildConfig();
         switch (target) {
             case GENERIC:
                    result = config.getLogChannel(target);
                 break;
             case MUSIC:
                 result = config.getLogChannel(target);
                 if(result == null)
                     result = getTargetChannel(ChannelTarget.GENERIC);
                 break;
             case NSFW:
                 result = config.getLogChannel(target);
                 break;
             case BOT_ANNOUNCEMENT:
                 result = config.getLogChannel(target);
                 if(result == null)
                     result = getTargetChannel(ChannelTarget.GENERIC);
                 break;
             case TWITCH_ANNOUNCEMENT:
                 result = config.getLogChannel(target);
                 if(result== null)
                     result = getTargetChannel(ChannelTarget.BOT_ANNOUNCEMENT);
                 break;
         }
         return result;
    }

}
