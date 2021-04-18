package ru.ross.dsbot.loops;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.ross.dsbot.Main;
import ru.ross.dsbot.tools.TEvents;
import ru.zont.dsbot2.ConfigCaster;
import ru.zont.dsbot2.ErrorReporter;
import ru.zont.dsbot2.ZDSBot;
import ru.zont.dsbot2.parser.ZParser;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public class PEvents extends ZParser<TEvents.Event> {

    public PEvents(ZDSBot.GuildContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return "ross-events";
    }

    @Override
    public List<TEvents.Event> retrieve() throws Throwable {
        final LocalDate now = LocalDate.now(ZoneId.of("GMT+3"));
        final TEvents.Response curr = TEvents.getResponse(now.getYear(), now.getMonthValue());
        final TEvents.Response next = TEvents.getResponse(curr.next_month.year, curr.next_month.month);

        ArrayList<TEvents.Event> res = new ArrayList<>();
        for (TEvents.Response r: Arrays.asList(curr, next)) {
            for (Element e: Jsoup.parseBodyFragment(r.events_side).getElementsByClass("mec-calendar-events-sec")) {
                final Elements title = e.getElementsByClass("mec-event-title");
                if (title.size() > 0) {
                    final Element a = title.first().getElementsByTag("a").first();

                    final String text = a.text();
                    final String link = a.attributes().get("href");
                    final String date = e.attributes().get("data-mec-cell");
                    final TEvents.Event event = new TEvents.Event(text, link, date);
                    if (event.getDate().isEqual(now) && Calendar.getInstance().get(Calendar.HOUR_OF_DAY) > 11)
                        res.add(new TEvents.Event(text, link, date, true));
                    else res.add(event);
                }
            }
        }
        return res;
    }

    @Override
    public List<TEvents.Event> onUpdate(List<TEvents.Event> newElements) {
        final ZDSBot.GuildContext context = getContext();
        Main.Config config = ConfigCaster.cast(context.getConfig());
        final TextChannel channel = context.getTChannel(config.channel_events.get());
        if (channel == null) {
            TEvents.LOG.error("Event channel not found");
            Thread.currentThread().interrupt();
            return Collections.emptyList();
        }

        List<TEvents.Event> res = new LinkedList<>();
        for (TEvents.Event event: newElements) {
            Message m;
            if (event.isCurrent())
                m = TEvents.Msg.notifyEvent(event);
            else m = TEvents.Msg.newEvent(event);

            try {
                channel.sendMessage(m).complete();
                res.add(event);
            } catch (Throwable e) {
                ErrorReporter.inst().reportError(context, getClass(), e);
            }
        }
        return res;
    }

    @Override
    public long nextUpdate() {
        return 60 * 1000;
    }

}
