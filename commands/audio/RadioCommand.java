package dev.armadeus.bot.commands.audio;

import dev.armadeus.core.audio.Radio;
import dev.armadeus.core.command.GuildCommandSender;
import dev.armadeus.core.managers.GuildMusicManager;
import dev.armadeus.core.util.ChannelTarget;
import dev.armadeus.command.CommandSender;
import dev.armadeus.command.command.Command;

import java.util.Arrays;
import java.util.Map;

public class RadioCommand extends Command {

    public RadioCommand() {
        super("radio", "<station>", "Plays one of the specified radio stations.");
    }

    @Override
    public void run(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
        if(sender instanceof GuildCommandSender) {
            GuildCommandSender sendee = (GuildCommandSender) sender;
            GuildMusicManager manager = sendee.getGuildConfig().getMusicManager();

            if (manager.getLink().getChannelId() == -1 && sendee.getVoiceChannel() == null) {
                sendee.sendMessage(ChannelTarget.MUSIC, "Sorry but you have to be in a voice channel to play music.");
                return;
            }

            Radio r = Radio.forName(args.get("station"));
            if(r == null) {
                sendee.sendMessage(ChannelTarget.MUSIC, "Sorry, but I only support the following stations, `" + Arrays.toString(Radio.values()) + "`.");
                return;
            }

            r.playRadio(sendee);
        } else {
            sender.sendMessage("Sorry but that command is only available in guilds I'm a part of.");
        }
    }
}
