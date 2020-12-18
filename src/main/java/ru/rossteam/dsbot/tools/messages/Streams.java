package ru.rossteam.dsbot.tools.messages;

import com.github.twitch4j.helix.domain.Stream;
import com.google.api.services.youtube.model.ChannelSnippet;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.SearchResultSnippet;
import com.google.api.services.youtube.model.VideoSnippet;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import ru.rossteam.dsbot.tools.TV;
import ru.zont.dsbot.core.tools.Messages;

import static ru.rossteam.dsbot.tools.TV.*;
import static ru.zont.dsbot.core.tools.Strings.STR;
import static ru.zont.dsbot.core.tools.Strings.trimSnippet;


public class Streams {
    public static Message newYTStream(TV.YTStream result) {
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

            return Messages.pushEveryone(new EmbedBuilder()
                    .setAuthor(channelTitle, channelLink, ytThumbnail)
                    .setTitle(STR.getString("shandler.streams.new.title"), ytLink)
                    .setDescription(String.format(STR.getString("shandler.streams.new.desc"),
                            title, desc))
                    .setThumbnail(ICON_YT)
                    .setImage(thumb)
                    .setColor(0xFF0000)
                    .build());

        } catch (Throwable e) {
            throw new RuntimeException("Error in getting info", e);
        }
    }

    public static Message newYTVideo(SearchResult result) {
        try {
            final String videoID = result.getId().getVideoId();
            final String channelID = result.getSnippet().getChannelId();
            final ChannelSnippet channel = getChannelSnippet(channelID).get(0).getSnippet();
            final SearchResultSnippet video = result.getSnippet();

            final String channelTitle = channel.getTitle();
            final String title = video.getTitle();
            final String desc = trimSnippet(video.getDescription(), 64);
            final String thumb = video.getThumbnails().getDefault().getUrl();
            final String ytLink = getYTVideoLink(videoID);
            final String channelLink = getYTChannelLink(channelID);
            final String ytThumbnail = channel.getThumbnails().getDefault().getUrl();

            return Messages.pushEveryone(new EmbedBuilder()
                    .setAuthor(channelTitle, channelLink, ytThumbnail)
                    .setTitle(STR.getString("shandler.videos.new.title"), ytLink)
                    .setDescription(String.format(STR.getString("shandler.videos.new.desc"),
                            title, desc))
                    .setThumbnail(ICON_YT)
                    .setImage(thumb)
                    .setColor(0xFF0000)
                    .build());

        } catch (Throwable e) {
            throw new RuntimeException("Error in getting info", e);
        }
    }

    public static Message newTTVStream(Stream stream) {

        try {
            final String title = stream.getTitle();
            final String name = stream.getUserName();
            final String thumb = stream.getThumbnailUrl(480, 270);
            final String link = getTTVChannelLink(name);
            final String ttvThumbnail = getTTVThumbnail(name);

            return Messages.pushEveryone(new EmbedBuilder()
                    .setAuthor(name, link, ttvThumbnail)
                    .setTitle(STR.getString("shandler.streams.new.title"), link)
                    .setDescription(String.format(STR.getString("shandler.streams.new.desc"), title, ""))
                    .setImage(thumb)
                    .setThumbnail(ICON_TTV)
                    .setColor(0x6441A4)
                    .build());
        } catch (Throwable e) {
            throw new RuntimeException("Error in getting info", e);
        }
    }
}
