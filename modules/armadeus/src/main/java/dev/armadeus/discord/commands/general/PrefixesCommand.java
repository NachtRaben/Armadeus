package dev.armadeus.discord.commands.general;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import dev.armadeus.bot.api.command.DiscordCommand;
import dev.armadeus.bot.api.command.DiscordCommandIssuer;
import dev.armadeus.bot.api.config.GuildConfig;
import dev.armadeus.discord.util.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;

public class PrefixesCommand extends DiscordCommand {

    @Default
    @CommandAlias("prefixes")
    @CommandPermission("armadeus.prefixes")
    @Description("Show all prefixes the bot will respond to in your guild")
    public void about(DiscordCommandIssuer user) {
        EmbedBuilder builder = EmbedUtils.newBuilder(user);
        StringBuilder sb = new StringBuilder();
        sb.append("`");
        if (user.isFromGuild()) {
            GuildConfig config = user.getGuildConfig();
            if (!config.getPrefixes().isEmpty())
                sb.append(config.getPrefixes());
            else
                sb.append(core.armaConfig().getDefaultPrefixes());
        } else {
            sb.append(core.armaConfig().getDefaultPrefixes());
        }
        sb.append("`");
        builder.addField("Prefixes", sb.toString(), false);
        user.sendMessage(builder.build());
    }

}
