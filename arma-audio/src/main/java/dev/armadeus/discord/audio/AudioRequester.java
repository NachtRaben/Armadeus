package dev.armadeus.discord.audio;

import dev.armadeus.bot.api.command.DiscordCommandIssuer;
import dev.armadeus.bot.api.util.DiscordReference;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;

public class AudioRequester {

    private transient final DiscordReference<User> user;
    private transient final DiscordReference<AudioChannel> audioChannel;
    private transient final DiscordReference<TextChannel> textChannel;

    public AudioRequester(DiscordCommandIssuer issuer) {
        user = new DiscordReference<>(issuer.getUser(), id -> ArmaAudio.core().shardManager().getUserById(id));
        audioChannel = new DiscordReference<>(issuer.getVoiceChannel(), id -> ArmaAudio.core().shardManager().getVoiceChannelById(id));
        textChannel = new DiscordReference<>(issuer.getTextChannel(), id -> ArmaAudio.core().shardManager().getTextChannelById(id));
    }

    public User getUser() {
        return user.resolve();
    }

    public AudioChannel getAudioChannel() {
        return audioChannel.resolve();
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
