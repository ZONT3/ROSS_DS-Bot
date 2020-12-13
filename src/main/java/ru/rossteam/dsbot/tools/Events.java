package ru.rossteam.dsbot.tools;

import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Events {

    private static final File committedFile = new File(Globals.DIR_DB, "events");

    public static void commitEventSet(HashSet<String> s) {
        Commons.commitObject(committedFile, s);
    }

    @SuppressWarnings("unchecked")
    public static HashSet<String> retrieveCommitted() {
        HashSet<String> o = (HashSet<String>) Commons.retrieveObject(committedFile);
        if (o == null) o = new HashSet<>();
        return o;
    }

    public static HashSet<Event> getEvents() throws IOException {
        final LocalDate now = LocalDate.now(ZoneId.of("GMT+3"));
        final Response curr = getResponse(now.getYear(), now.getMonthValue());
        final Response next = getResponse(curr.next_month.year, curr.next_month.month);

        HashSet<Event> res = new HashSet<>();
        for (Response r: Arrays.asList(curr, next)) {
            for (Element e: Jsoup.parseBodyFragment(r.events_side).getElementsByClass("mec-calendar-events-sec")) {
                final Elements title = e.getElementsByClass("mec-event-title");
                if (title.size() > 0) {
                    final Element a = title.first().getElementsByTag("a").first();
                    res.add(new Event(a.text(), a.attributes().get("href"), e.attributes().get("data-mec-cell")));
                }
            }
        }
        return res;
    }

    private static Response getResponse(int year, int month) throws IOException {
        return getResponse(year + "", month + "");
    }

    private static Response getResponse(String year, String month) throws IOException {
        final URL url = new URL("http://www.rossteam.ru/wp-admin/admin-ajax.php?action=mec_monthly_view_load_month&mec_year=" + year + "&mec_month=" + month + "&apply_sf_date=0&atts%5Bsf-options%5D%5Btile%5D%5Btext_search%5D%5Btype%5D=0&atts%5Bsf_status%5D=0&atts%5Bshow_past_events%5D=1&atts%5Bshow_only_past_events%5D=0&atts%5Bshow_only_ongoing_events%5D=0&atts%5B_edit_lock%5D=1602774171%3A1&atts%5B_edit_last%5D=1&atts%5Bid%5D=901");
        final String str = IOUtils.toString(url, StandardCharsets.UTF_8);
        return new Gson().fromJson(str, Response.class);
    }

    @Nullable
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

    public static class Event {
        private final String title;
        private final String link;
        private final LocalDate date;

        public Event(String title, String link, String date) {
            final Matcher m = Pattern.compile("(\\d{4})(\\d{2})(\\d{2})").matcher(date);
            if (!m.find()) throw new IllegalArgumentException("Date");
            this.title = title;
            this.link = link;
            this.date = LocalDate.of(
                    Integer.parseInt(m.group(1)),
                    Integer.parseInt(m.group(2)),
                    Integer.parseInt(m.group(3))
            );
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
    }
}
