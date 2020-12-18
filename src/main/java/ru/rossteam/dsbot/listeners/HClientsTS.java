package ru.rossteam.dsbot.listeners;

import com.github.theholywaffle.teamspeak3.TS3Api;
import com.github.theholywaffle.teamspeak3.TS3Config;
import com.github.theholywaffle.teamspeak3.TS3Query;
import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ShutdownEvent;
import org.jetbrains.annotations.NotNull;
import ru.rossteam.dsbot.tools.Commons;
import ru.rossteam.dsbot.tools.Globals;
import ru.zont.dsbot.core.ZDSBot;
import ru.zont.dsbot.core.handler.LStatusHandler;
import ru.zont.dsbot.core.tools.Tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.zont.dsbot.core.tools.Strings.STR;

public class HClientsTS extends LStatusHandler {
    public static final String DS_BOT_NAME = "DS Bot";
    private TS3Api api;
    private TS3Query query;
    private boolean loginSuccess = false;

    private Message tsStatus;

    int lastCount = Integer.MIN_VALUE;

    public HClientsTS(ZDSBot bot) {
        super(bot);
    }

    @Override
    public void prepare(ReadyEvent event) {
        for (String s: Stream.of(Globals.tsq_host, Globals.tsq_login, Globals.tsq_pass).collect(Collectors.toList()))
            if (s == null) throw new NullPointerException("One of (probably all) setup fields");

        TS3Config config = new TS3Config();
        config.setHost(Globals.tsq_host);
        query = new TS3Query(config);
        query.connect();
        api = query.getApi();
        api.login(Globals.tsq_login, Globals.tsq_pass);
        api.selectVirtualServerById(1, DS_BOT_NAME);
        loginSuccess = true;

        prepareMessage();
    }

    private void prepareMessage() {
        final TextChannel channel = Tools.tryFindTChannel(Commons.getTSChannelID(), getJda());
        for (Message message: channel.getHistory().retrievePast(50).complete()) {
            final List<MessageEmbed> embeds = message.getEmbeds();
            if (embeds.size() < 1) continue;
            final String title = embeds.get(0).getTitle();
            if (title != null && title.equals(STR.getString("shandler.ts_status.title")))
                tsStatus = message;
        }
        if (tsStatus != null) return;

        tsStatus = channel.sendMessage(
                new EmbedBuilder()
                .setTitle(STR.getString("shandler.ts_status.title"))
                .build()).complete();
    }

    @Override
    public void update() {
        try { updClients(); }
        catch (Exception e) { e.printStackTrace(); }
        try { updTSStatus(); }
        catch (Exception e) { e.printStackTrace(); }
    }

    private void updClients() {
        GuildChannel channel = Tools.tryFindChannel(Commons.getTSOnlineChannel(), getJda());

        int count = getClientCount();
        if (count == lastCount) return;

        channel.getManager().setName(String.format(STR.getString("shandler.ts_clients"), count))
                .complete();
        lastCount = count;
    }

    private void updTSStatus() {
        HashMap<Integer, ArrayList<Client>> channels = new HashMap<>();
        for (Client client: api.getClients()) {
            if (client.isServerQueryClient()) continue;
            final ArrayList<Client> list = channels.getOrDefault(client.getChannelId(), new ArrayList<>());
            list.add(client);
            channels.put(client.getChannelId(), list);
        }

        final EmbedBuilder builder = new EmbedBuilder()
                .setColor(0x666666)
                .setFooter(STR.getString("shandler.ts_status.footer"), "https://icons.iconarchive.com/icons/papirus-team/papirus-apps/256/teamspeak-3-icon.png")
                .setTitle(STR.getString("shandler.ts_status.title"));
        if (channels.isEmpty()) builder.setDescription(STR.getString("shandler.ts_status.no_one"));
        for (Entry<Integer, ArrayList<Client>> e: channels.entrySet()) {
            StringBuilder sb = new StringBuilder();
            for (Client client: e.getValue())
                sb.append(client.getNickname()).append('\n');
            builder.addField(api.getChannelInfo(e.getKey()).getName(), sb.toString(), false);
        }

        tsStatus.editMessage(builder.build()).queue();
    }

    private int getClientCount() {
        if (api == null) throw new NullPointerException("api");
        if (query == null) throw new NullPointerException("query");
        if (!loginSuccess) throw new IllegalStateException("Looks like login has failed!");

        int i = 0;
        for (Client client: api.getClients()) if (!client.isServerQueryClient()) i++;
        return i;
    }

    @Override
    public void onShutdown(@NotNull ShutdownEvent event) {
        super.onShutdown(event);
        query.exit();
    }

    @Override
    public long getPeriod() {
        return 60 * 1000;
    }
}
