package ru.ross.dsbot.tools;

import com.google.gson.Gson;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ross.dsbot.Strings;
import ru.ross.dsbot.loops.PEvents;
import ru.zont.dsbot2.parser.ZParserElement;
import ru.zont.dsbot2.tools.ZDSBMessages;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TEvents {
    public static final Logger LOG = LoggerFactory.getLogger(PEvents.class);

    public static Response getResponse(int year, int month) throws IOException {
        return getResponse(year + "", month + "");
    }

    public static Response getResponse(String year, String month) throws IOException {
        final URL url = new URL("http://www.rossteam.ru/wp-admin/admin-ajax.php?action=mec_monthly_view_load_month&mec_year=" + year + "&mec_month=" + month + "&apply_sf_date=0&atts%5Bsf-options%5D%5Btile%5D%5Btext_search%5D%5Btype%5D=0&atts%5Bsf_status%5D=0&atts%5Bshow_past_events%5D=1&atts%5Bshow_only_past_events%5D=0&atts%5Bshow_only_ongoing_events%5D=0&atts%5B_edit_lock%5D=1602774171%3A1&atts%5B_edit_last%5D=1&atts%5Bid%5D=901");
        final String str = IOUtils.toString(url, StandardCharsets.UTF_8);
        return new Gson().fromJson(str, Response.class);
    }

    public static String tryRetrieveDescription(Event event) {
        try {
            final Document doc = Jsoup.connect(event.getLink()).get();
            return doc.getElementsByClass("mec-single-event-description").first().text();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static class Response {
        public String events_side;
        public NextMonth next_month;

        @Override
        public String toString() {
            return "Response{" +
                    "next_month=" + next_month +
                    '}';
        }
    }

    public static class NextMonth {
        public String label;
        public String id;
        public String year;
        public String month;

        @Override
        public String toString() {
            return "NextMonth{" +
                    "label='" + label + '\'' +
                    '}';
        }
    }

    public static class Event implements ZParserElement, Serializable {
        private final String title;
        private final String link;
        private final LocalDate date;
        private final String signature;

        public Event(String title, String link, String date) {
            this(title, link, date, false);
        }

        public Event(String title, String link, String date, boolean current) {
            final Matcher m = Pattern.compile("(\\d{4})(\\d{2})(\\d{2})").matcher(date);
            if (!m.find()) throw new IllegalArgumentException("Date");
            this.title = title;
            this.link = link;
            this.date = LocalDate.of(
                    Integer.parseInt(m.group(1)),
                    Integer.parseInt(m.group(2)),
                    Integer.parseInt(m.group(3))
            );
            this.signature = "%s::%s::{%s}".formatted(current ? "C" : "F", date, title);
        }

        public String getTitle() {
            return title;
        }

        public LocalDate getDate() {
            return date;
        }

        public String getLink() {
            return link;
        }

        @Override
        public String toString() {
            return "Event{" +
                    "title='" + title + '\'' +
                    ", link='" + link + '\'' +
                    ", date=" + date +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Event)) return false;
            Event event = (Event) o;
            return link.equals(event.link);
        }

        @Override
        public int hashCode() {
            return link.hashCode();
        }

        @Override
        public String getSignature() {
            return signature;
        }

        public boolean isCurrent() {
            return signature.startsWith("C::");
        }
    }

    public static class Msg {
        private static Message eventEmbed(Event event, String title) {
            return ZDSBMessages.pushEveryone(new EmbedBuilder()
                    .setAuthor(title)
                    .setTitle(event.getTitle(), event.getLink())
                    .setTimestamp(event.getDate().atStartOfDay(ZoneId.of("GMT+3")).plusHours(12).toInstant())
                    .setDescription(tryRetrieveDescription(event))
                    .setColor(0xA81010)
                    .build());
        }

        public static Message newEvent(Event event) {
            return eventEmbed(event, Strings.STR.getString("site.event.new"));
        }

        public static Message notifyEvent(Event event) {
            return eventEmbed(event, Strings.STR.getString("site.event.day"));
        }
    }
}
