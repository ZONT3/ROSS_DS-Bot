package ru.rossteam.dsbot.listeners;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import ru.rossteam.dsbot.tools.Commons;
import ru.rossteam.dsbot.tools.Messages;

import javax.annotation.Nonnull;

import static ru.rossteam.dsbot.tools.Strings.STR;


public abstract class LStatusHandler extends ListenerAdapter {
    private CallerThread callerThread;
    private JDA jda;

    public abstract void prepare(ReadyEvent event);
    public abstract void update();
    public abstract long getPeriod();

    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        try {
            prepare(event);
        } catch (Throwable e) {
            e.printStackTrace();
            Messages.tryPrintError(STR.getString("err.unexpected"),
                    Messages.describeException(getClass(), e));
        }
        jda = event.getJDA();
        callerThread = new CallerThread();
        callerThread.start();
    }

    public JDA getJda() { return jda; }

    public CallerThread getCallerThread() { return callerThread; }

    @NotNull
    public GuildChannel tryFindChannel(String channelID) throws NullPointerException {
        return Commons.tryFindChannel(channelID, getJda());
    }

    @NotNull
    public MessageChannel tryFindTChannel(String channelID) throws NullPointerException {
        return Commons.tryFindTChannel(channelID, getJda());
    }

    @SuppressWarnings("BusyWait")
    private class CallerThread extends Thread {
        @Override
        public void run() {
            while (!interrupted()) {
                try { update(); }
                catch (Exception e) {
                    e.printStackTrace();
                    Messages.tryPrintError(STR.getString("err.unexpected"),
                            Messages.describeException(getClass(), e));
                }
                try { sleep(getPeriod()); }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
