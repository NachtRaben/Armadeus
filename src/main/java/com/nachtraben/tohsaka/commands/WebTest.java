package com.nachtraben.tohsaka.commands;

import com.nachtraben.commandapi.Cmd;
import com.nachtraben.commandapi.CommandSender;
import com.nachtraben.core.command.GuildCommandSender;
import com.nachtraben.core.utils.MessageTargetType;
import com.nachtraben.core.utils.MessageUtils;
import com.vdurmont.emoji.EmojiManager;
import com.vdurmont.emoji.EmojiParser;
import net.dv8tion.jda.core.EmbedBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by NachtRaben on 3/4/2017.
 */
public class WebTest {

    private static final Random random = new Random();
    private static final String fourchan = "http://boards.4chan.org";
    private static final String R34_API = "http://rule34.xxx/index.php?page=dapi&s=post&q=index";
    private static final String URBANDICTIONARY = "http://www.urbandictionary.com/define.php?term=";

    @Cmd(name = "r34", format = "{search}", description = "Searches rule34.xxx", flags = {"--5", "--10"})
    public void rule34(CommandSender sender, Map<String, String> args, Map<String, String> flags) {
        if (sender instanceof GuildCommandSender) {
            GuildCommandSender sendee = (GuildCommandSender) sender;
            if (!sendee.getChannel().isNSFW()) {
                sendee.getChannel().sendMessage("Sorry, " + sendee.getMember().getAsMention() + " but I can't satisfy that desire in here. " + EmojiParser.parseToUnicode(":cry:")).queue();
                return;
            }
            String search = args.get("search");
            try {
                String userAgent = "Test"; // Change this to your company's name and bot homepage!
                Elements images = Jsoup.connect(R34_API).userAgent(userAgent)
                        .data("limit", "100")
                        .data("tags", search).get().select("post");
                if (flags.containsKey("5")) {
                    sendr34Images(sendee, search, 5, images);
                } else if (flags.containsKey("10")) {
                    sendr34Images(sendee, search, 10, images);
                } else {
                    int index = random.nextInt(images.size());
                    Element e = images.get(index);
                    String url = e.absUrl("file_url");
                    boolean isWebm = url.endsWith(".webm");
                    EmbedBuilder eb = new EmbedBuilder();
                    eb.setAuthor(search, url, null);
                    if (isWebm)
                        eb.setDescription(EmojiManager.getForAlias(":video_camera:").getUnicode() + " Webm");
                    eb.setImage(url);
                    MessageUtils.sendMessage(MessageTargetType.GENERIC, sendee.getChannel(), eb.build());
                }
            } catch (IOException e) {
                MessageUtils.sendMessage(MessageTargetType.GENERIC, sendee.getChannel(), "Error occurred: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @Cmd(name = "4chan", format = "<search> [amount]", description = "Pulls images from the thread/board.")
    public void chan(CommandSender sender, Map<String, String> args, Map<String, String> flags) throws IOException {
        // TODO: Determine URL board and thread if fully provided
        // TODO: Filter NSFW boards with non NSFW boards, precompile list.

        if (sender instanceof GuildCommandSender) {
            GuildCommandSender sendee = (GuildCommandSender) sender;
            if (!sendee.getChannel().isNSFW()) {
                sendee.getChannel().sendMessage("Sorry, " + sendee.getMember().getAsMention() + " but I can't satisfy that desire in here. " + EmojiParser.parseToUnicode(":cry:")).queue();
                return;
            }
            String chan = args.get("search");
            int amount = 0;
            if (args.containsKey("amount")) {
                try {
                    amount = Integer.parseInt(args.get("amount"));
                    if (amount > 10 || amount <= 0) {
                        MessageUtils.sendMessage(MessageTargetType.GENERIC, sendee.getChannel(), "You must request between [1-10] images.");
                        return;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid number derived from: " + args.get("amount"));
                    return;
                }
            }

            if (!chan.startsWith("http") && chan.length() >= 1) chan = fourchan + "/" + chan;
            if (chan.startsWith("/") && chan.length() >= 2) chan = fourchan + chan;
            String userAgent = "ExampleBot 1.0 (+http://example.com/bot)"; // Change this to your company's name and bot homepage!
            Elements links = Jsoup.connect(chan).userAgent(userAgent).get().select(".fileText>a");

            if (amount != 0) {
                send4chanImages(sendee, amount, links);
            } else {
                int index = random.nextInt(links.size());
                Element e = links.get(index);
                String url = e.absUrl("href");
                MessageUtils.sendMessage(MessageTargetType.GENERIC, sendee.getChannel(), new EmbedBuilder().setAuthor(e.text(), url, null).setImage(url).build());
            }
            System.out.println("SIZE: " + links.size());
        }
    }

    private void sendr34Images(GuildCommandSender sendee, String search, int total, Elements elements) {
        List<Integer> indexes = new ArrayList<>();
        if (total >= elements.size()) {
            for (Element e : elements) {
                String url = e.absUrl("file_url");
                boolean isWebm = url.endsWith(".webm");
                EmbedBuilder eb = new EmbedBuilder();
                eb.setAuthor(search, url, null);
                if (isWebm)
                    eb.setDescription(EmojiManager.getForAlias(":video_camera:").getUnicode() + " Webm");
                eb.setImage(url);
                MessageUtils.sendMessage(MessageTargetType.GENERIC, sendee.getChannel(), eb.build());
            }
        } else {
            for (int i = 0; i < total; i++) {
                int image = random.nextInt(elements.size());
                while (indexes.contains(image)) image = random.nextInt(elements.size());
                Element e = elements.get(image);
                String url = e.absUrl("file_url");
                boolean isWebm = url.endsWith(".webm");
                EmbedBuilder eb = new EmbedBuilder();
                eb.setAuthor(search, url, null);
                if (isWebm)
                    eb.setDescription(EmojiManager.getForAlias(":video_camera:").getUnicode() + " Webm");
                eb.setImage(url);
                MessageUtils.sendMessage(MessageTargetType.GENERIC, sendee.getChannel(), eb.build());
            }
        }
    }

    private void send4chanImages(GuildCommandSender sender, int total, Elements elements) {
        List<Integer> indexes = new ArrayList<>();
        if (total >= elements.size()) {
            for (Element e : elements) {
                String url = e.absUrl("href");
                MessageUtils.sendMessage(MessageTargetType.GENERIC, sender.getChannel(), new EmbedBuilder().setImage(url).build());
            }
        } else {
            for (int i = 0; i < total; i++) {
                int image = random.nextInt(elements.size());
                while (indexes.contains(image)) image = random.nextInt(elements.size());
                Element e = elements.get(image);
                String url = e.absUrl("href");
                MessageUtils.sendMessage(MessageTargetType.GENERIC, sender.getChannel(), new EmbedBuilder().setAuthor(e.text(), url, null).setImage(url).build());
                indexes.add(image);
                System.out.println("URL: \"" + url + "\"");
            }
        }
    }

    @Cmd(name = "google", format = "{term}", description = "Attempts a google search.")
    public void google(CommandSender sender, Map<String, String> args) throws IOException {
        if (sender instanceof GuildCommandSender) {
            GuildCommandSender sendee = (GuildCommandSender) sender;
            String google = "http://www.google.com/search?q=";
            String search = args.get("term");
            String charset = "UTF-8";
            String userAgent = "ExampleBot 1.0 (+http://example.com/bot)"; // Change this to your company's name and bot homepage!
            Elements links = Jsoup.connect(google + URLEncoder.encode(search, charset)).userAgent(userAgent).get().select(".g>.r>a");

            EmbedBuilder builder = new EmbedBuilder();
            builder.setAuthor("Search results for: " + search, google + URLEncoder.encode(search, charset), null);
            for (Element link : links) {
                String title = link.text();
                String url = link.absUrl("href"); // Google returns URLs in format "http://www.google.com/url?q=<url>&sa=U&ei=<someKey>".
                url = URLDecoder.decode(url.substring(url.indexOf('=') + 1, url.indexOf('&')), "UTF-8");

                if (!url.startsWith("http")) {
                    continue; // Ads/news/etc.
                }
                builder.addField(title, url, false);

                System.out.println("Title: " + title);
                System.out.println("URL: " + url);
            }
            MessageUtils.sendMessage(MessageTargetType.GENERIC, sendee.getChannel(), builder.build());
        }

    }

    @Cmd(name = "urban", format = "{search}", description = "Searches urban dictionary for that phrase")
    public void urbandictionary(CommandSender sender, Map<String, String> args) throws IOException {
        if (sender instanceof GuildCommandSender) {
            GuildCommandSender sendee = (GuildCommandSender) sender;
            try {
                String userAgent = "test"; // Change this to your company's name and bot homepage!
                Document doc = Jsoup.connect(URBANDICTIONARY + URLEncoder.encode(args.get("search"), "UTF-8")).userAgent(userAgent).get();
                Elements word = doc.select(".def-header");
                Elements meaning = doc.select(".meaning");
                Elements example = doc.select(".example");

                if (word.size() >= 1 && meaning.size() >= 1 && example.size() >= 1) {
                    EmbedBuilder eb = new EmbedBuilder();
                    eb.setAuthor("Definition for, " + word.get(0).text() + ".", doc.baseUri(), null);
                    eb.setDescription(meaning.get(0).text().substring(0, Math.min(2048, meaning.get(0).text().length())));
                    eb.addField("Example: ", example.get(0).text(), false);
                    MessageUtils.sendMessage(MessageTargetType.GENERIC, sendee.getChannel(), eb.build());
                } else {
                    System.out.println("No results");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
