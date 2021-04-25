package dev.armadeus.discord.commands.general;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import dev.armadeus.bot.api.ArmaCore;
import dev.armadeus.bot.api.command.DiscordCommand;
import dev.armadeus.bot.api.command.DiscordCommandIssuer;
import dev.armadeus.bot.api.config.GuildConfig;
import dev.armadeus.discord.util.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ApplicationInfo;
import net.dv8tion.jda.api.entities.SelfUser;

public class AboutCommand extends DiscordCommand {

    @Default
    @CommandAlias("about")
    public void about(DiscordCommandIssuer user) {
        EmbedBuilder builder = EmbedUtils.newBuilder(user);
        ApplicationInfo info = user.getJda().retrieveApplicationInfo().complete();
        SelfUser bot = user.getJda().getSelfUser();
        builder.setAuthor(bot.getName(), "https://armadeus.net", bot.getAvatarUrl());
        builder.setThumbnail(info.getIconUrl());
        builder.setTitle("About Armadeus:", null);
        builder.setDescription(info.getDescription());
        builder.addField("Links:", "Invite me: [Invite](https://discord.armadeus.net/invite)" +
                "\nSupport: [Discord](https://discord.armadeus.net)" +
                "\nDonations: [PayPal](https://paypal.me/nachtraben)", false);
        builder.setFooter(String.format("Author: %s#%s", info.getOwner().getName(), info.getOwner().getDiscriminator()), info.getOwner().getAvatarUrl());

        StringBuilder sb = new StringBuilder();
        sb.append("`");
        if (user.isFromGuild()) {
            GuildConfig config = user.getGuildConfig();
            if (!config.getPrefixes().isEmpty())
                sb.append(config.getPrefixes());
            else
                sb.append(ArmaCore.get().getArmaConfig().getDefaultPrefixes());
        } else {
            sb.append(ArmaCore.get().getArmaConfig().getDefaultPrefixes());
        }
        sb.append("`");

        builder.addField("Prefixes", sb.toString(), false);
        user.sendMessage(builder.build());
    }

}
