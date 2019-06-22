package com.nachtraben.core.audio;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

public class MusicRequestor {

    private long userID;
    private User user;
    private Member member;

    private long textChannelID;
    private TextChannel textChannel;

    private long guildID;
    private Guild guild;

    public MusicRequestor(User user, TextChannel textChannel) {
        this.user = user;
        this.userID = user.getIdLong();
        this.textChannel = textChannel;
        this.textChannelID = textChannel.getIdLong();
        this.guild = textChannel.getGuild();
        this.guildID = textChannel.getIdLong();
    }

    public User getUser() {
        if(user == null)
            user = guild.getJDA().getUserById(userID);
        return user;
    }

    public Member getMember() {
        if(member == null)
            member = guild.getMemberById(userID);
        return member;
    }

    public TextChannel getTextChannel() {
        if(user == null)
            textChannel = guild.getJDA().getTextChannelById(textChannelID);
        return textChannel;
    }

    public Guild getGuild() {
        return guild;
    }

}
