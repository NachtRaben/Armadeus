package dev.armadeus.discord.commands.general;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import dev.armadeus.bot.api.command.DiscordCommand;
import dev.armadeus.bot.api.command.DiscordCommandIssuer;
import dev.armadeus.discord.util.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDAInfo;

import java.text.DecimalFormat;

public class StatsCommand extends DiscordCommand {

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
            "Lavaplayer: " + ""/*PlayerLibrary.VERSION*/,
            "Java: " + System.getProperty("java.version"),
            "",
            "__**Developers**__",
            "NachtRaben#8307: Its my child, deal with it.",
            "Coolguy3289#2290: For the occasional change, maybe.");

    @Default
    @CommandAlias("stats")
    @CommandPermission("armadeus.stats")
    @Description("Show some live stats about the bots performance")
    public void stats(DiscordCommandIssuer user) {
        Runtime rt = Runtime.getRuntime();
        JDA.ShardInfo info = user.getJda().getShardInfo();
        String shard = String.valueOf(info.getShardId());
        String total = String.valueOf(info.getShardTotal());
        String stats = String.format(StatsCommand.stats,
                System.getProperty("os.name"), System.getProperty("os.arch"), System.getProperty("os.version"),
                getMemoryString(rt.totalMemory() - rt.freeMemory()), getMemoryString(rt.totalMemory()),
                shard, total,
                Thread.getAllStackTraces().keySet().size(),
                core.shardManager().getTextChannels().size(),
                core.shardManager().getVoiceChannels().size(),
                core.shardManager().getPrivateChannels().size(),
                0 /*ArmaCore.get().getLavalink().getLinks().stream().filter(link -> link.getChannelId() != -1).count()*/,
                core.shardManager().getGuilds().size(),
                core.shardManager().getUsers().size());
        EmbedBuilder eb = EmbedUtils.newBuilder(user);
        eb.setDescription(stats);
        user.sendMessage(eb.build());
    }


    public String getMemoryString(long l) {
        DecimalFormat df = new DecimalFormat("#.00");
        if (l > GB)
            return df.format(l / GB) + "GB";
        else
            return df.format(l / MB) + "MB";
    }

}
