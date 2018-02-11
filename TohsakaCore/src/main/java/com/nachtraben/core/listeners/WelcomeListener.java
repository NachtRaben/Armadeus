package com.nachtraben.core.listeners;

import com.nachtraben.core.DiscordBot;
import com.nachtraben.core.configuration.GuildConfig;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class WelcomeListener extends ListenerAdapter {

    private DiscordBot dbot;

    public WelcomeListener(DiscordBot dbot) {
        this.dbot = dbot;
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent e) {
        GuildConfig config = dbot.getGuildManager().getConfigurationFor(e.getGuild().getIdLong());
        String action = config.getMetadata().get("welcome_action");
        String message = config.getMetadata().get("welcome_message");
        //String channel = config.getMetadata().get("welcome_channel");   //Will be used to set custom welcome channel later.

        // If unconfigured, don't do anything.
        if(action == null || message == null)
            return;

        // Filter out and replace some tags.
        final String msg = message.replaceAll("%username%", e.getMember().getUser().getName())
            .replaceAll("%nickname%", e.getMember().getEffectiveName())
            .replaceAll("%guild", e.getGuild().getName())
            .replaceAll("%mention%", e.getUser().getAsMention());
        

        // Send based on action.
        // TODO: Implement setting this metadata
        if(action.equalsIgnoreCase("dm")) {
            e.getMember().getUser().openPrivateChannel().queue(pc -> pc.sendMessage(msg).queue());
        } else if(action.equalsIgnoreCase("message")) {
            if (msg != null) {
                e.getGuild().getDefaultChannel().sendMessage(msg).queue();
            }
        }
    }

}
