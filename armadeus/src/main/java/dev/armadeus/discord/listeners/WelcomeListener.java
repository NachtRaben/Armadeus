package dev.armadeus.discord.listeners;

import com.electronwill.nightconfig.core.CommentedConfig;
import dev.armadeus.bot.api.ArmaCore;
import dev.armadeus.bot.api.config.GuildConfig;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class WelcomeListener extends ListenerAdapter {

    private final ArmaCore core;

    public WelcomeListener(ArmaCore core) {
        this.core = core;
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent e) {
        GuildConfig config = core.getGuildManager().getConfigFor(e.getGuild().getIdLong());
        CommentedConfig welcomer = config.getMetadata("arma-welcomer");
        if(welcomer == null)
            return;
        String action = welcomer.get("action");
        String message = welcomer.get("message");

        // If unconfigured, don't do anything.
        if (action.isEmpty() || message == null)
            return;

        // Filter out and replace some tags.
        String msg = message.replaceAll("%username%", e.getMember().getUser().getName())
                .replaceAll("%nickname%", e.getMember().getEffectiveName())
                .replaceAll("%guild%", e.getGuild().getName())
                .replaceAll("%mention%", e.getUser().getAsMention());


        // Send based on action.
        // TODO: Implement setting this metadata
        if (action.equalsIgnoreCase("dm")) {
            e.getMember().getUser().openPrivateChannel().queue(pc -> pc.sendMessage(msg).queue());
        } else if (action.equalsIgnoreCase("message") && e.getGuild().getDefaultChannel() != null) {
            e.getGuild().getDefaultChannel().sendMessage(msg).queue();
        }
    }

}