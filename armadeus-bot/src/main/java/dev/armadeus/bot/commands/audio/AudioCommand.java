package dev.armadeus.bot.commands.audio;

import dev.armadeus.core.command.DiscordCommand;
import dev.armadeus.core.command.DiscordUser;
import dev.armadeus.core.managers.GuildMusicManager;

public abstract class AudioCommand extends DiscordCommand {

    protected boolean canInteractMusic(DiscordUser user) {
        if (!user.isFromGuild()) return false;
        String userChannel = user.getVoiceChannel() != null ? user.getVoiceChannel().getId() : null;
        String botChannel = user.getGuildMusicManager().getLink().getChannel();

        if (userChannel == null) {
            user.sendMessage("You are not in a voice channel, you cannot execute this command.");
            return false;
        }
        if (botChannel != null && !userChannel.equals(botChannel)) {
            user.sendMessage("You are not in the same channel as the bot. The queue must be stopped or the bot must be moved.");
            return false;
        }
        return true;
    }

    public boolean verifyPlayingTrack(DiscordUser user) {
        GuildMusicManager manager = user.getGuildMusicManager();
        if (!manager.getScheduler().isPlaying()) {
            user.sendMessage("There is currently nothing playing. Queue a song with the `play` command.");
            return false;
        }
        return true;
    }
}
