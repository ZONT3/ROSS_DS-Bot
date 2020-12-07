package ru.rossteam.dsbot.tools;

import net.dv8tion.jda.api.JDA;
import ru.rossteam.dsbot.command.CommandAdapter;
import ru.rossteam.dsbot.listeners.LStatusHandler;

public class Globals {
    public static final String version = "1.0-SNAPSHOT";
    public static final String ZONT_MENTION = "<@331524458806247426>";

    public static JDA jda;
    public static CommandAdapter[] commandAdapters = null;
    public static LStatusHandler ytStreams;
    public static LStatusHandler usersMonitoring;

    public static String GOOGLE_API;

    public static String tsq_host;
    public static String tsq_login;
    public static String tsq_pass;
}
