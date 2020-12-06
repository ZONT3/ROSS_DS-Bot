package ru.zont.rgdsb.tools;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;

import java.awt.*;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static ru.zont.rgdsb.tools.Strings.STR;

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

    public static String describeException(Exception e) {
        String localizedMessage = e.getLocalizedMessage();
        return e.getClass().getSimpleName() + (localizedMessage == null ? "" : ": " + localizedMessage);
    }

    public static void addOK(Message msg) {
        msg.addReaction("\u2705").queue();
    }
}
