package dev.armadeus.discord.commands.general;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import dev.armadeus.bot.api.command.DiscordCommand;
import dev.armadeus.bot.api.command.DiscordCommandIssuer;
import dev.armadeus.discord.util.EmbedUtils;
import dev.armadeus.discord.util.TimeUtil;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

public class UptimeCommand extends DiscordCommand {

    private static final RuntimeMXBean rb = ManagementFactory.getRuntimeMXBean();

    @Default
    @CommandAlias("uptime|up")
    public void uptime(DiscordCommandIssuer user) {
        user.sendMessage(EmbedUtils.newBuilder(user)
                .setDescription("__**Uptime**__")
                .setFooter(TimeUtil.format(rb.getUptime()))
                .build());
    }

}
