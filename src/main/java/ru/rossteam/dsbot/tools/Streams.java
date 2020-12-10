package ru.rossteam.dsbot.tools;

import com.github.twitch4j.helix.TwitchHelix;
import com.github.twitch4j.helix.TwitchHelixBuilder;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchResult;
import org.jetbrains.annotations.NotNull;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Streams {
    public static final String REGEX_CHANNEL_ID = "[\\w-]+";
    public static final String LINK_CHANNEL_ID = "https://www.youtube.com/channel/";
    public static final String LINK_CHANNEL_NAME = "https://www.youtube.com/c/";
    public static final String LINK_VIDEO_ID = "https://www.youtube.com/watch?v=";
    public static final String ICON_YT = "https://upload.wikimedia.org/wikipedia/commons/thumb/0/09/YouTube_full-color_icon_%282017%29.svg/1024px-YouTube_full-color_icon_%282017%29.svg.png";
    public static final String ICON_TTV = "https://assets.help.twitch.tv/Glitch_Purple_RGB.png";

    private static final File watchlistFile = new File("db", "watchlist.bin");

    public static YouTube api;
    public static TwitchHelix helix;

    public static void setupYouTube() throws GeneralSecurityException, IOException {
        api = new YouTube.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(), null).setApplicationName("ross-ds-bot").build();
    }

    public static String getYTChannelID(String link) throws IOException {
        Pattern pattern = Pattern.compile("youtube\\.com/channel/(" + REGEX_CHANNEL_ID + ")");
        boolean matchesID = link.matches(REGEX_CHANNEL_ID);
        if (!matchesID) {
            Matcher matcher = pattern.matcher(link);
            if (matcher.find())
                return matcher.group(1);
        }

        Document document = null;
        if (matchesID) {
            for (String s: Stream.of(LINK_CHANNEL_ID + link, LINK_CHANNEL_NAME + link)
                    .collect(Collectors.toList())) {
                try {
                    document = Jsoup.connect(s).get();
                } catch (HttpStatusException ignored) {}
                if (document != null) break;
            }
        } else document = Jsoup.connect(link).get();
        if (document == null) throw new IOException("Cannot handle input");

        Elements l = document.root().getElementsByAttributeValue("rel", "canonical");
        for (Element element: l) {
            Matcher href = pattern.matcher(element.attributes().getIgnoreCase("href"));
            if (href.find())
                return href.group(1);
        }
        throw new RuntimeException("Cannot find canonical link");
    }

    public static List<SearchResult> getYTStreams(String channelId) throws IOException {
        if (api == null) throw new NullPointerException("API instance");

        return api.search().list("snippet")
                .setKey(Globals.GOOGLE_API)
                .setChannelId(channelId)
                .setType("video")
                .setEventType("live")
                .execute().getItems();
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored", "unchecked"})
    public static HashSet<String> retrieveWatchingList() {
        if (!watchlistFile.exists()) {
            watchlistFile.getParentFile().mkdirs();
            return new HashSet<>();
        }

        try (FileInputStream fis = new FileInputStream(watchlistFile);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            return (HashSet<String>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            watchlistFile.delete();
            throw new RuntimeException(e);
        }
    }

    public static String wlStatementToLink(String statement) {
        final Matcher matcher = Pattern.compile("(\\w+):(.*)").matcher(statement);
        if (!matcher.find()) throw new IllegalArgumentException("Not a WL statement");
        switch (matcher.group(1)) {
            case "yt": return getYTLink(matcher.group(2));
            case "ttv": return getTTVChannelLink(matcher.group(2));
            default: throw new IllegalArgumentException("Unknown type");
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void addToWatchingList(String link) {
        HashSet<String> set = retrieveWatchingList();
        set.add(link);

        if (!watchlistFile.exists())
            watchlistFile.getParentFile().mkdirs();

        try (FileOutputStream fos = new FileOutputStream(watchlistFile);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(set);
            oos.flush();
        } catch (IOException e) {
            watchlistFile.delete();
            throw new RuntimeException(e);
        }
    }

    public static String getYTThumbnail(String channelLink) throws IOException {
        return "";
    }

    public static String getYTLink(String videoID) {
        return LINK_VIDEO_ID + videoID;
    }

    @NotNull
    public static String getYTChannelLink(String channelID) {
        return LINK_CHANNEL_ID + channelID;
    }

    public static String getTTVChannelLink(String userid) {
        return "https://www.twitch.tv/" + userid;
    }

    public static void setupTwitch() {
        helix = TwitchHelixBuilder.builder()
                .withClientId(Globals.TWITCH_API_CLIENT_ID)
                .withClientSecret(Globals.TWITCH_API_SECRET)
                .build();
    }

    public static List<com.github.twitch4j.helix.domain.Stream> getTTVStreams(String userid) {
        if (helix == null) throw new NullPointerException("API instance");

        return helix.getStreams(null, null, null,
                null, null, null, null,
                Collections.singletonList(userid))
                .execute().getStreams();
    }

    public static String getTwitchChannel(String link) throws IOException {
        Pattern pattern = Pattern.compile("twitch\\.tv/(" + REGEX_CHANNEL_ID + ")");
        final Matcher matcher = pattern.matcher(link);
        if (matcher.find()) return matcher.group(1);
        else if (link.matches(REGEX_CHANNEL_ID)) return link;
        else throw new IOException("Cannot parse twitch userid");
    }
}
