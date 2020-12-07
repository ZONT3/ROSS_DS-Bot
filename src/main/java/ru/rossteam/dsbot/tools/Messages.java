package ru.rossteam.dsbot.tools;

import com.google.api.services.youtube.model.SearchResult;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;

import java.awt.*;
import java.time.Instant;
import java.util.List;

public class Messages {
    public static MessageEmbed error(String title, String description) {
        return new EmbedBuilder()
                .setTitle(title)
                .setDescription(description)
                .setColor(Color.RED)
                .build();
    }

    public static void printError(MessageChannel channel, String title, String description) {
        channel.sendMessage(error(title, description)).queue();
    }

    public static void tryPrintError(String title, String description) {
        TextChannel channel = null;
        try {
            channel = Commons.tryFindTChannel(Configs.getLogChannelID(), Globals.jda);
        } catch (Exception ignored) { };
        if (channel == null) {
            for (Guild guild: Globals.jda.getGuilds()) {
                channel = guild.getSystemChannel();
                if (channel != null) break;
            }
            if (channel == null) {
                for (Guild guild: Globals.jda.getGuilds()) {
                    channel = guild.getDefaultChannel();
                    if (channel != null) break;
                }
            }
        }
        if (channel != null) printError(channel, title, description);
    }

    public static MessageEmbed addTimestamp(MessageEmbed e) {
        return addTimestamp(new EmbedBuilder(e));
    }

    public static MessageEmbed addTimestamp(EmbedBuilder builder) {
        return builder.setTimestamp(Instant.now()).build();
    }

    public static void sendSplit(MessageChannel channel, List<EmbedBuilder> builders, boolean timestamp) {
        for (EmbedBuilder builder: builders)
            channel.sendMessage(
                    timestamp
                    ? Messages.addTimestamp(builder)
                    : builder.build()
            ).complete();
    }

    public static void sendSplit(MessageChannel channel, List<EmbedBuilder> builders) {
        sendSplit(channel, builders, false);
    }

    public static String describeException(Throwable e) {
        return describeException(null, e);
    }

    public static String describeException(Class<?> klass, Throwable e) {
        String localizedMessage = e.getLocalizedMessage();
        return (klass != null ? (klass.getSimpleName() + ": ") : "") + e.getClass().getSimpleName() + (localizedMessage == null ? "" : ": " + localizedMessage);
    }

    public static void addOK(Message msg) {
        msg.addReaction("\u2705").queue();
    }

    public static class Streams {
        public static MessageEmbed newStream(SearchResult result) {
            return new EmbedBuilder()

                    .build();
        }
    }
}
