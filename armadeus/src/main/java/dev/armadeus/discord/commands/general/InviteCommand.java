package dev.armadeus.discord.commands.general;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import dev.armadeus.bot.api.command.DiscordCommand;
import dev.armadeus.bot.api.command.DiscordCommandIssuer;
import dev.armadeus.bot.api.util.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.SelfUser;

public class InviteCommand extends DiscordCommand {

    @Default
    @CommandAlias("invite|join")
    @CommandPermission("armadeus.invite")
    @Description("Generate an invite embed to add the bot to new servers")
    public void invite(DiscordCommandIssuer user) {
        EmbedBuilder embedBuilder = EmbedUtils.newBuilder(user);
        SelfUser bot = user.getJda().getSelfUser();
        embedBuilder.setAuthor(bot.getName(), "https://armadeus.net", bot.getAvatarUrl());
        embedBuilder.setDescription("Invite me: [link](https://discord.com/oauth2/authorize?scope=bot&client_id=" + user.getJda().getSelfUser().getIdLong() + "&permissions=892726979)" +
                "\nSupport: [link](https://discord.armadeus.net/)");
        user.sendMessage(embedBuilder.build());
    }

}
