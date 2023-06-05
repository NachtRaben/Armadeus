package dev.armadeus.discord.audio.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.armadeus.bot.api.command.DiscordCommandIssuer;
import dev.armadeus.discord.audio.AudioManager;

import java.util.List;

@CommandAlias("remove")
@CommandPermission("armadeus.remove")
@Description("Removes a song by index from the currently playing queue")
public class RemoveCommand extends AudioCommand {

    public void queueRemove(DiscordCommandIssuer issuer, int index) {
        AudioManager manager = getAudioManager(issuer);
        if (isNotPlaying(issuer))
            return;
        List<AudioTrack> queue = manager.getScheduler().getQueue();
        AudioTrack removed = queue.remove(index - 1);
        manager.getScheduler().clearQueue();
        queue.forEach(track -> manager.getScheduler().queue(track));
        if (removed != null)
            issuer.sendMessage("Removed `%s` by `%s` from the queue", removed.getInfo().title, removed.getInfo().author);
    }
}
