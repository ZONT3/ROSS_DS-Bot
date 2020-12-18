package ru.rossteam.dsbot.listeners;

import com.google.api.services.youtube.model.SearchResult;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import ru.rossteam.dsbot.tools.Commons;
import ru.rossteam.dsbot.tools.Globals;
import ru.rossteam.dsbot.tools.messages.Streams;
import ru.zont.dsbot.core.ZDSBot;
import ru.zont.dsbot.core.handler.LStatusHandler;
import ru.zont.dsbot.core.tools.Tools;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static ru.rossteam.dsbot.tools.TV.retrieveYTWatchingList;


@SuppressWarnings("unchecked")
public class HYTVideos extends LStatusHandler {
    private static File committedFile = new File(Globals.DIR_DB, "c_videos.bin");

    public HYTVideos(ZDSBot bot) {
        super(bot);
    }


    @Override
    public void prepare(ReadyEvent event) throws Exception { }

    @Override
    public void update() throws Exception {
        final HashSet<String> set = retrieveCommitted();
        for (String cr: retrieveYTWatchingList()) {
            for (SearchResult video: getVideos(cr.replaceFirst("yt:", ""))) {
                if (LocalDateTime.ofEpochSecond(
                        video.getSnippet().getPublishedAt().getValue() / 1000,
                        0,
                        ZoneOffset.ofHours(3))
                        .isBefore(Commons.DONT_POST_BEFORE.atStartOfDay().plusHours(12)))
                    continue;
                if (!set.contains(video.getId().getVideoId()))
                    post(video.getId().getVideoId(), video, set);
            }
        }
    }

    private HashSet<String> retrieveCommitted() {
        HashSet<String> wl = (HashSet<String>) Tools.retrieveObject(committedFile);
        if (wl == null) return new HashSet<>();
        return wl;
    }


    private void commit(HashSet<String> set) {
        Tools.commitObject(committedFile, set);
    }


    private void post(String sign, SearchResult video, HashSet<String> set) {
        final MessageChannel channel = tryFindTChannel(Commons.getStreamsChannelID());

        synchronized (set) {
            set.add(sign);
            commit(set);
        }

        channel.sendMessage(Streams.newYTVideo(video)).queue(null, e -> {
            synchronized (set) {
                set.remove(sign);
                commit(set);
            }
        });
    }

    private List<SearchResult> getVideos(String channelID) {
        try {
            return Globals.api.search()
                    .list("snippet").setChannelId(channelID)
                    .setKey(Globals.GOOGLE_API)
                    .setMaxResults(10L).setOrder("date")
                    .setType("video")
                    .execute().getItems();
        } catch (IOException e) {
            System.err.println("Looks like we has ran out of quota");
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public long getPeriod() {
        return 60 * 60 * 1000;
    }
}
