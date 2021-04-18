//package dev.armadeus.core.command;
//
//import dev.armadeus.command.CommandResult;
//import dev.armadeus.command.CommandSender;
//import dev.armadeus.core.DiscordBot;
//import dev.armadeus.core.util.ChannelTarget;
//import lombok.Getter;
//import net.dv8tion.jda.api.entities.Message;
//import net.dv8tion.jda.api.entities.MessageChannel;
//import net.dv8tion.jda.api.entities.MessageEmbed;
//import net.dv8tion.jda.api.entities.User;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//
//import java.io.Serializable;
//import java.util.concurrent.Future;
//
//@Getter
//public class DiscordCommandSender implements CommandSender, Serializable {
//
//    private Logger logger = LogManager.getLogger();
//    private final transient DiscordBot bot;
//    private final User user;
//    private final Message message;
//    private MessageChannel messageChannel;
//
//    public DiscordCommandSender(DiscordBot bot, Message message) {
//        this.bot = bot;
//        this.user = message.getAuthor();
//        this.message = message;
//        this.messageChannel = message.getChannel();
//    }
//
//    public DiscordBot getBot() {
//        return bot;
//    }
//
//
//    public void sendPrivateMessage(String message) {
//        getUser().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage(message).queue());
//    }
//
//    public void sendPrivateMessage(MessageEmbed embed) {
//        getUser().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage(embed).queue());
//    }
//
//    public void sendPrivateMessage(Message message) {
//        getUser().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage(message).queue());
//    }
//
//    @Override
//    public void sendMessage(String message) {
//        MessageChannel channel = getMessageChannel();
//        if (channel != null)
//            channel.sendMessage(message).queue();
//    }
//
//    public void sendMessage(MessageEmbed embed) {
//        MessageChannel channel = getMessageChannel();
//        if (channel != null)
//            channel.sendMessage(embed).queue();
//    }
//
//    public void sendMessage(Message message) {
//        MessageChannel channel = getMessageChannel();
//        if (channel != null)
//            channel.sendMessage(message).queue();
//    }
//
//    public boolean hasPermission() {
//        return true;
//    }
//
//    @Override
//    public String getName() {
//        User user = getUser();
//        if(user != null) {
//            return user.getName();
//        } else {
//            return "UNKNOWN_USER";
//        }
//    }
//
//    @Override
//    public Future<CommandResult> runCommand(String command, String[] args) {
//        return bot.getCommandBase().execute(this, command, args);
//    }
//
//    public void sendMessage(ChannelTarget target, String message) {
//        sendMessage(message);
//    }
//
//    public void sendMessage(ChannelTarget target, Message message) {
//        sendMessage(message);
//    }
//
//    public void sendMessage(ChannelTarget target, MessageEmbed embed) {
//        sendMessage(embed);
//    }
//
//}
