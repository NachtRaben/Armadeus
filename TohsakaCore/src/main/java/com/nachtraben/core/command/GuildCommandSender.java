package com.nachtraben.core.command;

import com.nachtraben.core.DiscordBot;
import com.nachtraben.core.configuration.GuildConfig;
import com.nachtraben.core.util.ChannelTarget;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class GuildCommandSender extends DiscordCommandSender implements Serializable {

    private transient static final Logger LOGGER = LoggerFactory.getLogger(GuildCommandSender.class);

    private long guildId;
    private long textChannelId;

    public GuildCommandSender(DiscordBot dbot, Message message) {
        super(dbot, message);
        if(!message.isFromType(ChannelType.TEXT))
            throw new IllegalArgumentException("Message must be from type " + ChannelType.TEXT + ".");

        this.guildId = message.getGuild().getIdLong();
        this.textChannelId = message.getChannel().getIdLong();
    }

    public Guild getGuild() {
        return getJDA().getGuildById(guildId);
    }

    public Member getMember() {
        Guild guild = getGuild();
        if(guild != null)
            return guild.getMemberById(getUserID());
        return null;
    }

    public TextChannel getTextChannel() {
        return getJDA().getTextChannelById(textChannelId);
    }

    public VoiceChannel getVoiceChannel() {
        Member m = getMember();
        if(m != null)
            return m.getVoiceState().getChannel();
        return null;
    }

    public GuildConfig getGuildConfig() {
        return getDbot().getGuildManager().getConfigurationFor(guildId);
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
        return guildId;
    }

    public long getTextChannelId() {
        return textChannelId;
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
