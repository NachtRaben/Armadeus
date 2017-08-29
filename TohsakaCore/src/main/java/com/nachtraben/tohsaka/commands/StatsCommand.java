package com.nachtraben.tohsaka.commands;

import com.nachtraben.core.command.DiscordCommandSender;
import com.nachtraben.core.command.GuildCommandSender;
import com.nachtraben.core.util.ChannelTarget;
import com.nachtraben.orangeslice.CommandSender;
import com.nachtraben.orangeslice.command.Command;
import com.nachtraben.tohsaka.Tohsaka;
import com.sedmelluq.discord.lavaplayer.tools.PlayerLibrary;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDAInfo;

import java.util.Map;

public class StatsCommand extends Command {

    public StatsCommand() {
        super("stats", "", "Gets statistical information about the bot.");
    }

    @Override
    public void run(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
        if (sender instanceof DiscordCommandSender) {
            DiscordCommandSender sendee = (DiscordCommandSender) sender;
            Runtime rt = Runtime.getRuntime();
            EmbedBuilder eb = new EmbedBuilder();
            String stats = String.join("\n",
                    "__**Statistics**__",
                    "Server: [OS Name/Arch/Version] [" + System.getProperty("os.name") + "/" + System.getProperty("os.arch") + "/" + System.getProperty("os.version") + "]",
                    "Ram: [Used/Total] [" + (rt.totalMemory() - rt.freeMemory()) / 1024 / 1024 + "Mb/" + rt.totalMemory() / 1024 / 1024 + " Mb]",
                    "Shards: [Current/Total] " + (sendee.getUser().getJDA().getShardInfo() != null ? sendee.getUser().getJDA().getShardInfo().getShardString().replace(" ", "") : "[1/1]"),
                    "Theads: " + Thread.getAllStackTraces().keySet().size(),
                    "",
                    "__**Discord Stats**__",
                    "",
                    "**Channels:**",
                    "Total Channels: " + Tohsaka.getInstance().getTotalChannels(),
                    "Text Channels: " + Tohsaka.getInstance().getTextChannels(),
                    "Voice Channels: " + Tohsaka.getInstance().getVoiceChannels(),
                    "Private Channels: " + Tohsaka.getInstance().getPrivateChannels(),
                    "",
                    "**Other**",
                    "Guilds: " + Tohsaka.getInstance().getGuildCount(),
                    "Users: " + Tohsaka.getInstance().getUserCount(),
                    "",
                    "__**Libraries**__",
                    "JDA: " + JDAInfo.VERSION,
                    "Lavaplayer: " + PlayerLibrary.VERSION,
                    "Java: " + System.getProperty("java.version"),
                    "",
                    "__**Developers**__",
                    "NachtRaben#8307: Its my child, deal with it."
            );
            eb.setDescription(stats);
            sendee.sendMessage(ChannelTarget.GENERIC, eb.build());
        } else {
            sender.sendMessage("Sorry but that command is only available in guilds I'm a part of.");
        }
    }

}