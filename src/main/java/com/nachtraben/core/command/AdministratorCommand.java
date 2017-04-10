package com.nachtraben.core.command;

import com.nachtraben.commandapi.Command;
import com.nachtraben.commandapi.CommandSender;
import com.nachtraben.core.utils.MessageTargetType;
import com.nachtraben.core.utils.MessageUtils;
import net.dv8tion.jda.core.Permission;

import java.util.Map;

/**
 * Created by NachtRaben on 4/2/2017.
 */
public abstract class AdministratorCommand extends Command {

    public AdministratorCommand(String name, String format) {
        super(name, format);
    }

    public abstract void onGuildSender(GuildCommandSender sender, Map<String, String> args, Map<String, String> flags);

    @Override
    public void run(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
        if(sender instanceof GuildCommandSender) {
            GuildCommandSender sendee = (GuildCommandSender) sender;
            if(sendee.getMember().isOwner() || sendee.getMember().hasPermission(Permission.ADMINISTRATOR) || sendee.getUser().getId().equals("118255810613608451"))
                onGuildSender(sendee, args, flags);
            else
                MessageUtils.sendMessage(MessageTargetType.GENERIC, sendee.getChannel(), "You do not have the Administrative permissions to run this command!");
        }
    }

}
