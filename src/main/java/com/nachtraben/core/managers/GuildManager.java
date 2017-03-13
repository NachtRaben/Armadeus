package com.nachtraben.core.managers;

import com.nachtraben.core.JDABot;
import com.nachtraben.core.audio.GuildMusicManager;
import com.nachtraben.core.audio.TrackScheduler;
import com.nachtraben.core.configuration.GuildConfig;
import com.nachtraben.core.utils.MessageTargetType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.HashMap;

/**
 * Created by NachtRaben on 2/20/2017.
 */
public class GuildManager {

    private static HashMap<String, GuildManager> GUILDS = new HashMap<>();

    private Guild guild;
    private String id;
    private GuildConfig config;
    private GuildMusicManager audioManager;

    public GuildManager(Guild guild) {
        if(guild == null) throw new IllegalArgumentException("Guild cannot be null!");
        this.id = guild.getId();
        config = new GuildConfig(id).load();
    }

    public Guild getGuild() {
    	if(guild == null)
    		guild = JDABot.getInstance().getGuildById(id);
        return guild;
    }

    public GuildConfig getConfig() {
        return this.config;
    }

    public GuildMusicManager getAudioManager() {
        if(audioManager == null) {
            setAudioManager(new GuildMusicManager(this, GuildMusicManager.DEFAULT_PLAYER_MANAGER));;
        }
        return audioManager;
    }

    public void setAudioManager(GuildMusicManager audioManager) {
        if(this.audioManager != null) {
			this.audioManager.getPlayer().destroy();
		}
        this.audioManager = audioManager;
        this.audioManager.setTrackScheduler(new TrackScheduler(this.audioManager));
        getGuild().getAudioManager().setSendingHandler(this.audioManager.getSendHandler());
    }

    public static GuildManager getManagerFor(String id) {
        return GUILDS.computeIfAbsent(id, m -> new GuildManager(JDABot.getInstance().getGuildById(id)));
    }

	public static GuildManager getManagerFor(Guild guild) {
		return GUILDS.computeIfAbsent(guild.getId(), m -> new GuildManager(guild));
	}

	/* Convenience Messages */

	public TextChannel getRecommendedChannelFor(MessageTargetType type) {
		switch(type) {
			case GENERIC:
				if(config.getGenericLogChannel() != null) return config.getGenericLogChannel();
				return null;
			case ADMIN:
				if(config.getAdminLogChannel() != null) return config.getGenericLogChannel();
				else return config.getGenericLogChannel();
			case MUSIC:
				if(config.getMusicLogChannel() != null) return config.getMusicLogChannel();
				else return config.getGenericLogChannel();
		}
		return null;
	}

}
