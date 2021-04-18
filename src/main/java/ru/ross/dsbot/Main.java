package ru.ross.dsbot;

import com.sun.net.httpserver.HttpServer;
import net.dv8tion.jda.api.requests.GatewayIntent;
import ru.ross.dsbot.commands.Media;
import ru.ross.dsbot.http.ReportHandler;
import ru.ross.dsbot.listeners.Greetings;
import ru.ross.dsbot.loops.LMedia;
import ru.ross.dsbot.loops.LTSClients;
import ru.ross.dsbot.loops.PEvents;
import ru.ross.dsbot.loops.PNews;
import ru.zont.dsbot2.ZDSBot;
import ru.zont.dsbot2.ZDSBotBuilder;
import ru.zont.dsbot2.commands.implement.Clear;
import ru.zont.dsbot2.commands.implement.Help;
import ru.zont.dsbot2.commands.implement.Say;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Main {
    private static String getVersion() {
        Properties properties = new Properties();
        try {
            properties.load(Main.class.getResourceAsStream("/version.properties"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return properties.getProperty("version", "UNKNOWN");
    }

    public static class Config extends ru.zont.dsbot2.Config {
        public final Entry role_checked = new Entry("0");
        public final Entry message_checkpoint = new Entry("0");
        public final Entry channel_streams = new Entry("0");
        public final Entry channel_video = new Entry("0");
        public final Entry channel_report = new Entry("0");
        public final Entry channel_ts = new Entry("0");
        public final Entry channel_ts_clients = new Entry("0");
        public final Entry channel_events = new Entry("723938067190382613", true);
        public final Entry channel_news = new Entry("526032797908074498", true);

        public Config() {
            super.prefix = new Entry("r.");
            super.channel_log = new Entry("814472065574109184", true);
            super.version = new Entry(getVersion(), true, false);
            super.version_str = new Entry("ROSS DS BOT v.%s", true);
            super.approved_guilds = new Entry("785203451797569626,620965426381324288", true);
        }
    }

    public static void main(String[] args) throws LoginException, InterruptedException, IOException {
        handleArgs(args);

        ZDSBotBuilder builder = new ZDSBotBuilder(args[0])
                .defaultSetup()
                .setConfig(new Config())
                .addCommands(Help.class,
                        Clear.class, Say.class,
                        Media.class
                )
                .addLoops(LMedia.class, LTSClients.class)
                .addParsers(PEvents.class, PNews.class)
                .setTechAdmins(List.of("375638389195669504", "331524458806247426"))
                .addListeners(new Greetings());

        builder.getJdaBuilder().enableIntents(GatewayIntent.GUILD_MESSAGE_REACTIONS);

        ZDSBot bot = builder.build();
        bot.getJda().awaitReady();

        setupServer(bot.getVoidGuildContext());
    }

    private static void handleArgs(String[] args) throws LoginException {
        if (args.length < 4) throw new LoginException("Not enough args");

        Globals.TWITCH_API_SECRET = args[1];
        Globals.GOOGLE_API = args[2];

        String[] split = args[3].split(";");
        if (split.length != 3) throw new LoginException("TSQuery Connect string is invalid");
        Globals.tsqHost  = split[0];
        Globals.tsqLogin = split[1];
        Globals.tsqPass  = split[2];
    }

    private static void setupServer(ZDSBot.GuildContext bot) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", 228), 0);
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
        server.createContext("/postForm", new ReportHandler(bot));
        server.setExecutor(threadPoolExecutor);
        server.start();
    }
}
