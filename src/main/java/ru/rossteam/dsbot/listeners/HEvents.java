package ru.rossteam.dsbot.listeners;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.ReadyEvent;
import ru.rossteam.dsbot.tools.Commons;
import ru.rossteam.dsbot.tools.Configs;
import ru.rossteam.dsbot.tools.Messages;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.HashSet;

import static ru.rossteam.dsbot.tools.Events.*;

public class HEvents extends LStatusHandler {

    @Override
    public void prepare(ReadyEvent event) {

    }

    @Override
    public void update() throws Exception {
        final HashSet<Event> events = getEvents();
        final HashSet<String> set = retrieveCommitted();
        for (Event event: events) {
            if (event.getDate().isBefore(Commons.DONT_POST_BEFORE))
            if (!set.contains(event.getLink()))
                reportNewEvent(event, set);
            final LocalDate now = LocalDate.now(ZoneId.of("GMT+3"));
            if (event.getDate().isEqual(now) && Calendar.getInstance().get(Calendar.HOUR_OF_DAY) > 11)
                reportCurrEvent(event, set);
        }
    }

    private void reportEvent(String sign, HashSet<String> set, MessageEmbed embed) {
        final MessageChannel channel = tryFindTChannel(Configs.getEventsChannelID());

        synchronized (set) {
            set.add(sign);
            commitEventSet(set);
        }
        channel.sendMessage(embed).queue(null, e -> {
            synchronized (set) {
                set.remove(sign);
                commitEventSet(set);
            }
        });
    }

    private void reportCurrEvent(Event event, HashSet<String> set) {
        reportEvent("C:" + event.getLink(), set, Messages.Site.notifyEvent(event));
    }

    private void reportNewEvent(Event event, HashSet<String> set) {
        reportEvent(event.getLink(), set, Messages.Site.newEvent(event));
    }

    @Override
    public long getPeriod() {
        return 60 * 60 * 1000;
    }
}
