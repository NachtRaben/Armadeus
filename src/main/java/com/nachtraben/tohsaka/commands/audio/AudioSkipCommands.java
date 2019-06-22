package com.nachtraben.tohsaka.commands.audio;

import com.nachtraben.core.command.GuildCommandSender;
import com.nachtraben.core.managers.GuildMusicManager;
import com.nachtraben.orangeslice.CommandSender;
import com.nachtraben.orangeslice.command.Cmd;
import com.nachtraben.tohsaka.Tohsaka;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class AudioSkipCommands {
    private static final Logger LOGGER = LoggerFactory.getLogger(AudioSkipCommands.class);

    @Cmd(name = "skip", format = "", description = "Skips to the next track.")
    public void skip(CommandSender sender) {
        if (sender instanceof GuildCommandSender) {
            GuildCommandSender sendee = (GuildCommandSender) sender;
            GuildMusicManager manager = Tohsaka.getInstance().getGuildManager().getConfigurationFor(sendee.getGuild().getIdLong()).getMusicManager();
            manager.getScheduler().skip();
        } else {
            sender.sendMessage("Sorry but that command is only available in guilds I'm a part of.");
        }
    }

    @Cmd(name = "skip", format = "{track}", description = "Skips to the desired track in the queue.", flags = {"skipto"})
    public void skipTo(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
        if (sender instanceof GuildCommandSender) {
            GuildCommandSender sendee = (GuildCommandSender) sender;
            GuildMusicManager manager = Tohsaka.getInstance().getGuildManager().getConfigurationFor(sendee.getGuild().getIdLong()).getMusicManager();
            List<AudioTrack> tracks = manager.getScheduler().getQueue();
            if (tracks.isEmpty()) {
                sender.sendMessage("The queue is empty, add more tracks with `/play <search>`");
                return;
            }
            try {
                int i = Integer.parseInt(args.get("track"));
                if (i > 0 && i <= tracks.size()) {
                    manager.getScheduler().skipTo(tracks.get(i - 1));
                } else {
                    sender.sendMessage("Sorry, I can't skip that many tracks.");
                }
            } catch (NumberFormatException e) {
                String search = args.get("track");
                AudioTrack track = prefixSearch(tracks, search);
                //if (track == null)
                //    track = distanceSearch(tracks, search);
                if(track != null)
                    manager.getScheduler().skipTo(track);
                else
                    sender.sendMessage("Sorry, I can't find `" + search + "` in the queue.");

            }
        } else {
            sender.sendMessage("Sorry but that command is only available in guilds I'm a part of.");
        }
    }

    private AudioTrack prefixSearch(List<AudioTrack> tracks, String search) {
        for (AudioTrack t : tracks) {
            if (t.getInfo().title.toLowerCase().startsWith(search.toLowerCase()) || t.getInfo().title.toLowerCase().contains(search.toLowerCase()))
                return t;
        }
        return null;
    }

    private AudioTrack distanceSearch(List<AudioTrack> tracks, String search) {
        int distance = Integer.MAX_VALUE;
        AudioTrack track = null;
        for (AudioTrack t : tracks) {
            int sizeDiff = Math.abs(t.getInfo().title.length() - search.length());
            int dis = distance(t.getInfo().title, search);
            int finalDis = Math.abs(dis - sizeDiff);
            if (finalDis > 0 && finalDis < distance) {
                distance = finalDis;
                track = t;
                LOGGER.debug(String.format("Selecting %s with a distance of %s and a final diff of %s and a size diff of %s.", track.getInfo().title, dis, finalDis, sizeDiff));
            }
        }
        return track;
    }

    private int distance(CharSequence lhs, CharSequence rhs) {
        int len0 = lhs.length() + 1;
        int len1 = rhs.length() + 1;

        // the array of distances
        int[] cost = new int[len0];
        int[] newcost = new int[len0];

        // initial cost of skipping prefix in String s0
        for (int i = 0; i < len0; i++) cost[i] = i;

        // dynamically computing the array of distances

        // transformation cost for each letter in s1
        for (int j = 1; j < len1; j++) {
            // initial cost of skipping prefix in String s1
            newcost[0] = j;

            // transformation cost for each letter in s0
            for (int i = 1; i < len0; i++) {
                // matching current letters in both strings
                int match = (lhs.charAt(i - 1) == rhs.charAt(j - 1)) ? 0 : 1;

                // computing cost for each transformation
                int cost_replace = cost[i - 1] + match;
                int cost_insert = cost[i] + 1;
                int cost_delete = newcost[i - 1] + 1;

                // keep minimum cost
                newcost[i] = Math.min(Math.min(cost_insert, cost_delete), cost_replace);
            }

            // swap cost/newcost arrays
            int[] swap = cost;
            cost = newcost;
            newcost = swap;
        }

        // the distance is the cost for transforming all letters in both strings
        return cost[len0 - 1];
    }

}
