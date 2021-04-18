package ru.ross.dsbot.loops;

import net.dv8tion.jda.api.entities.TextChannel;
import ru.ross.dsbot.Main;
import ru.ross.dsbot.tools.TEvents;
import ru.zont.dsbot2.ConfigCaster;
import ru.zont.dsbot2.ZDSBot;
import ru.zont.dsbot2.parser.ZParser;

import java.util.Collections;
import java.util.List;

import static ru.ross.dsbot.tools.TNews.*;

public class PNews extends ZParser<NewsEntry> {
    public PNews(ZDSBot.GuildContext context) {
        super(context);
    }

    @Override
    public String getName() {
        return "ross-news";
    }

    @Override
    public List<NewsEntry> retrieve() throws Throwable {
        return parseNews();
    }

    @Override
    public List<NewsEntry> onUpdate(List<NewsEntry> newElements) throws Throwable {
        final ZDSBot.GuildContext context = getContext();
        Main.Config config = ConfigCaster.cast(context.getConfig());
        final TextChannel channel = context.getTChannel(config.channel_news.get());
        if (channel == null) {
            TEvents.LOG.error("News channel not found");
            Thread.currentThread().interrupt();
            return Collections.emptyList();
        }

        return handleNewElements(newElements, entry -> channel.sendMessage(Msg.newTopic(entry)).complete());
    }

    @Override
    public long nextUpdate() {
        return 30 * 60 * 1000;
    }
}
