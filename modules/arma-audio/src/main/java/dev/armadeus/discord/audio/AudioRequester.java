package dev.armadeus.discord.audio;

import dev.armadeus.bot.api.ArmaCore;
import dev.armadeus.bot.api.command.DiscordCommandIssuer;
import dev.armadeus.bot.api.util.DiscordReference;
import net.dv8tion.jda.api.entities.*;

public class AudioRequester {

    private transient final DiscordReference<User> user;
    private transient final DiscordReference<VoiceChannel> voiceChannel;
    private transient final DiscordReference<TextChannel> textChannel;

    public AudioRequester(DiscordCommandIssuer issuer) {
        user = new DiscordReference<>(issuer.getUser(), id -> ArmaCore.get().getShardManager().getUserById(id));
        voiceChannel = new DiscordReference<>(issuer.getVoiceChannel(), id -> ArmaCore.get().getShardManager().getVoiceChannelById(id));
        textChannel = new DiscordReference<>(issuer.getTextChannel(), id -> ArmaCore.get().getShardManager().getTextChannelById(id));
    }

    public User getUser() {
        return user.resolve();
    }

    public VoiceChannel getVoiceChannel() {
        return voiceChannel.resolve();
    }

    public TextChannel getTextChannel() {
        return textChannel.resolve();
    }

    public Guild getGuild() {
        return getTextChannel().getGuild();
    }

    public Member getMember() {
        return getGuild().getMember(getUser());
    }

}
