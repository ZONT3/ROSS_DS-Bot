package ru.rossteam.dsbot.listeners.streams;

import com.google.api.services.youtube.model.SearchResult;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import ru.rossteam.dsbot.listeners.LStatusHandler;
import ru.rossteam.dsbot.tools.Configs;
import ru.rossteam.dsbot.tools.Messages;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;

import static ru.rossteam.dsbot.tools.Streams.*;

public class HYTStreams extends LStatusHandler {

    private static final Set<String> committed = Collections.synchronizedSet(new HashSet<>());

    @Override
    public void prepare(ReadyEvent event) {
        try {
            getYouTube();
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update() {
        try {
            for (String link: retrieveWatchingList())
                if (link.contains("youtube.com"))
                    for (SearchResult result: getStreams(getChannelID(link)))
                        if (!committed.contains(result.getId().getVideoId()))
                            commitStream(result);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void commitStream(SearchResult result) {
        MessageChannel channel = tryFindTChannel(Configs.getStreamsChannelID());

        String id = result.getId().getVideoId();
        committed.add(id);
        channel.sendMessage(Messages.Streams.newStream(result)).queue(null, throwable -> {
            Messages.trySendError("Cannot commit a new stream notation", Messages.describeException(throwable));
            committed.remove(id);
        });
    }

    @Override
    public long getPeriod() {
        return 120000;
    }
}
