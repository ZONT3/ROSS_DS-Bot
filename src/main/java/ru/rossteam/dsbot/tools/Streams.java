package ru.rossteam.dsbot.tools;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import org.jetbrains.annotations.NotNull;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Streams {
    public static final String REGEX_CHANNEL_ID = "[\\w-]+";

    private static final File watchlistFile = new File("db", "watchlist.bin");

    public static YouTube api;

    public static String getChannelID(String link) throws IOException {
        Pattern pattern = Pattern.compile("youtube\\.com/channel/(" + REGEX_CHANNEL_ID + ")");
        boolean matchesID = link.matches(REGEX_CHANNEL_ID);
        if (!matchesID) {
            Matcher matcher = pattern.matcher(link);
            if (matcher.find())
                return matcher.group(1);
        }

        Document document = null;
        if (matchesID) {
            for (String s: Stream.of("https://www.youtube.com/channel/" + link, "https://www.youtube.com/c/" + link)
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

    public static List<SearchResult> getStreams(String channelId) throws IOException {
        if (api == null) throw new NullPointerException("API instance");
        YouTube.Search.List search = api.search().list("snippet");

        return search.setKey(Globals.GOOGLE_API)
                .setChannelId(channelId)
                .setType("video")
                .setEventType("live")
                .execute().getItems();
    }

    public static void getYouTube() throws GeneralSecurityException, IOException {
        api = new YouTube.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(), null).setApplicationName("ross-ds-bot").build();
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

    public static String getYTLink(String videoID) {
        return "https://www.youtube.com/watch?v=" + videoID;
    }
}
