package com.nachtraben.core.command;

import com.nachtraben.core.DiscordBot;
import com.nachtraben.core.configuration.GuildConfig;
import com.nachtraben.core.util.ChannelTarget;
import net.dv8tion.jda.core.entities.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("Duplicates")
public class GuildCommandSender extends DiscordCommandSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(GuildCommandSender.class);

    private Guild guild;
    private Member member;
    private TextChannel textChannel;

    private long guildId;
    private long textChannelId;

    public GuildCommandSender(DiscordBot dbot, Message message) {
        super(dbot, message);
        if(!message.isFromType(ChannelType.TEXT))
            throw new IllegalArgumentException("Message must be from type " + ChannelType.TEXT + ".");
        this.guild = message.getGuild();
        this.member = guild.getMember(message.getAuthor());
        this.textChannel = message.getTextChannel();
        this.guildId = guild.getIdLong();
        this.textChannelId = textChannel.getIdLong();
    }

    public Guild getGuild() {
        return guild;
    }

    public Member getMember() {
        return member;
    }

    public TextChannel getTextChannel() {
        if(textChannel == null) textChannel = getDbot().getShardManager().getTextChannelByID(textChannelId);
        return textChannel;
    }

    public VoiceChannel getVoiceChannel() {
        Member m = getMember();
        if(m != null) {
            return m.getVoiceState().getChannel();
        }
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
        TextChannel channel = getTargetChannel(target);
        if(channel == null)
            channel = getTextChannel();

        if(channel != null && channel.canTalk()) {
            channel.sendMessage(message).queue();
        } else if(channel == null) {
            LOGGER.warn("Attempted to send a message to " + getUser() + " but couldn't find a valid destination.");
        } else if(!channel.canTalk()) {
            LOGGER.warn("Attempted to send a message to " + getUser() + " but I do not have permission to talk in " + channel + ".");
        }
    }

    @Override
    public void sendMessage(ChannelTarget target, Message message) {
        TextChannel channel = getTargetChannel(target);
        if(channel == null)
            channel = getTextChannel();

        if(channel != null && channel.canTalk()) {
            channel.sendMessage(message).queue();
        } else if(channel == null) {
            LOGGER.warn("Attempted to send a message to " + getUser() + " but couldn't find a valid destination.");
        } else if(!channel.canTalk()) {
            LOGGER.warn("Attempted to send a message to " + getUser() + " but I do not have permission to talk in " + channel + ".");
        }
    }

    @Override
    public void sendMessage(ChannelTarget target, MessageEmbed embed) {
        TextChannel channel = getTargetChannel(target);
        if(channel == null)
            channel = getTextChannel();

        if(channel != null && channel.canTalk()) {
            channel.sendMessage(embed).queue();
        } else if(channel == null) {
            LOGGER.warn("Attempted to send a message to " + getUser() + " but couldn't find a valid destination.");
        } else if(!channel.canTalk()) {
            LOGGER.warn("Attempted to send a message to " + getUser() + " but I do not have permission to talk in " + channel + ".");
        }
    }

    private TextChannel getTargetChannel(ChannelTarget target) {
        System.out.println("Target: " + target.toString());
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
