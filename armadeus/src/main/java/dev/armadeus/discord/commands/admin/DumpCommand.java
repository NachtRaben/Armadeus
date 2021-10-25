package dev.armadeus.discord.commands.admin;

import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Private;
import dev.armadeus.bot.api.command.DiscordCommand;
import dev.armadeus.bot.api.command.DiscordCommandIssuer;
import dev.armadeus.bot.api.util.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DumpCommand extends DiscordCommand {

    @Private
    @Conditions("owneronly")
    @CommandAlias("eval")
    @Default
    @CatchUnknown
    public void dump(DiscordCommandIssuer user) {
        Map<User, List<Guild>> guilds = new HashMap<>();
        core.shardManager().getGuilds().forEach(g -> {
            List<Guild> collections = guilds.computeIfAbsent(g.getOwner().getUser(), j -> new ArrayList<>());
            collections.add(g);
        });
        EmbedBuilder builder = EmbedUtils.newBuilder(user);
        builder.setTitle("Ownership Dump");
        for (Map.Entry<User, List<Guild>> entry : guilds.entrySet()) {
            builder.appendDescription(entry.getKey().getAsTag() + " - " + entry.getValue().size() + "\n");
        }
        user.sendMessage(builder.build());
    }
}
