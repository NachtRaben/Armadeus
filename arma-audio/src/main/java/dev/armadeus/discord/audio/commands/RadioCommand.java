package dev.armadeus.discord.audio.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import dev.armadeus.bot.api.command.DiscordCommandIssuer;
import dev.armadeus.bot.api.util.EmbedUtils;
import dev.armadeus.discord.audio.radio.Radio;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.Map;

@Conditions("guildonly")
@CommandAlias("radio")
@Description("Loads a live broadcast from predefined stations")
public class RadioCommand extends AudioCommand {


    @Subcommand("play|listen")
    @CommandPermission("armadeus.radio")
    @Description("Loads a live broadcast from predefined stations")
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

    @Subcommand("list")
    @CommandPermission("armadeus.radio")
    @Description("Shows a list of radio stations")
    public void list(DiscordCommandIssuer user) {
        if (cannotQueueMusic(user))
            return;

        EmbedBuilder builder = EmbedUtils.newBuilder(user);
        builder.setTitle("Radio Stations");
        for(Map.Entry<String, Radio> station : Radio.getStations().entrySet()) {
            builder.appendDescription(String.format("`%s` provided by `%s`\n", station.getKey(), station.getValue().getArtist()));
        }
        user.sendMessage(builder.build());
    }
}
