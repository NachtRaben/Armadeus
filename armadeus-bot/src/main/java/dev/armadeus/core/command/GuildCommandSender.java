//package dev.armadeus.core.command;
//
//import dev.armadeus.core.DiscordBot;
//import dev.armadeus.core.configuration.GuildConfig;
//import dev.armadeus.core.util.ChannelTarget;
//import lombok.Getter;
//import net.dv8tion.jda.api.MessageBuilder;
//import net.dv8tion.jda.api.entities.*;
//import net.dv8tion.jda.api.utils.WidgetUtil;
//import net.dv8tion.jda.internal.utils.cache.SnowflakeReference;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//
//import java.io.Serializable;
//
//@Getter
//public class GuildCommandSender extends DiscordCommandSender implements Serializable {
//
//    private transient static final Logger logger = LogManager.getLogger();
//
//    private final Guild guild;
//    private final TextChannel textChannel;
//
//    public GuildCommandSender(DiscordBot dbot, Message message) {
//        super(dbot, message);
//        if (!message.isFromType(ChannelType.TEXT))
//            throw new IllegalArgumentException("Message must be from type " + ChannelType.TEXT + ".");
//
//        guild = message.getGuild();
//        textChannel = message.getTextChannel();
//    }
//
//
//    public Member getMember() {
//        Guild guild = getGuild();
//        if(guild != null)
//            return guild.getMember(getUser());
//        return null;
//    }
//
//
//    public VoiceChannel getVoiceChannel() {
//        Member m = getMember();
//        GuildVoiceState vs = m.getVoiceState();
//        if(vs != null && vs.inVoiceChannel())
//            return vs.getChannel();
//        return null;
//    }
//
//    public GuildConfig getGuildConfig() {
//        return getBot().getGuildManager().getConfigurationFor(getGuild());
//    }
//
//    @Override
//    public void sendMessage(String message) {
//        getTextChannel().sendMessage(message).queue();
//    }
//
//    @Override
//    public void sendMessage(ChannelTarget target, String message) {
//        sendMessage(target, new MessageBuilder().append(message).build());
//    }
//
//    @Override
//    public void sendMessage(ChannelTarget target, Message message) {
//        TextChannel channel = getTargetChannel(target);
//        if(channel == null)
//            channel = getTextChannel();
//
//        if (channel != null && channel.canTalk()) {
//            channel.sendMessage(message).queue();
//        }
//    }
//
//    @Override
//    public void sendMessage(ChannelTarget target, MessageEmbed embed) {
//        TextChannel channel = getTargetChannel(target);
//        if (channel == null)
//            channel = getTextChannel();
//
//        if (channel != null && channel.canTalk()) {
//            TextChannel finalChannel = channel;
//            channel.sendMessage(embed).queue(null, failure -> {
//                finalChannel.sendMessageFormat("{} {}", embed.getTitle(), embed.getDescription()).queue();
//            });
//        }
//    }
//
//    private TextChannel getTargetChannel(ChannelTarget target) {
//        TextChannel result = null;
//        GuildConfig config = getGuildConfig();
//         switch (target) {
//             case GENERIC:
//             case NSFW:
//                 result = config.getLogChannel(target);
//                 break;
//             case MUSIC:
//             case BOT_ANNOUNCEMENT:
//                 result = config.getLogChannel(target);
//                 if(result == null)
//                     result = getTargetChannel(ChannelTarget.GENERIC);
//                 break;
//             case TWITCH_ANNOUNCEMENT:
//                 result = config.getLogChannel(target);
//                 if(result == null)
//                     result = getTargetChannel(ChannelTarget.BOT_ANNOUNCEMENT);
//                 break;
//         }
//         return result;
//    }
//
//}
