package dev.armadeus.bot.commands.moderation;

import dev.armadeus.core.command.GuildCommandSender;
import dev.armadeus.core.managers.GuildMusicManager;
import dev.armadeus.core.util.ChannelTarget;
import dev.armadeus.command.CommandSender;
import dev.armadeus.command.command.Command;

import java.util.Map;

public class DisconnectCommand extends Command {

    public DisconnectCommand() {
        super("disconnect", "", "Disconnects from the active channel.");
    }

    @Override
    public void run(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
        if (sender instanceof GuildCommandSender) {
            GuildCommandSender sendee = (GuildCommandSender) sender;
            GuildMusicManager manager = sendee.getGuildConfig().getMusicManager();
            if (manager.getLink().getChannelId() != -1) {
                manager.getScheduler().stop();
                manager.getLink().destroy();
                sendee.sendMessage(ChannelTarget.MUSIC, "Guess you don't want me listening anymore :c");
            } else {
                sendee.sendMessage(ChannelTarget.GENERIC, "I'm not connected, why did you feel the need to run that?");
            }
        } else {
            sender.sendMessage("Sorry but that command is only available in guilds I'm a part of.");
        }
    }

}
