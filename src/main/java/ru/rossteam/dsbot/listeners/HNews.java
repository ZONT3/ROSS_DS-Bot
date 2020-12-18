package ru.rossteam.dsbot.listeners;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import ru.rossteam.dsbot.tools.Commons;
import ru.rossteam.dsbot.tools.messages.Site;
import ru.zont.dsbot.core.ZDSBot;
import ru.zont.dsbot.core.handler.LStatusHandler;

import java.util.ArrayList;
import java.util.HashSet;

import static ru.rossteam.dsbot.tools.Site.*;

public class HNews extends LStatusHandler {
    public HNews(ZDSBot bot) {
        super(bot);
    }

    @Override
    public void prepare(ReadyEvent event) {

    }

    @Override
    public void update() throws Exception {
        final ArrayList<NewsEntry> newsEntries = parseNews();
        final HashSet<String> set = retrieveCommittedSet();
        for (NewsEntry entry: newsEntries) {
            if (set.contains(entry.getHref())) continue;
            if (entry.getDate() != null && entry.getDate().isBefore(Commons.DONT_POST_BEFORE)) continue;
            post(entry, set);
        }
    }

    private void post(NewsEntry entry, HashSet<String> set) {
        final MessageChannel channel = tryFindTChannel(Commons.getNewsChannelID());

        synchronized (set) {
            set.add(entry.getHref());
            commitSet(set);
        }
        channel.sendMessage(Site.newTopic(entry)).queue(null, e -> {
            synchronized (set) {
                set.remove(entry.getHref());
                commitSet(set);
            }
        });
    }

    @Override
    public long getPeriod() {
        return 10 * 60 * 1000;
    }
}
