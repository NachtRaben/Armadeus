package com.nachtraben.core.command;

import com.nachtraben.commandapi.CommandEvent;
import com.nachtraben.commandapi.CommandSender;
import com.nachtraben.core.JDABot;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.*;
import org.apache.http.util.Asserts;

import java.util.concurrent.Future;

/**
 * Created by NachtRaben on 3/9/2017.
 */
public class GuildCommandSender implements CommandSender {

	private JDA jda;

	private Message message;
	private User user;
	private Member member;
	private Guild guild;
	private TextChannel channel;

	private String userID;
	private String messageID;
	private String guildID;
	private String channelID;

	public GuildCommandSender(Message message) {
		Asserts.notNull(message, "Message cannot be null.");
		Asserts.check(message.isFromType(ChannelType.TEXT), "Message was not a guild message.");

		/* Objects */
		this.message = message;
		this.jda = message.getJDA();
		this.user = message.getAuthor();
		this.member = message.getGuild().getMember(user);
		this.channel = message.getTextChannel();
		this.guild = message.getGuild();

		/* IDs */
		this.userID = user.getId();
		this.messageID = message.getId();
		this.guildID = guild.getId();
		this.channelID = channel.getId();
	}

	/* Getters */
	public JDA getJDA() {
		return jda;
	}

	public Message getMessage() {
		if(message == null) message = getChannel().getMessageById(messageID).complete();
		return message;
	}

	public User getUser() {
		if(user == null) user = getJDA().getUserById(userID);
		return user;
	}

	public Member getMember() {
		if(member == null) member = getGuild().getMember(getUser());
		return member;
	}

	public Guild getGuild() {
		if(guild == null) guild = getJDA().getGuildById(guildID);
		return guild;
	}

	public TextChannel getChannel() {
		if(channel == null) channel = getGuild().getTextChannelById(channelID);
		return channel;
	}

	/* Utilities */

	public void sendPrivateMessage(String message) {
		PrivateChannel pc = getUser().openPrivateChannel().complete();
		pc.sendMessage(message).queue();
	}

	/* Overrides */
	@Override
	public void sendMessage(String message) {
		getChannel().sendMessage(message).queue();
	}

	@Override
	public boolean hasPermission() {
		return false;
	}

	@Override
	public String getName() {
		return getUser().getName();
	}

	@Override
	public Future<CommandEvent> runCommand(String command, String[] args) {
		return JDABot.getInstance().getCommandHandler().execute(this, command, args);
	}
}
