package dev.armadeus.discord.audio.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import com.velocitypowered.api.event.Subscribe;
import dev.armadeus.bot.api.command.DiscordCommandIssuer;
import dev.armadeus.bot.api.util.TimeUtil;
import dev.armadeus.discord.audio.ArmaAudio;
import dev.armadeus.discord.audio.AudioManager;
import dev.armadeus.discord.audio.util.AudioEmbedUtils;
import lavalink.client.player.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.List;

@Conditions("guildonly")
@CommandAlias("queue")
@CommandPermission("armadeus.queue")
public class QueueCommand extends AudioCommand {

    private static final String trackDescription = "\n**[%s](%s)** ) `%s` for (`%s`)";

    public QueueCommand() {
        ArmaAudio.core().eventManager().register(ArmaAudio.get(), this);
    }

    @Subscribe
    public void onButtonInteract(ButtonInteractionEvent event) {
        String label = event.getButton().getId();
        String[] tokens = label.split("-");
        Member member = event.getMember();
        event.deferEdit().queue();
        if (member == null || tokens.length != 2 || !tokens[0].equals("armaqueue")) {
            return;
        }
        int index = Integer.parseInt(tokens[1]);
        AudioManager manager = getAudioManager(event.getMember());
        MessageBuilder builder = getQueueEmbed(event.getMember(), manager, index);
        event.getMessage().editMessage(builder.build()).queue();
    }

    @Default
    @Description("Shows all songs currently in queue")
    public void queue(DiscordCommandIssuer user) {
        AudioManager manager = getAudioManager(user);
        if (isNotPlaying(user))
            return;

        MessageBuilder builder = getQueueEmbed(user.getMember(), manager, 1);
        user.sendMessage(builder.build());
    }

    private MessageBuilder getQueueEmbed(Member member, AudioManager manager, int index) {
        if (!manager.getPlayer().isPlaying())
            return new MessageBuilder("There are no tracks being played");
        AudioTrack current = manager.getPlayer().getPlayingTrack();
        List<AudioTrack> tracks = manager.getPlayer().getScheduler().getQueue();
        long totalTime = current.getInfo().isStream() ? 0 : current.getInfo().getLength() - manager.getPlayer().getTrackPosition();
        totalTime += tracks.stream().mapToLong(track -> track.getInfo().isStream() ? 0 : track.getInfo().getLength()).sum();

        EmbedBuilder eb = new EmbedBuilder(AudioEmbedUtils.getNowPlayingEmbed(member, current));
        eb.setAuthor(member.getGuild().getSelfMember().getEffectiveName() + "'s Queue:",
                EmbedBuilder.URL_PATTERN.matcher(current.getInfo().getUri()).matches() ? current.getInfo().getUri() : null,
                null);

        int pages = (int) Math.ceil((double) tracks.size() / 10.0d);

        int start = Math.max(0, 10 * (index - 1));
        int end = Math.min(start + 10, tracks.size());

        eb.getDescriptionBuilder().insert(0, "**Currently Playing:** ");
        eb.appendDescription("\n\n**Queued:**\n");

        for (int i = start; i < end; i++) {
            AudioTrack track = tracks.get(i);
            String message = String.format(trackDescription, i + 1, track.getInfo().getUri(), track.getInfo().getTitle(), (track.getInfo().isStream() ? "Stream" : TimeUtil.format(track.getInfo().getLength())));
            if (eb.getDescriptionBuilder().length() + message.length() > MessageEmbed.TEXT_MAX_LENGTH)
                break;
            else
                eb.appendDescription(message);
        }
        eb.setFooter("Tracks: " + (tracks.size() + 1) + " Runtime: " + TimeUtil.format(totalTime), member.getJDA().getSelfUser().getAvatarUrl());
        MessageBuilder builder = new MessageBuilder()
                .setEmbeds(eb.build());
        if (pages > 1) {
            if (index == 1) {
                builder.setActionRows(ActionRow.of(
                        Button.secondary("armaqueue-current", String.valueOf(index) + "/" + pages).asDisabled(),
                        Button.primary("armaqueue-" + String.valueOf(index + 1), "▶️")
                ));
            } else if (index == pages) {
                builder.setActionRows(ActionRow.of(
                        Button.primary("armaqueue-" + String.valueOf(index - 1), "◀️"),
                        Button.secondary("armaqueue-current", String.valueOf(index) + "/" + pages).asDisabled()
                ));
            } else {
                builder.setActionRows(ActionRow.of(
                        Button.primary("armaqueue-" + String.valueOf(index - 1), "◀️"),
                        Button.secondary("armaqueue-current", String.valueOf(index) + "/" + pages).asDisabled(),
                        Button.primary("armaqueue-" + String.valueOf(index + 1), "▶️")
                ));
            }
        }
        return builder;
    }
}
