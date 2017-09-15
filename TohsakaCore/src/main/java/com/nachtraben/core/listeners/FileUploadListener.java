package com.nachtraben.core.listeners;

import com.nachtraben.core.command.GuildCommandSender;
import com.nachtraben.core.configuration.GuildConfig;
import com.nachtraben.core.managers.GuildMusicManager;
import com.nachtraben.core.util.ChannelTarget;
import com.nachtraben.tohsaka.Tohsaka;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("Duplicates")
public class FileUploadListener extends ListenerAdapter {

    private static final File CACHE_DIR = new File("temp");
    private static final Map<String, File> CACHE = new HashMap<>();
    private static Field titleField;
    private static Field artistField;

    static {
        if (!CACHE_DIR.exists())
            CACHE_DIR.mkdirs();

        for(File f : CACHE_DIR.listFiles()) {
            if(f.isFile())
                f.delete();
        }

        try {
            Class clz = AudioTrackInfo.class;
            titleField = clz.getDeclaredField("title");
            titleField.setAccessible(true);
            artistField = clz.getDeclaredField("author");
            artistField.setAccessible(true);

            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);

            modifiersField.setInt(titleField, titleField.getModifiers() & ~Modifier.FINAL);
            modifiersField.setInt(artistField, artistField.getModifiers() & ~Modifier.FINAL);
        } catch (IllegalAccessException | NoSuchFieldException e) {
        }
    }

    private void modifyTrackInfo(AudioTrackInfo info, String title, String artist) {
        try {
            if(titleField != null)
                titleField.set(info, title);
            if(artistField != null)
                artistField.set(info, artist);
        } catch (IllegalAccessException e) {}
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        Message m = event.getMessage();
        GuildCommandSender sender = new GuildCommandSender(Tohsaka.getInstance(), m);
        if(Tohsaka.getInstance().isDebugging())
            return;

        if (!m.getAuthor().isBot() && !m.getAuthor().isFake() && !m.getAttachments().isEmpty()) {
            Guild g = m.getGuild();
            GuildConfig config = Tohsaka.getInstance().getGuildManager().getConfigurationFor(g);
            TextChannel channel = config.getLogChannel(ChannelTarget.MUSIC);
            if(channel != null && m.getTextChannel().getIdLong() == channel.getIdLong()) {
                GuildMusicManager manager = null;
                for (Message.Attachment att : m.getAttachments()) {
                    if (!att.isImage()) {
                        if (manager == null)
                            manager = config.getMusicManager();

                        if (!manager.getScheduler().isPlaying() && sender.getVoiceChannel() == null) {
                            sender.sendMessage(ChannelTarget.MUSIC, "Sorry but you have to be in a voice channel to play music.");
                            return;
                        }

                        File f = CACHE.computeIfAbsent(att.getFileName(), file -> {
                            File ff = null;
                            while (ff == null || ff.exists()) {
                                ff = new File(CACHE_DIR, UUID.randomUUID().toString() + att.getFileName().substring(att.getFileName().lastIndexOf(".")));
                            }
                            ff.deleteOnExit();
                            return ff;
                        });
                        if (f == null)
                            return;

                        att.download(f);
                        GuildMusicManager finalManager = manager;
                        manager.getPlayerManager().loadItemOrdered(f, f.getAbsolutePath(), new AudioLoadResultHandler() {
                            @Override
                            public void trackLoaded(AudioTrack track) {
                                AudioTrackInfo inf = track.getInfo();
                                if(inf.title.equalsIgnoreCase("unknown title"))
                                    modifyTrackInfo(inf, att.getFileName().substring(0, att.getFileName().lastIndexOf(".")).replace("_", " "), "Unknown artist");
                                sender.sendMessage(ChannelTarget.MUSIC, String.format("Adding to queue, `%s` by `%s`.", track.getInfo().title, track.getInfo().author));
                                track.setUserData(sender);
                                finalManager.getScheduler().queue(track);
                            }

                            @Override
                            public void playlistLoaded(AudioPlaylist playlist) {

                            }

                            @Override
                            public void noMatches() {
                                sender.sendMessage(ChannelTarget.MUSIC, "Sorry, but I was unable to play `" + att.getFileName().replace("_", "") + "`.");
                            }

                            @Override
                            public void loadFailed(FriendlyException exception) {
                                sender.sendMessage(ChannelTarget.MUSIC, "Sorry but I was unable to play that file, `" + exception.getMessage() + "`.");
                            }
                        });
                        if(config.shouldDeleteCommands()) {
                            try {
                                m.delete().complete();
                            } catch (Exception ignored){}
                        }
                    }
                }
            }
        }
    }
}
