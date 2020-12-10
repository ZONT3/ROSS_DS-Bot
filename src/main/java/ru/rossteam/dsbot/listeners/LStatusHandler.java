package ru.rossteam.dsbot.listeners;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import ru.rossteam.dsbot.tools.Commons;

import javax.annotation.Nonnull;


public abstract class LStatusHandler extends ListenerAdapter {
    private CallerThread callerThread;
    private JDA jda;

    public abstract void prepare(ReadyEvent event);
    public abstract void update();
    public abstract long getPeriod();

    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        jda = event.getJDA();
        try {
            prepare(event);
        } catch (Throwable e) {
            Commons.reportError(e, getClass());
        }
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

        private CallerThread() {
            super("SH$CT-" + LStatusHandler.this.getClass().getSimpleName());
        }

        @Override
        public void run() {
            while (!interrupted()) {
                try { update(); }
                catch (Exception e) {
                    Commons.reportError(e, getClass());
                }
                try { sleep(getPeriod()); }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
