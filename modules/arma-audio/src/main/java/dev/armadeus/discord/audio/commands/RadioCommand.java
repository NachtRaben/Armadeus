package dev.armadeus.discord.audio.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Default;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.armadeus.bot.api.command.DiscordCommandIssuer;
import dev.armadeus.bot.api.util.TimeUtil;
import dev.armadeus.discord.audio.AudioManager;
import dev.armadeus.discord.audio.TrackScheduler;
import dev.armadeus.discord.audio.radio.Radio;
import dev.armadeus.discord.audio.util.AudioEmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;

public class RadioCommand extends AudioCommand {

    @Conditions("guildonly")
    @CommandAlias("radio")
    public void radio(DiscordCommandIssuer user, String station) {
        if (cannotQueueMusic(user))
            return;

        Radio radio = Radio.getStation(station);
        if(radio == null) {
            user.sendMessage("There is no station available by that name! :slight_frown:");
            return;
        }

        radio.play(user);
    }
}
