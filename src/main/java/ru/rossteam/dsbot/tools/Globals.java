package ru.rossteam.dsbot.tools;

import ru.rossteam.dsbot.command.CommandAdapter;
import ru.rossteam.dsbot.listeners.LStatusHandler;

public class Globals {
    public static final String version = "1.0-SNAPSHOT";

    public static CommandAdapter[] commandAdapters = null;

    public static String ZONT_MENTION = "<@331524458806247426>";

    public static LStatusHandler usersMonitoring;

    public static String tsq_host;
    public static String tsq_login;
    public static String tsq_pass;
}
