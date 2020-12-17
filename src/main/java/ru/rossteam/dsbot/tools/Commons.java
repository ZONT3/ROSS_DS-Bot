package ru.rossteam.dsbot.tools;

import java.time.LocalDate;

import static ru.zont.dsbot.core.tools.Configs.getID;

public class Commons {

    public static final LocalDate DONT_POST_BEFORE = LocalDate.of(2020, 12, 10);

    public static String getStreamsChannelID() {
        return getID("channel_streams");
    }
    public static String getEventsChannelID() {
        return getID("channel_events");
    }
    public static String getNewsChannelID() {
        return getID("channel_news");
    }
    public static String getLogChannelID() {
        return getID("channel_log");
    }
    public static String getTSOnlineChannel() {
        return getID("channel_ts_online");
    }
}
