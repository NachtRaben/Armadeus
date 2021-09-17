package dev.armadeus.discord.audio.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Description;
import dev.armadeus.bot.api.command.DiscordCommandIssuer;
import dev.armadeus.discord.audio.AudioManager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayCommand extends AudioCommand {

    private static final Pattern LIMIT_MATCH = Pattern.compile("-{1,2}limit[\\s=](\\d+)");
    private static final Pattern PLAYLIST_MATCH = Pattern.compile("-{1,2}p(laylist)?");

    @Conditions("guildonly")
    @CommandAlias("play")
    @Description("Request the specified track, use -p to load a playlist")
    @CommandPermission("armadeus.play")
    public void play(DiscordCommandIssuer user, String identifier) {
        if (cannotQueueMusic(user))
            return;

        int limit = 1;
        if(PLAYLIST_MATCH.matcher(identifier).find()) {
            limit = 10;
            identifier = identifier.replaceAll(PLAYLIST_MATCH.pattern(), "").trim();
        }
        Matcher matcher = LIMIT_MATCH.matcher(identifier);
        if (matcher.find()) {
            limit = Integer.parseInt(matcher.group(1));
            identifier = identifier.replaceAll(LIMIT_MATCH.pattern(), "").trim();
        }
        AudioManager.TrackLoader.loadAndPlay(user, identifier, limit);
    }
}
