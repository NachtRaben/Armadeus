package dev.armadeus.discord.welcome.listeners;

import com.electronwill.nightconfig.core.Config;
import dev.armadeus.bot.api.ArmaCore;
import dev.armadeus.discord.welcome.ArmaWelcome;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class WelcomeListener extends ListenerAdapter {

    private final ArmaCore core;

    public WelcomeListener(ArmaCore core) {
        this.core = core;
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent e) {
        Config wConf = ArmaWelcome.get().getConfig(e.getGuild());
        if(wConf == null)
            return;

        boolean enabled = wConf.get("enabled");
        boolean dm = wConf.get("dm");
        String message = wConf.get("message");

        // If unconfigured, don't do anything.
        if (!enabled || message == null)
            return;

        // Filter out and replace some tags.
        String msg = message.replaceAll("%username%", e.getMember().getUser().getName())
                .replaceAll("%nickname%", e.getMember().getEffectiveName())
                .replaceAll("%guild%", e.getGuild().getName())
                .replaceAll("%mention%", e.getUser().getAsMention());


        // Send based on action.
        // TODO: Implement setting this metadata
        if (dm) {
            e.getMember().getUser().openPrivateChannel().queue(pc -> pc.sendMessage(msg).queue());
        } else if (e.getGuild().getDefaultChannel() != null) {
            e.getGuild().getDefaultChannel().asTextChannel().sendMessage(msg).queue();
        }
    }

}