package dev.armadeus.discord.audio.commands;

import dev.armadeus.bot.api.command.DiscordCommand;
import dev.armadeus.bot.api.command.DiscordCommandIssuer;
import dev.armadeus.discord.audio.ArmaAudio;
import dev.armadeus.discord.audio.AudioManager;
import net.dv8tion.jda.api.entities.Member;

public abstract class AudioCommand extends DiscordCommand {

    protected boolean cannotQueueMusic(DiscordCommandIssuer user) {
        if (!user.isFromGuild()) return true;
        long userChannel = user.getVoiceChannel() != null ? user.getVoiceChannel().getIdLong() : -1;
        String botChannel = getAudioManager(user).getPlayer().getLink().getChannel();

        if (userChannel == -1) {
            user.sendMessage("You are not in a voice channel, you cannot execute this command.");
            return true;
        }
        if (botChannel != null && userChannel != Long.parseLong(botChannel)) {
            user.sendMessage("You are not in the same channel as the bot. The queue must be stopped or the bot must be moved.");
            return true;
        }
        return false;
    }

    public boolean isNotPlaying(DiscordCommandIssuer user) {
        AudioManager manager = getAudioManager(user);
        if (!manager.getPlayer().isPlaying()) {
            user.sendMessage("There is currently nothing playing. Queue a song with the `play` command.");
            return true;
        }
        return false;
    }

    public AudioManager getAudioManager(DiscordCommandIssuer user) {
        return ArmaAudio.getManagerFor(user.getGuild());
    }

    public AudioManager getAudioManager(Member member) {
        return ArmaAudio.getManagerFor(member.getGuild());
    }
}
