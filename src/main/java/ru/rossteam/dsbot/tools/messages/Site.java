package ru.rossteam.dsbot.tools.messages;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;
import ru.rossteam.dsbot.tools.Events;
import ru.zont.dsbot.core.tools.Messages;

import java.time.ZoneId;

import static ru.rossteam.dsbot.tools.Site.LINK_SITE;
import static ru.zont.dsbot.core.tools.Strings.STR;

public class Site {
    public static Message newTopic(ru.rossteam.dsbot.tools.Site.NewsEntry entry) {
        return Messages.pushEveryone(new EmbedBuilder()
                .setAuthor(STR.getString("shandler.site.news.title"), LINK_SITE)
                .setTitle(entry.getTitle(), entry.getHref())
                .setDescription(entry.getDesc())
                .appendDescription(String.format("\n\n[%s](%s)",
                        STR.getString("shandler.site.news.read"),
                        entry.getHref()))
                .setImage(entry.getThumb())
                .setColor(0x2A2AD0)
                .build());
    }

    public static Message newEvent(Events.Event event) {
        return eventEmbed(event, STR.getString("shandler.site.event.new"));
    }

    public static Message notifyEvent(Events.Event event) {
        return eventEmbed(event, STR.getString("shandler.site.event.day"));
    }

    @NotNull
    private static Message eventEmbed(Events.Event event, String title) {
        return Messages.pushEveryone(new EmbedBuilder()
                .setAuthor(title)
                .setTitle(event.getTitle(), event.getLink())
                .setTimestamp(event.getDate().atStartOfDay(ZoneId.of("GMT+3")).plusHours(12).toInstant())
                .setDescription(Events.tryRetrieveDescription(event))
                .setColor(0xA81010)
                .build());
    }

}
