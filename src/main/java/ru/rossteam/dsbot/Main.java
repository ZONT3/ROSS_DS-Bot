package ru.rossteam.dsbot;

import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import ru.rossteam.dsbot.tools.Globals;
import ru.zont.dsbot.core.ZDSBot;
import ru.zont.dsbot.core.tools.Configs;

import javax.security.auth.login.LoginException;
import java.util.Properties;

public class Main extends ListenerAdapter {

    public static void main(String[] args) throws LoginException, InterruptedException, IllegalArgumentException {
        Configs.setGlobalPropsDefaults(new Properties(){{
            setProperty("command_prefix", "r.");
            setProperty("channel_ts_online", "0");
            setProperty("channel_log", "0");
            setProperty("channel_streams", "0");
            setProperty("channel_news", "0");
            setProperty("channel_events", "0");
            setProperty("channel_ts", "0");
            setProperty("message_checkpoint", "0");
            setProperty("role_checked", "0");
            setProperty("TA_IDS", "331524458806247426");
        }});
        Configs.writeDefaultGlobalProps();

        handleArguments(args);

        ZDSBot bot = new ZDSBot(args[0], "1.1",
                "ru.rossteam.dsbot.command", "ru.rossteam.dsbot.listeners");
        bot.getJdaBuilder().enableIntents(GatewayIntent.GUILD_MESSAGE_REACTIONS);
        bot.create().awaitReady();
    }

    private static void handleArguments(String[] args) throws LoginException, IllegalArgumentException {
        if (args.length < 4) throw new LoginException("Too few arguments");

        String[] tsqArgs = args[1].split(";");
        if (tsqArgs.length < 3) throw new IllegalArgumentException("Too few TS Query arguments");
        Globals.tsq_host  = tsqArgs[0];
        Globals.tsq_login = tsqArgs[1];
        Globals.tsq_pass  = tsqArgs[2];

        Globals.GOOGLE_API = args[2];
        Globals.TWITCH_API_SECRET = args[3];
    }

}
