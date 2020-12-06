package ru.zont.rgdsb.listeners;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import ru.zont.rgdsb.tools.Configs;
import ru.zont.rgdsb.tools.LOG;
import ru.zont.rgdsb.tools.Strings;

import javax.annotation.Nonnull;

/**
 * Template listener for monitoring count of users with role
 */
public class LPlayersMonitoring extends ListenerAdapter {
    private boolean ready = false;

    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        // TODO
        ready = true;
    }

    @Override
    public void onGuildMemberRoleAdd(@Nonnull GuildMemberRoleAddEvent event) {
        if (!ready) return;
        // TODO
    }

    @Override
    public void onGuildMemberRoleRemove(@Nonnull GuildMemberRoleRemoveEvent event) {
        if (!ready) return;
        // TODO
    }

    private static void displayMembersWithRole(Guild guild, Role role, String channelId, String formattedDisplay) {
        VoiceChannel channel = guild.getVoiceChannelById(channelId);
        if (channel == null) return;

        int size = guild.getMembersWithRoles(role).size();
        LOG.d("Updating players: %d", size);
        channel
                .getManager()
                .setName( String.format(formattedDisplay, size) )
                .queue(v -> LOG.d("Queued"));
    }
}
