package ru.rossteam.dsbot.listeners;

import com.github.theholywaffle.teamspeak3.TS3Api;
import com.github.theholywaffle.teamspeak3.TS3Config;
import com.github.theholywaffle.teamspeak3.TS3Query;
import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ShutdownEvent;
import org.jetbrains.annotations.NotNull;
import ru.rossteam.dsbot.tools.Commons;
import ru.rossteam.dsbot.tools.Configs;
import ru.rossteam.dsbot.tools.Globals;
import ru.rossteam.dsbot.tools.Strings;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HClientsTS extends LStatusHandler {
    public static final String DS_BOT_NAME = "DS Bot";
    private TS3Api api;
    private TS3Query query;
    private boolean loginSuccess = false;

    int lastCount = Integer.MIN_VALUE;

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
    }

    @Override
    public void update() {
        GuildChannel channel = tryFindChannel(Configs.getTSCountChannelID());

        int count = getClientCount();
        if (count == lastCount) return;

        channel.getManager().setName(String.format(Strings.STR.getString("shandler.ts_clients"), count))
                .complete();
        lastCount = count;
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
        return 60000;
    }
}
