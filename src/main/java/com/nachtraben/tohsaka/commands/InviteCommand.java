package com.nachtraben.tohsaka.commands;

import com.nachtraben.core.command.DiscordCommandSender;
import com.nachtraben.core.util.ChannelTarget;
import com.nachtraben.orangeslice.CommandSender;
import com.nachtraben.orangeslice.command.Command;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.SelfUser;

import java.util.Map;

public class InviteCommand extends Command {

    public InviteCommand() {
        super("invite", "", "Get Tohsaka's invite information.");
    }

    @Override
    public void run(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
        if (sender instanceof DiscordCommandSender) {
            DiscordCommandSender sendee = (DiscordCommandSender) sender;
            EmbedBuilder embedBuilder = new EmbedBuilder();
            SelfUser bot = sendee.getUser().getJDA().getSelfUser();
            embedBuilder.setAuthor(bot.getName(), "https://tohsakabot.com", bot.getAvatarUrl());
            embedBuilder.setDescription("Invite me: [link](" + sendee.getMessage().getJDA().asBot().getInviteUrl(Permission.ADMINISTRATOR) + ")" +
                    "\nSupport: [link](https://discord.gg/jmKhbar)");
            sendee.sendMessage(ChannelTarget.GENERIC, embedBuilder.build());
        }
    }
}
