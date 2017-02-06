package com.nachtraben.command.sender;

import com.nachtraben.command.PermissionLevel;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.*;

/**
 * Created by NachtDesk on 8/30/2016.
 */
public class UserCommandSender implements CommandSender {

    Message commandMessage;

    private JDA shard;

    private User user;
    private Guild guild;
    private TextChannel tchannel;

    private String userID;
    private String guildID;
    private String tchannelID;

    private boolean pm = false;

    private String command;
    private String[] args;

    public UserCommandSender(Message message, String command, String[] args) {
        this.commandMessage = message;
        this.shard = message.getJDA();
        this.command = command;
        this.args = args;

        if (message.isFromType(ChannelType.PRIVATE)) pm = true;

        this.user = message.getAuthor();
        this.userID = user.getId();
        this.guild = message.getGuild();
        this.guildID = guild.getId();
        if (!pm) {
            this.tchannel = message.getTextChannel();
            this.tchannelID = tchannel.getId();
        }
    }

    public JDA getShard() {
        return shard;
    }

    public User getUser() {
        if (user == null) user = shard.getUserById(userID);
        return user;
    }

    public Member getUserAsMember() {
        if (!pm) getGuild().getMember(getUser());
        return null;
    }

    public Guild getGuild() {
        if (!pm && guild == null) guild = shard.getGuildById(guildID);
        return guild;
    }

    public MessageChannel getTextChannel() {
        if (!pm && tchannel == null) tchannel = shard.getTextChannelById(tchannelID);
        return tchannel;
    }

    public VoiceChannel getVoiceChannel() {
        if(!pm) return getUserAsMember().getVoiceState().getChannel();
        return null;
    }

    public String getCommand() {
        return command;
    }

    public String[] getArgsAsArray() {
        return args;
    }

    public String getArgs() {
        return argsToString(args);
    }

    private String argsToString(String[] args) {
        if (args.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        for (String s : args) {
            sb.append(s).append(" ");
        }
        sb.replace(sb.length() - 1, sb.length(), "");
        return sb.toString();
    }

    public PrivateChannel getPMChannel() {
        return getUser().openPrivateChannel().complete();
    }

    public void getNickName() {

    }

    @Override
    public void sendMessage(String s) {
        getPMChannel().sendMessage(s).queue();
    }

    public Message getCommandMessage() {
        return commandMessage;
    }

    @Override
    public boolean hasPermission(PermissionLevel p) {
        return p.has(commandMessage.getAuthor(), commandMessage.getGuild());
    }

    @Override
    public String getName() {
        return getUser().getName();
    }
}
