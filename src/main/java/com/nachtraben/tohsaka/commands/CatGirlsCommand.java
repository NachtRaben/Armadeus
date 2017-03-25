package com.nachtraben.tohsaka.commands;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.nachtraben.core.command.GuildCommandSender;
import com.nachtraben.core.commandmodule.Command;
import com.nachtraben.core.commandmodule.CommandSender;
import com.nachtraben.core.utils.MessageTargetType;
import com.nachtraben.core.utils.MessageUtils;
import net.dv8tion.jda.core.EmbedBuilder;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by NachtRaben on 3/12/2017.
 */
public class CatGirlsCommand extends Command {

	private static final Logger LOGGER = LoggerFactory.getLogger(CatGirlsCommand.class);
	private static final String BASEURL = "http://catgirls.brussell98.tk/api/random";
	private static final String NSFWURL = "http://catgirls.brussell98.tk/api/nsfw/random";

	public CatGirlsCommand() {
		super("neko", "[optional]");
	}

	@Override
	public void run(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
		if(sender instanceof GuildCommandSender) {
			GuildCommandSender s = (GuildCommandSender) sender;
			try {
				JSONObject response;
				if(args.get("optional") != null && args.get("optional").toLowerCase().equals("nsfw"))
					response = Unirest.get(NSFWURL).asJson().getBody().getObject();
				else
					response = Unirest.get(BASEURL).asJson().getBody().getObject();

				if(response.has("url"))
					MessageUtils.sendMessage(MessageTargetType.GENERIC, s.getChannel(), new EmbedBuilder().setImage(response.get("url").toString()).setFooter("Requested by " + s.getMember().getEffectiveName(), s.getUser().getAvatarUrl()).build());
			} catch (UnirestException e) {
				LOGGER.warn("Failed to query catgirls api!", e);
			}
		}
	}
}
