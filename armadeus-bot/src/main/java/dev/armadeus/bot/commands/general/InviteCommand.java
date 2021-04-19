package dev.armadeus.bot.commands.general;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import dev.armadeus.core.command.DiscordCommand;
import dev.armadeus.core.command.DiscordUser;
import dev.armadeus.core.util.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.SelfUser;

public class InviteCommand extends DiscordCommand {

    @Default
    @CommandAlias("invite|join")
    public void invite(DiscordUser user) {
        EmbedBuilder embedBuilder = EmbedUtils.newBuilder(user);
        SelfUser bot = user.getJda().getSelfUser();
        embedBuilder.setAuthor(bot.getName(), "https://Armadeusbot.com", bot.getAvatarUrl());
        embedBuilder.setDescription("Invite me: [link](https://discord.armadeus.net/invite)" +
                "\nSupport: [link](https://discord.gg/jmKhbar)");
        user.sendMessage(embedBuilder.build());
    }

}
