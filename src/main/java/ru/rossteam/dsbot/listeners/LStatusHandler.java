package ru.rossteam.dsbot.listeners;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import ru.rossteam.dsbot.tools.LOG;

import javax.annotation.Nonnull;


public abstract class LStatusHandler extends ListenerAdapter {
    private CallerThread callerThread;
    private JDA jda;

    public abstract void prepare(ReadyEvent event);
    public abstract void update();
    public abstract long getPeriod();

    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        prepare(event);
        jda = event.getJDA();
        callerThread = new CallerThread();
        callerThread.start();
    }

    public JDA getJda() { return jda; }

    public CallerThread getCallerThread() { return callerThread; }

    @SuppressWarnings("BusyWait")
    private class CallerThread extends Thread {
        @Override
        public void run() {
            while (!interrupted()) {
                try { update(); }
                catch (Exception e) { e.printStackTrace(); }
                try { sleep(getPeriod()); }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
