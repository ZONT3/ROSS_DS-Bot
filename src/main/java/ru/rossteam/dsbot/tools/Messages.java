package ru.rossteam.dsbot.tools;

import com.github.twitch4j.helix.domain.Stream;
import com.google.api.services.youtube.model.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import ru.rossteam.dsbot.tools.Site.NewsEntry;

import java.awt.*;
import java.time.Instant;
import java.util.List;

import static ru.rossteam.dsbot.tools.Site.*;
import static ru.rossteam.dsbot.tools.Streams.*;
import static ru.rossteam.dsbot.tools.Strings.*;

public class Messages {
    public static MessageEmbed error(String title, String description) {
        return new EmbedBuilder()
                .setTitle(title)
                .setDescription(description)
                .setColor(Color.RED)
                .build();
    }

    public static void printError(MessageChannel channel, String title, String description) {
        channel.sendMessage(error(title, description)).queue();
    }

    public static void tryPrintError(String title, String description) {
        TextChannel channel = null;
        try {
            channel = Commons.tryFindTChannel(Configs.getLogChannelID(), Globals.jda);
        } catch (Exception ignored) { }
        if (channel == null) {
            for (Guild guild: Globals.jda.getGuilds()) {
                channel = guild.getSystemChannel();
                if (channel != null) break;
            }
            if (channel == null) {
                for (Guild guild: Globals.jda.getGuilds()) {
                    channel = guild.getDefaultChannel();
                    if (channel != null) break;
                }
            }
        }
        if (channel != null) channel.sendMessage(
                new EmbedBuilder(error(title, description))
                        .setFooter(Strings.STR.getString("err.unexpected.foot"))
                        .build()).queue();
    }

    public static MessageEmbed addTimestamp(MessageEmbed e) {
        return addTimestamp(new EmbedBuilder(e));
    }

    public static MessageEmbed addTimestamp(EmbedBuilder builder) {
        return builder.setTimestamp(Instant.now()).build();
    }

    public static void sendSplit(MessageChannel channel, List<EmbedBuilder> builders, boolean timestamp) {
        for (EmbedBuilder builder: builders)
            channel.sendMessage(
                    timestamp
                    ? Messages.addTimestamp(builder)
                    : builder.build()
            ).complete();
    }

    public static void sendSplit(MessageChannel channel, List<EmbedBuilder> builders) {
        sendSplit(channel, builders, false);
    }

    public static String describeException(Throwable e) {
        return describeException(null, e);
    }

    public static String describeException(Class<?> klass, Throwable e) {
        String localizedMessage = e.getLocalizedMessage();
        return (klass != null ? (klass.getSimpleName() + ": ") : "") + e.getClass().getSimpleName() + (localizedMessage == null ? "" : ": " + localizedMessage);
    }

    public static void addOK(Message msg) {
        msg.addReaction("\u2705").queue();
    }

    public static class Site {
        public static MessageEmbed newTopic(NewsEntry entry) {
            return new EmbedBuilder()
                    .setAuthor(STR.getString("shandler.site.news.title"), LINK_SITE)
                    .setTitle(entry.getTitle(), entry.getHref())
                    .setDescription(entry.getDesc())
                    .appendDescription(String.format("\n\n[%s](%s)",
                            STR.getString("shandler.site.news.read"),
                            entry.getHref()))
                    .setImage(entry.getThumb())
                    .setColor(0x2A2AD0)
                    .build();
        }
    }

    public static class Streams {
        public static MessageEmbed newYTStream(YTStream result) {
            try {
                final String videoID = result.getVideoID();
                final String channelID = result.getChannelID();
                final ChannelSnippet channel = getChannelSnippet(channelID).get(0).getSnippet();
                final VideoSnippet video = getVideoSnippet(videoID).get(0).getSnippet();

                final String channelTitle = channel.getTitle();
                final String title = video.getTitle();
                final String desc = trimSnippet(video.getDescription(), 64);
                final String thumb = video.getThumbnails().getDefault().getUrl();
                final String ytLink = getYTVideoLink(videoID);
                final String channelLink = getYTChannelLink(channelID);
                final String ytThumbnail = channel.getThumbnails().getDefault().getUrl();

                return new EmbedBuilder()
                        .setAuthor(channelTitle, channelLink, ytThumbnail)
                        .setTitle(STR.getString("shandler.streams.new.title"), ytLink)
                        .setDescription(String.format(STR.getString("shandler.streams.new.desc"),
                                title, desc))
                        .setThumbnail(ICON_YT)
                        .setImage(thumb)
                        .setColor(0xFF0000)
                        .build();

            } catch (Throwable e) {
                throw new RuntimeException("Error in getting info", e);
            }
        }

        public static MessageEmbed newTTVStream(Stream stream) {

            try {
                final String title = stream.getTitle();
                final String name = stream.getUserName();
                final String thumb = stream.getThumbnailUrl(480, 270);
                final String link = getTTVChannelLink(name);
                final String ttvThumbnail = getTTVThumbnail(name);

                return new EmbedBuilder()
                        .setAuthor(name, link, ttvThumbnail)
                        .setTitle(STR.getString("shandler.streams.new.title"), link)
                        .setDescription(String.format(STR.getString("shandler.streams.new.desc"), title, ""))
                        .setImage(thumb)
                        .setThumbnail(ICON_TTV)
                        .setColor(0x6441A4)
                        .build();
            } catch (Throwable e) {
                throw new RuntimeException("Error in getting info", e);
            }
        }
    }
}
