package com.nachtraben.tohsaka.commands;

import com.nachtraben.core.command.DiscordCommandSender;
import com.nachtraben.core.command.GuildCommandSender;
import com.nachtraben.core.configuration.GuildConfig;
import com.nachtraben.core.managers.GuildManager;
import com.nachtraben.core.util.ChannelTarget;
import com.nachtraben.core.util.Utils;
import com.nachtraben.orangeslice.CommandSender;
import com.nachtraben.orangeslice.command.Command;
import com.nachtraben.tohsaka.Tohsaka;
import net.dv8tion.jda.bot.entities.ApplicationInfo;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.SelfUser;

import java.util.Map;

public class AboutCommand extends Command {

    public AboutCommand() {
        super("about", "", "Information about the bot.");
    }

    @Override
    public void run(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
        if (sender instanceof DiscordCommandSender) {
            DiscordCommandSender sendee = (DiscordCommandSender) sender;
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(Utils.randomColor());
            ApplicationInfo info = sendee.getUser().getJDA().asBot().getApplicationInfo().complete();
            SelfUser bot = sendee.getUser().getJDA().getSelfUser();
            builder.setAuthor(bot.getName(), "https://tohsakabot.com", bot.getAvatarUrl());
            builder.setThumbnail(info.getIconUrl());
            builder.setTitle("About Tohsaka:", null);
            builder.setDescription(info.getDescription());
            builder.addField("Links:","Invite me: [Invite](" + bot.getJDA().asBot().getInviteUrl(Permission.ADMINISTRATOR) + ")" +
                    "\nSupport: [Discord](https://discord.gg/mmYZGGB)" +
                    "\nDonations: [PayPal](https://paypal.me/nachtraben)", false);
            builder.setFooter(String.format("Author: %s#%s", info.getOwner().getName(), info.getOwner().getDiscriminator()), info.getOwner().getAvatarUrl());

            StringBuilder sb = new StringBuilder();
            sb.append("`");
            if(sendee instanceof GuildCommandSender) {
                GuildCommandSender sendeee = (GuildCommandSender) sendee;
                GuildConfig config = Tohsaka.getInstance().getGuildManager().getConfigurationFor(sendeee.getGuild().getIdLong());
                if(!config.getPrefixes().isEmpty())
                    sb.append(config.getPrefixes().toString());
                else
                    sb.append(Tohsaka.getInstance().getConfig().getPrefixes());
            } else {
                sb.append(Tohsaka.getInstance().getConfig().getPrefixes());
            }
            sb.append("`");

            builder.addField("Prefixes", sb.toString(), false);
            sendee.sendMessage(ChannelTarget.GENERIC, builder.build());
        }
    }
}
