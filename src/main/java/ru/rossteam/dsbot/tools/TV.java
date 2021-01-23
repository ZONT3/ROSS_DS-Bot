package ru.rossteam.dsbot.tools;

import com.github.twitch4j.helix.TwitchHelixBuilder;
import com.github.twitch4j.helix.domain.User;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.Video;
import javafx.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.zont.dsbot.core.commands.CommandAdapter;
import ru.zont.dsbot.core.commands.Commands;
import ru.zont.dsbot.core.commands.DescribedException;
import ru.zont.dsbot.core.tools.Messages;
import ru.zont.dsbot.core.tools.Tools;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.zont.dsbot.core.tools.Strings.STR;

public class TV {
    public static final String REGEX_CHANNEL_ID = "[\\w-]+";
    public static final String LINK_CHANNEL_ID = "https://www.youtube.com/channel/";
    public static final String LINK_CHANNEL_NAME = "https://www.youtube.com/c/";
    public static final String LINK_VIDEO_ID = "https://www.youtube.com/watch?v=";
    public static final String ICON_YT = "https://upload.wikimedia.org/wikipedia/commons/thumb/0/09/YouTube_full-color_icon_%282017%29.svg/1024px-YouTube_full-color_icon_%282017%29.svg.png";
    public static final String ICON_TTV = "https://assets.help.twitch.tv/Glitch_Purple_RGB.png";

    private static final File watchlistFile = new File(Globals.DIR_DB, "watchlist.bin");
    public static File ytWatchlist = new File(Globals.DIR_DB, "wl_videos.bin");

    public static void setupYouTube() throws GeneralSecurityException, IOException {
        Globals.api = new YouTube.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(), null).setApplicationName("ross-ds-bot").build();
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
                } catch (HttpStatusException ignored) {
                }
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

    public static YTStream getYTStream(String channelId) {
        try {
            final String canonical = Jsoup.connect(LINK_CHANNEL_ID + channelId + "/live").get().body()
                    .getElementsByAttributeValue("rel", "canonical").first().attributes().get("href");
            if (!canonical.contains("watch?")) return null;
            final YTStream stream = new YTStream(channelId, canonical);
            final List<Video> snippet = Globals.api.videos().list("snippet").setId(stream.getVideoID()).execute().getItems();
            if (snippet.size() < 1) return null;
            if (snippet.get(0).getSnippet().getLiveBroadcastContent().equals("live")) return null;
            return stream;
        } catch (Throwable ignored) {
            return null;
        }
    }

    @SuppressWarnings({"unchecked"})
    public static HashSet<String> retrieveWatchingList() {
        final Object o = Tools.retrieveObject(watchlistFile);
        return o != null ? (HashSet<String>) o : new HashSet<>();
    }

    private static void commitWL(HashSet<String> set) {
        Tools.commitObject(watchlistFile, set);
    }

    public static Pair<String, String> wlStatementToLink(String statement) {
        final Matcher matcher = Pattern.compile("(\\w+):(.*)").matcher(statement);
        if (!matcher.find()) throw new IllegalArgumentException("Not a WL statement");
        final String platform = matcher.group(1);
        final String identifier = matcher.group(2);

        switch (platform) {
            case "yt":
                return new Pair<>(getYTName(identifier), getYTChannelLink(identifier));
            case "ttv":
                return new Pair<>(getTTVName(identifier), getTTVChannelLink(identifier));
            default:
                throw new IllegalArgumentException("Unknown type");
        }
    }

    public static String getTTVName(String username) {
        final List<User> users = getUsersSnippet(username);
        if (users.size() < 1) throw new IllegalArgumentException("Not found such user");
        return users.get(0).getDisplayName();
    }

    public static String getYTName(String id) {
        final List<Channel> snippet = getChannelSnippet(id);
        if (snippet.size() < 1) return "???"; /*throw new IllegalArgumentException("Not found such user");*/
        return snippet.get(0).getSnippet().getTitle();
    }

    public static String getTTVThumbnail(String username) {
        final List<User> users = getUsersSnippet(username);
        if (users.size() < 1) throw new IllegalArgumentException("Not found such user");
        return users.get(0).getProfileImageUrl();
    }

    private static List<User> getUsersSnippet(String username) {
        if (Globals.helix == null) throw new NullPointerException("API instance");
        return Globals.helix.getUsers(null, null, Collections.singletonList(username)).execute().getUsers();
    }

    public static List<Channel> getChannelSnippet(String id) {
        if (Globals.api == null) throw new NullPointerException("API instance");

        final List<Channel> snippet;
        try {
            snippet = Globals.api.channels().list("snippet")
                    .setKey(Globals.GOOGLE_API)
                    .setId(id)
                    .execute().getItems();
        } catch (IOException e) {
//            LOG.d("Cannot fetch channels");
            return new ArrayList<>();
        }
        return snippet;
    }

    public static List<Video> getVideoSnippet(String id) {
        if (Globals.api == null) throw new NullPointerException("API instance");

        final List<Video> snippet;
        try {
            snippet = Globals.api.videos().list("snippet")
                    .setKey(Globals.GOOGLE_API)
                    .setId(id)
                    .execute().getItems();
        } catch (IOException e) {
//            LOG.d("Cannot fetch channels");
            return new ArrayList<>();
        }
        return snippet;
    }

    public static void addToWatchingStreamsList(String link) {
        HashSet<String> set = retrieveWatchingList();
        set.add(link);
        commitWL(set);
    }

    public static void removeFromWatchingStreamsList(String res) {
        HashSet<String> set = retrieveWatchingList();
        set.removeIf(s -> s.equalsIgnoreCase(res));
        commitWL(set);
    }

    public static String getYTVideoLink(String videoID) {
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
        Globals.helix = TwitchHelixBuilder.builder()
                .withClientId(Globals.TWITCH_API_CLIENT_ID)
                .withClientSecret(Globals.TWITCH_API_SECRET)
                .build();
    }

    public static List<com.github.twitch4j.helix.domain.Stream> getTTVStreams(String userid) {
        if (Globals.helix == null) throw new NullPointerException("API instance");

        return Globals.helix.getStreams(null, null, null,
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

    @NotNull
    public static String getChannelReference(String reference) {
        String channel;
        String service = "yt:";
        try {
            channel = getYTChannelID(reference);
        } catch (Exception e) {
            try {
                service = "ttv:";
                channel = getTwitchChannel(reference);
            } catch (Throwable ee) {
                e.printStackTrace();
                ee.printStackTrace();
                throw new DescribedException(
                        STR.getString("comm.streams.err.add.title"),
                        String.format(STR.getString("comm.streams.err.add"),
                                Messages.describeException(e)));
            }
        }
        return service + channel;
    }

    public static String getValidReference(Commands.Input input) {
        ArrayList<String> args = input.getArgs();
        if (args.size() < 2) throw new CommandAdapter.UserInvalidArgumentException(STR.getString("err.insufficient_args"));
        final String reference = args.get(1);

        return getChannelReference(reference);
    }

    @SuppressWarnings("unchecked")
    public static ArrayList<String> retrieveYTWatchingList() {
        ArrayList<String> wl = (ArrayList<String>) Tools.retrieveObject(ytWatchlist);
        if (wl == null) return new ArrayList<>();
        return wl;
    }

    public static class YTStream {

        private final String channelID;
        private final String link;

        public YTStream(String channelID, String link) {
            this.channelID = channelID;
            this.link = link;
        }

        public String getChannelID() {
            return channelID;
        }

        public String getLink() {
            return link;
        }

        public String getVideoID() {
            final Matcher matcher = Pattern.compile("v=(" + REGEX_CHANNEL_ID + ")").matcher(link);
            if (!matcher.find()) return "";
            return matcher.group(1);
        }

        @Override
        public String toString() {
            return "YTStream{" +
                    "channelID='" + channelID + '\'' +
                    ", link='" + link + '\'' +
                    '}';
        }
    }
}
