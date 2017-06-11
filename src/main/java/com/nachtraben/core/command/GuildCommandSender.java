package com.nachtraben.core.command;

import com.nachtraben.core.DiscordBot;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

public class GuildCommandSender extends DiscordCommandSender {

    private Guild guild;
    private Member member;
    private TextChannel textChannel;

    private long guildId;
    private long textChannelId;

    public GuildCommandSender(DiscordBot dbot, Message message) {
        super(dbot, message);
        this.guild = message.getGuild();
        this.member = guild.getMember(message.getAuthor());
        this.textChannel = message.getTextChannel();
        this.guildId = guild.getIdLong();
    }

    public Guild getGuild() {
        return guild;
    }

    public Member getMember() {
        return member;
    }

    public TextChannel getTextChannel() {
        if(textChannel == null) textChannel = getDbot().getShardManager().getTextChannelById(textChannelId);
        return textChannel;
    }

    @Override
    public void sendMessage(String message) {
        getTextChannel().sendMessage(message).queue();
    }
}
