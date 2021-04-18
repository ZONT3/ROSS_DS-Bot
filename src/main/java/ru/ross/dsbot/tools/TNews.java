package ru.ross.dsbot.tools;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import ru.ross.dsbot.Strings;
import ru.zont.dsbot2.parser.ZParserElement;
import ru.zont.dsbot2.tools.ZDSBMessages;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class TNews {
    public static final String LINK_NEWS = "http://www.rossteam.ru/%D0%BD%D0%BE%D0%B2%D0%BE%D1%81%D1%82%D0%B8-%D1%81%D0%BA%D0%B2%D0%B0%D0%B4%D0%B0/";
    public static final String LINK_SITE = "http://www.rossteam.ru";

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

    public static class NewsEntry implements ZParserElement {
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

        @Override
        public String getSignature() {
            return href;
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

    public static class Msg {
        public static Message newTopic(NewsEntry entry) {
            return ZDSBMessages.pushEveryone(new EmbedBuilder()
                    .setAuthor(Strings.STR.getString("site.news.title"), LINK_SITE)
                    .setTitle(entry.getTitle(), entry.getHref())
                    .setDescription(entry.getDesc())
                    .appendDescription(String.format("\n\n[%s](%s)",
                            Strings.STR.getString("site.news.read"),
                            entry.getHref()))
                    .setFooter(DateTimeFormatter.ofPattern("dd.MM.yyyy").format(entry.getDate()))
                    .setImage(entry.getThumb())
                    .setColor(0x2A2AD0)
                    .build());
        }
    }
}
