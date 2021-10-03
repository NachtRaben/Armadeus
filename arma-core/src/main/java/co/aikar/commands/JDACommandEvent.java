package co.aikar.commands;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class JDACommandEvent implements CommandIssuer {

    private MessageReceivedEvent event;
    private SlashCommandEvent slash;
    private JDACommandManager manager;

    public JDACommandEvent(JDACommandManager manager, SlashCommandEvent event) {
        this.manager = manager;
        this.slash = event;
    }

    public JDACommandEvent(JDACommandManager manager, MessageReceivedEvent event) {
        this.manager = manager;
        this.event = event;
    }

    public MessageReceivedEvent getEvent() {
        return event;
    }

    public SlashCommandEvent getSlash() {
        return slash;
    }

    public boolean isSlashEvent() {
        return slash != null;
    }

    @Override
    public MessageReceivedEvent getIssuer() {
        return event;
    }

    @Override
    public CommandManager getManager() {
        return this.manager;
    }

    @Override
    public boolean isPlayer() {
        return false;
    }

    @Override
    public @NotNull UUID getUniqueId() {
        // Discord id only have 64 bit width (long) while UUIDs have twice the size.
        // In order to keep it unique we use 0L for the first 64 bit.
        long authorId = event.getAuthor().getIdLong();
        return new UUID(0, authorId);
    }

    @Override
    public boolean hasPermission(String permission) {
        CommandPermissionResolver permissionResolver = this.manager.getPermissionResolver();
        return permissionResolver == null || permissionResolver.hasPermission(manager, this, permission);
    }

    @Override
    public void sendMessageInternal(String message) {
        getChannel().sendMessage(message).queue();
    }

    public void sendMessage(Message message) {
        getChannel().sendMessage(message).queue();
    }

    public void sendMessage(MessageEmbed message) {
        getChannel().sendMessage(message).queue();
    }

    // Additionals

    public Message getMessage() {
        return !isSlashEvent() ? getEvent().getMessage() : null;
    }

    public User getUser() {
        return !isSlashEvent() ? getEvent().getAuthor() : getSlash().getUser();
    }

    // Ambiguous
    public MessageChannel getChannel() {
        return !isSlashEvent() ? getEvent().getChannel() : getSlash().getChannel();
    }

    // Guild Specific
    public Guild getGuild() {
        return !isSlashEvent() ? getEvent().getGuild() : getSlash().getGuild();
    }

    public Member getMember() {
        return !isSlashEvent() ? getEvent().getMember() : getSlash().getMember();
    }

    public boolean isFromGuild() {
        return !isSlashEvent() ? getEvent().isFromGuild() : getSlash().isFromGuild();
    }

    public boolean isSlash() { return isSlashEvent(); }

    public TextChannel getTextChannel() {
        MessageChannel ch = getEvent().getChannel();
        return ch.getType() == ChannelType.TEXT ? (TextChannel) ch : null;
    }

    public VoiceChannel getVoiceChannel() {
        Member m = getMember();
        if (m != null && m.getVoiceState() != null) {
            return m.getVoiceState().getChannel();
        }
        return null;
    }

    public JDA getJda() {
        return !isSlashEvent() ? getEvent().getJDA() : getSlash().getJDA();
    }
}
