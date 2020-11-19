package com.nachtraben.armadeus.commands;

import com.nachtraben.core.command.DiscordCommandSender;
import com.nachtraben.core.util.ChannelTarget;
import com.nachtraben.orangeslice.CommandSender;
import com.nachtraben.orangeslice.command.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.SelfUser;

import java.util.Map;

public class InviteCommand extends Command {

    public InviteCommand() {
        super("invite", "", "Get Armadeus's invite information.");
    }

    @Override
    public void run(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
        if (sender instanceof DiscordCommandSender) {
            DiscordCommandSender sendee = (DiscordCommandSender) sender;
            EmbedBuilder embedBuilder = new EmbedBuilder();
            SelfUser bot = sendee.getUser().getJDA().getSelfUser();
            embedBuilder.setAuthor(bot.getName(), "https://Armadeusbot.com", bot.getAvatarUrl());
            embedBuilder.setDescription("Invite me: [link](" + sendee.getMessage().getJDA().getInviteUrl(Permission.ADMINISTRATOR) + ")" +
                    "\nSupport: [link](https://discord.gg/jmKhbar)");
            sendee.sendMessage(ChannelTarget.GENERIC, embedBuilder.build());
        }
    }
}
