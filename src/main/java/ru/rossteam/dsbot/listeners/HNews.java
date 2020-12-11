package ru.rossteam.dsbot.listeners;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import ru.rossteam.dsbot.tools.Configs;
import ru.rossteam.dsbot.tools.Messages;
import ru.rossteam.dsbot.tools.Site;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;

import static ru.rossteam.dsbot.tools.Site.*;

public class HNews extends LStatusHandler {
    @Override
    public void prepare(ReadyEvent event) {

    }

    @Override
    public void update() {
        try {
            final ArrayList<NewsEntry> newsEntries = parseNews();
            final HashSet<String> set = retrieveCommittedSet();
            for (NewsEntry entry: newsEntries) {
                if (set.contains(entry.getHref())) continue;
                if (!entry.getDate().isAfter(LocalDate.of(2020, 12, 1))) continue;

                set.add(entry.getHref());
                post(entry);
            }
            commitSet(set);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private void post(NewsEntry entry) {
        final MessageChannel channel = tryFindTChannel(Configs.getNewsChannelID());

        channel.sendMessage(Messages.Site.newTopic(entry)).queue(null, e -> {
            final HashSet<String> set = retrieveCommittedSet();
            set.remove(entry.getHref());
        });
    }

    @Override
    public long getPeriod() {
        return 600000;
    }
}
