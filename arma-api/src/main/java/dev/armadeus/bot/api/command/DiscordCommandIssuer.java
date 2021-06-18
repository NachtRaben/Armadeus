package dev.armadeus.bot.api.command;

import co.aikar.commands.CommandIssuer;
import dev.armadeus.bot.api.config.GuildConfig;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;

public interface DiscordCommandIssuer extends CommandIssuer {

    int defaultPurgeDelay = 120;
    Message getMessage();
    User getUser();
    MessageChannel getChannel();
    Guild getGuild();
    Member getMember();
    boolean isFromGuild();
    GuildConfig getGuildConfig();
    TextChannel getTextChannel();
    VoiceChannel getVoiceChannel();
    JDA getJda();

    // Channel Agnostic Senders
    void sendMessage(String message);
    void sendMessage(String pattern, Object... args);
    void sendMessage(String message, long purgeAfter);
    void sendMessage(MessageEmbed embed);
    void sendMessage(MessageEmbed embed, long purgeAfter);
    void sendMessage(Message message);
    void sendMessage(Message message, long purgeAfter);

    // Private Only Senders
    void sendPrivateMessage(String message);
    void sendPrivateMessage(String pattern, Object... args);
    void sendPrivateMessage(MessageEmbed embed);
    void sendPrivateMessage(Message message);
}
