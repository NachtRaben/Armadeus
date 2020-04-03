package com.nachtraben.tohsaka.commands;

import com.nachtraben.core.command.DiscordCommandSender;
import com.nachtraben.core.util.ChannelTarget;
import com.nachtraben.core.util.Utils;
import com.nachtraben.orangeslice.CommandSender;
import com.nachtraben.orangeslice.command.Command;
import com.nachtraben.tohsaka.Tohsaka;
import com.sedmelluq.discord.lavaplayer.tools.PlayerLibrary;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDAInfo;

import java.text.DecimalFormat;
import java.util.Map;

public class StatsCommand extends Command {

    private static final long MB = 1000000;
    private static final long GB = MB * 1000;

    private static final String stats = String.join("\n",
            "__**Statistics**__",
            "Server[OS/Arch/Version]: %s / %s / %s",
            "Ram[Used/Max]: %s / %s",
            "Shards[Current/Total]: %s / %s",
            "Threads: %s",
            "",
            "__**Discord Stats**__",
            "",
            "**Channels:**",
            "Text Channels: %s",
            "Voice Channels: %s",
            "Private Channels: %s",
            "Connected VCs: %s",
            "",
            "**Other**",
            "Guilds: %s",
            "Users: %s",
            "",
            "__**Libraries**__",
            "JDA: " + JDAInfo.VERSION,
            "Lavaplayer: " + PlayerLibrary.VERSION,
            "Java: " + System.getProperty("java.version"),
            "",
            "__**Developers**__",
            "NachtRaben#8307: Its my child, deal with it.",
            "Coolguy3289#2290: For the occasional change, maybe.");



    public StatsCommand() {
        super("stats", "", "Gets statistical information about the bot.");
    }

    @Override
    public void run(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
        Runtime rt = Runtime.getRuntime();
        JDA.ShardInfo info = sender instanceof DiscordCommandSender ? ((DiscordCommandSender) sender).getUser().getJDA().getShardInfo() : null;
        String shard = info != null ? String.valueOf(info.getShardId()) : "1";
        String total = info != null ? String.valueOf(info.getShardTotal()) : "1";
        String stats = String.format(StatsCommand.stats,
                System.getProperty("os.name"), System.getProperty("os.arch"), System.getProperty("os.version"),
                getMemoryString(rt.totalMemory() - rt.freeMemory()), getMemoryString(rt.totalMemory()),
                shard, total,
                Thread.getAllStackTraces().keySet().size(),
                Tohsaka.getInstance().getShardManager().getTextChannels(),
                Tohsaka.getInstance().getShardManager().getVoiceChannels(),
                Tohsaka.getInstance().getShardManager().getPrivateChannels(),
                Tohsaka.getInstance().getLavalink().getLinks().stream().filter(link -> link.getChannel() != null).count(),
                Tohsaka.getInstance().getShardManager().getGuilds().size(),
                Tohsaka.getInstance().getShardManager().getUsers().size());
        if (sender instanceof DiscordCommandSender) {
            DiscordCommandSender sendee = (DiscordCommandSender) sender;
            EmbedBuilder eb = new EmbedBuilder();
            eb.setColor(Utils.randomColor());
            eb.setDescription(stats);
            sendee.sendMessage(ChannelTarget.GENERIC, eb.build());
        } else {
            sender.sendMessage(stats.replaceAll("[_|*]", ""));
        }
    }

    public String getMemoryString(long l) {
        DecimalFormat df = new DecimalFormat("#.00");
        if(l > GB)
            return df.format(l/GB) + "GB";
        else
            return df.format(l/MB) + "MB";
    }

}
