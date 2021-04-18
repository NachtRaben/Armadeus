package dev.armadeus.core.audio;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

public class MusicRequestor {

    private final long userID;
    private User user;
    private Member member;

    private final long textChannelID;
    private TextChannel textChannel;

    private final long guildID;
    private final Guild guild;

    public MusicRequestor(User user, TextChannel textChannel) {
        this.user = user;
        this.userID = user.getIdLong();
        this.textChannel = textChannel;
        this.textChannelID = textChannel.getIdLong();
        this.guild = textChannel.getGuild();
        this.guildID = textChannel.getIdLong();
    }

    public User getUser() {
        if (user == null)
            user = guild.getJDA().getUserById(userID);
        return user;
    }

    public Member getMember() {
        if (member == null)
            member = guild.getMemberById(userID);
        return member;
    }

    public TextChannel getTextChannel() {
        if (user == null)
            textChannel = guild.getJDA().getTextChannelById(textChannelID);
        return textChannel;
    }

    public Guild getGuild() {
        return guild;
    }

}