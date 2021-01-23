package ru.rossteam.dsbot;

import com.sun.net.httpserver.HttpServer;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import ru.rossteam.dsbot.command.*;
import ru.rossteam.dsbot.http.FormsHandler;
import ru.rossteam.dsbot.listeners.*;
import ru.rossteam.dsbot.tools.Globals;
import ru.zont.dsbot.core.ZDSBot;
import ru.zont.dsbot.core.commands.CommandAdapter;
import ru.zont.dsbot.core.handler.LStatusHandler;
import ru.zont.dsbot.core.tools.Configs;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static ru.rossteam.dsbot.tools.TV.setupTwitch;
import static ru.rossteam.dsbot.tools.TV.setupYouTube;

public class Main extends ListenerAdapter {

    public static void main(String[] args) throws Exception {
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

        setupYouTube();
        setupTwitch();

        ZDSBot bot = new ZDSBot(args[0], "1.1",
                "", "");
        bot.getJdaBuilder().enableIntents(GatewayIntent.GUILD_MESSAGE_REACTIONS);
        registerAdapters(bot);
        bot.create().awaitReady();

        setupServer(bot);
    }

    private static void setupServer(ZDSBot bot) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", 228), 0);
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
        server.createContext("/postForm", new FormsHandler(bot));
        server.setExecutor(threadPoolExecutor);
        server.start();
    }

    private static void registerAdapters(ZDSBot bot) {
        // Нет, я не дегенерад, просто Reflections перестали работать какого-то хуя. Помогите мне.
        bot.commandAdapters = new CommandAdapter[]{
                new Streams(bot),
                new Videos(bot),
                new Config(bot),
                new Help(bot),
                new Ping(bot),
                new Say(bot),
                new Clear(bot)
        };
        bot.statusHandlers = new LStatusHandler[0];
        bot.getJdaBuilder().addEventListeners((Object[]) bot.statusHandlers);
        bot.getJdaBuilder().addEventListeners(
                new HYTVideos(bot),
                new HClientsTS(bot),
                new HEvents(bot),
                new HNews(bot),
                new HStreams(bot),
                new HCheckpoint(bot)
                );
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
