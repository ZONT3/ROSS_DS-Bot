package ru.rossteam.dsbot.listeners;

import com.github.twitch4j.helix.domain.Stream;
import com.google.api.services.youtube.model.SearchResult;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import ru.rossteam.dsbot.listeners.LStatusHandler;
import ru.rossteam.dsbot.tools.Configs;
import ru.rossteam.dsbot.tools.Messages;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.function.Consumer;

import static ru.rossteam.dsbot.tools.Streams.*;

public class HStreams extends LStatusHandler {

    private static final Set<String> committed = Collections.synchronizedSet(new HashSet<>());

    @Override
    public void prepare(ReadyEvent event) {
        try {
            setupYouTube();
            setupTwitch();
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update() {
        try {
            for (String link: retrieveWatchingList()) {
                if (link.startsWith("yt:")) {
                    for (SearchResult result: getYTStreams(link.replaceFirst("yt:", "")))
                        if (!committed.contains(result.getId().getVideoId()))
                            commitYTStream(result);
                } else if (link.startsWith("ttv:")) {
                    for (Stream stream: getTTVStreams(link.replaceFirst("ttv:", "")))
                        if (!committed.contains(stream.getId()))
                            commitTTVStream(stream);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void commitTTVStream(Stream stream) {
        MessageChannel channel = tryFindTChannel(Configs.getStreamsChannelID());

        final String id = stream.getId();
        committed.add(id);
        channel.sendMessage(Messages.Streams.newTTVStream(stream)).queue(null, t -> {
            Messages.tryPrintError("Cannot commit a new stream notation",
                    Messages.describeException(t));
            committed.remove(id);
        });
    }

    private void commitYTStream(SearchResult result) {
        MessageChannel channel = tryFindTChannel(Configs.getStreamsChannelID());

        String id = result.getId().getVideoId();
        committed.add(id);
        channel.sendMessage(Messages.Streams.newYTStream(result)).queue(null, throwable -> {
            Messages.tryPrintError("Cannot commit a new stream notation",
                    Messages.describeException(throwable));
            committed.remove(id);
        });
    }

    @Override
    public long getPeriod() {
        return 120000;
    }
}
