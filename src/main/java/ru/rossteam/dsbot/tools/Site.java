package ru.rossteam.dsbot.tools;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import ru.zont.dsbot.core.tools.Tools;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;

public class Site {
    public static final String LINK_NEWS = "http://www.rossteam.ru/%D0%BD%D0%BE%D0%B2%D0%BE%D1%81%D1%82%D0%B8-%D1%81%D0%BA%D0%B2%D0%B0%D0%B4%D0%B0/";
    public static final String LINK_SITE = "http://www.rossteam.ru";

    private static final File committedSetFile = new File(Globals.DIR_DB, "news.bin");


    @SuppressWarnings("unchecked")
    public static HashSet<String> retrieveCommittedSet() {
        final Object o = Tools.retrieveObject(committedSetFile);
        return o != null ? (HashSet<String>) o : new HashSet<>();
    }

    public static void commitSet(HashSet<String> set) {
        Tools.commitObject(committedSetFile, set);
    }

    public static ArrayList<NewsEntry> parseNews() throws IOException {
        ArrayList<NewsEntry> news = new ArrayList<>();
        final Element root = Jsoup.connect(LINK_NEWS).get().root();
        try {
            for (Element li: root.getElementById("news-page").getElementsByTag("li")) {
                try {
                    final String href = li.getElementsByClass("read-more-btn").first()
                            .attributes().get("href");
                    final String title = li.getElementsByTag("h2").first().getElementsByTag("a")
                            .first().text();
                    final String dateText = li.getElementsByTag("span").first().text();
                    final LocalDate date = dateText.isEmpty() ? null : LocalDate.parse(
                            dateText,
                            DateTimeFormatter.ofPattern("dd.MM.yyyy"));

                    final NewsEntry entry = new NewsEntry(href, date, title);
                    news.add(entry);

                    entry.setThumb(li.getElementsByTag("img").first().attributes().get("src"));
                    entry.setDesc(li.getElementsByTag("p").first().text());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return news;
        } catch (Exception e) {
            throw new ParsingError(e);
        }
    }

    public static class NewsEntry {
        private final String href;
        private final LocalDate date;
        private final String title;
        private String thumb;
        private String desc;

        public NewsEntry(String href, LocalDate date, String title) {
            this.href = href;
            this.date = date;
            this.title = title;
        }

        public String getHref() {
            return href;
        }

        public LocalDate getDate() {
            return date;
        }

        public String getTitle() {
            return title;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public String getThumb() {
            return thumb;
        }

        public void setThumb(String thumb) {
            this.thumb = thumb;
        }
    }

    public static class ParsingError extends RuntimeException {
        public ParsingError(String s) {
            super(s);
        }

        public ParsingError(Throwable e) {
            super(e);
        }
    }
}
