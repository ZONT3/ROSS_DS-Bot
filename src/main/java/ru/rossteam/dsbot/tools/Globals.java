package ru.rossteam.dsbot.tools;

import com.github.twitch4j.helix.TwitchHelix;
import com.google.api.services.youtube.YouTube;

public class Globals {
    public static final String version = "1.2";
    public static final String DIR_DB = "db";

    public static String GOOGLE_API;
    public static final String TWITCH_API_CLIENT_ID = "5n48e9ffucc1movvu6who9n4wq63u3";
    public static String TWITCH_API_SECRET;

    public static YouTube api;
    public static TwitchHelix helix;

    public static String tsq_host;
    public static String tsq_login;
    public static String tsq_pass;
}
