package ru.ross.dsbot.loops;

import com.github.theholywaffle.teamspeak3.TS3Api;
import com.github.theholywaffle.teamspeak3.TS3Config;
import com.github.theholywaffle.teamspeak3.TS3Query;
import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ross.dsbot.Globals;
import ru.ross.dsbot.Main;
import ru.zont.dsbot2.ConfigCaster;
import ru.zont.dsbot2.ErrorReporter;
import ru.zont.dsbot2.ZDSBot;
import ru.zont.dsbot2.loops.LoopAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.ross.dsbot.Strings.*;

public class LTSClients extends LoopAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(LTSClients.class);

    public static final String DS_BOT_NAME = "DS Bot";
    private TS3Query query;
    private TS3Api api;

    private Main.Config config;
    private Message tsStatus;
    private int lastCount;

    public LTSClients(ZDSBot.GuildContext context) {
        super(context);
    }

    @Override
    public void prepare() {
        this.config = ConfigCaster.cast(getContext().getConfig());
        for (String s: List.of(Globals.tsqHost, Globals.tsqLogin, Globals.tsqPass))
            if (s == null) throw new NullPointerException("One of setup fields");

        createQuery();
        prepareMessage();
    }

    private void createQuery() {
        TS3Config config = new TS3Config();
        config.setHost(Globals.tsqHost);
        query = new TS3Query(config);
        query.connect();
        api = query.getApi();
        api.login(Globals.tsqLogin, Globals.tsqPass);
        api.selectVirtualServerById(1, DS_BOT_NAME);
    }

    private void prepareMessage() {
        final TextChannel channel = getContext().getTChannel(config.channel_ts.get());
        for (Message message: channel.getHistory().retrievePast(50).complete()) {
            final List<MessageEmbed> embeds = message.getEmbeds();
            if (embeds.size() < 1) continue;
            final String title = embeds.get(0).getTitle();
            if (title != null && title.equals(STR.getString("ts_status.title")))
                tsStatus = message;
        }
        if (tsStatus != null) return;

        tsStatus = channel.sendMessage(
                new EmbedBuilder()
                        .setTitle(STR.getString("ts_status.title"))
                        .build()).complete();
    }

    @Override
    public void loop() throws Throwable {
        List<Client> clients;
        try {
            clients = api.getClients();
        } catch (Exception e) {
            ErrorReporter.printStackTrace(e, getClass());
            LOG.warn("Failed to get clients, retrying...");
            try {
                try {
                    api.logout();
                    query.exit();
                } catch (Exception ignored) {
                    LOG.warn("Cannot disconnect previous query");
                }
                createQuery();
                clients = api.getClients();
            } catch (Exception exception) {
                LOG.error("Cannot reconnect!");
                throw exception;
            }
        }

        try {
            updTSStatus(clients);
        } catch (Exception e) {
            ErrorReporter.inst().reportError(getContext(), getClass(), e);
        }
        try {
            updClients(clients);
        } catch (Exception e) {
            ErrorReporter.inst().reportError(getContext(), getClass(), e);
        }
    }

    private void updClients(List<Client> clients) {
        GuildChannel channel = getContext().getChannel(config.channel_ts_clients.get());

        int count = getClientCount(clients);
        if (count == lastCount) return;

        channel.getManager().setName(String.format(STR.getString("ts_clients"), count))
                .complete();
        lastCount = count;
    }

    private int getClientCount(List<Client> clients) {
        int i = 0;
        for (Client client: clients)
            if (!client.isServerQueryClient()) i++;
        return i;
    }

    private void updTSStatus(List<Client> clients) {
        HashMap<Integer, ArrayList<Client>> channels = new HashMap<>();
        for (Client client: clients) {
            if (client.isServerQueryClient()) continue;
            final ArrayList<Client> list = channels.getOrDefault(client.getChannelId(), new ArrayList<>());
            list.add(client);
            channels.put(client.getChannelId(), list);
        }

        final EmbedBuilder builder = new EmbedBuilder()
                .setColor(0x00c8ff)
                .setFooter(STR.getString("ts_status.footer"), "https://icons.iconarchive.com/icons/papirus-team/papirus-apps/256/teamspeak-3-icon.png")
                .setTitle(STR.getString("ts_status.title"));
        if (channels.isEmpty()) builder.setDescription(STR.getString("ts_status.no_one"));
        for (Map.Entry<Integer, ArrayList<Client>> e: channels.entrySet()) {
            StringBuilder sb = new StringBuilder();
            for (Client client: e.getValue())
                sb.append(client.getNickname()).append('\n');
            builder.addField(api.getChannelInfo(e.getKey()).getName(), sb.toString(), false);
        }

        tsStatus.editMessage(builder.build()).queue();
    }

    @Override
    public long getPeriod() {
        return 15 * 1000;
    }

    @Override
    public boolean runInGlobal() {
        return true;
    }

    @Override
    public boolean runInLocal() {
        return false;
    }
}
