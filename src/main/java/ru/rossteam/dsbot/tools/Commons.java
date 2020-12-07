package ru.rossteam.dsbot.tools;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.NotNull;

public class Commons {

    @NotNull
    public static GuildChannel tryFindChannel(String channelID, JDA jda) throws NullPointerException {
        GuildChannel channel = null;
        for (Guild guild: jda.getGuilds()) {
            channel = guild.getGuildChannelById(channelID);
            if (channel != null) break;
        }
        if (channel == null) throw new NullPointerException("Cannot find channel");
        return channel;
    }

    @NotNull
    public static TextChannel tryFindTChannel(String channelID, JDA jda) throws NullPointerException {
        TextChannel channel = null;
        for (Guild guild: jda.getGuilds()) {
            channel = guild.getTextChannelById(channelID);
            if (channel != null) break;
        }
        if (channel == null) throw new NullPointerException("Cannot find channel");
        return channel;
    }
}
