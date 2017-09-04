package com.nachtraben.tohsaka.commands;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.nachtraben.core.command.DiscordCommandSender;
import com.nachtraben.core.command.GuildCommandSender;
import com.nachtraben.orangeslice.CommandSender;
import com.nachtraben.orangeslice.command.Cmd;
import com.nachtraben.orangeslice.command.Command;
import com.vdurmont.emoji.EmojiParser;
import net.dv8tion.jda.core.EmbedBuilder;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class CatGirlsCommands {

    private static final Logger LOGGER = LoggerFactory.getLogger(CatGirlsCommands.class);
    private static final String BASEURL = "http://catgirls.brussell98.tk/api/random";
    private static final String NSFWURL = "http://catgirls.brussell98.tk/api/nsfw/random";


    @Cmd(name = "neko", format = "", description = "Sends a cute cat for your viewing pleasure.")
    public void neko(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
        if (sender instanceof DiscordCommandSender) {
                sendNeko((DiscordCommandSender) sender, false);
        }
    }

    @Cmd(name = "nsfwneko", format = "", description = "Sends a lewd cat for your other pleasures.")
    public void nsfwNeko(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
        if(sender instanceof DiscordCommandSender) {
            if(sender instanceof GuildCommandSender) {
                GuildCommandSender sendee = (GuildCommandSender) sender;
                if (!sendee.getTextChannel().isNSFW()) {
                    sendee.getTextChannel().sendMessage("Sorry, " + sendee.getMember().getAsMention() + " but I can't satisfy that desire in here. " + EmojiParser.parseToUnicode(":cry:")).queue();
                    return;
                }
                sendNeko((DiscordCommandSender) sender, true);
            } else {
                sendNeko((DiscordCommandSender) sender, true);
            }
        }
    }

    private void sendNeko(DiscordCommandSender sender, boolean nsfw) {
        try {
            JSONObject response;
            if(nsfw) {
                response = Unirest.get(NSFWURL).asJson().getBody().getObject();
            } else {
                response = Unirest.get(BASEURL).asJson().getBody().getObject();
            }
            if (response.has("url")) {
                EmbedBuilder eb = new EmbedBuilder();
                eb.setImage(response.getString("url"));
                if(sender instanceof GuildCommandSender) {
                    GuildCommandSender sendee = (GuildCommandSender) sender;
                    eb.setFooter("Requested by " + sendee.getMember().getEffectiveName(), sendee.getUser().getAvatarUrl());
                }
                sender.sendMessage(eb.build());
            }
        } catch (UnirestException e) {
            LOGGER.warn ("Failed to query catgirls api!", e);
        }
        sender.sendMessage("Sorry but I was unable to query the website.");
    }
}
