package ru.rossteam.dsbot.command;

import javafx.util.Pair;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.rossteam.dsbot.DescribedException;
import ru.rossteam.dsbot.tools.Commands;
import ru.rossteam.dsbot.tools.Messages;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;

import static ru.rossteam.dsbot.tools.Streams.*;
import static ru.rossteam.dsbot.tools.Strings.STR;

public class Streams extends CommandAdapter {

    @Override
    public void onRequest(@NotNull MessageReceivedEvent event) throws UserInvalidArgumentException {
        Commands.Input input = Commands.parseInput(this, event);

        ArrayList<String> args = input.getArgs();
        if (args.size() < 1) throw new UserInvalidArgumentException(STR.getString("err.insufficient_args"));

        switch (input.getArg(0).toLowerCase()) {
            case "list":
            case "get":
                StringBuilder builder = new StringBuilder();
                for (String channel: retrieveWatchingList())
                    builder.append("• ").append(getChannel(channel)).append('\n');
                event.getChannel().sendMessage(new EmbedBuilder()
                        .setTitle(STR.getString("comm.streams.list.title"))
                        .setDescription(builder)
                        .build()).queue();
                break;
            case "add":
                addWatch(event, input);
                Messages.addOK(event.getMessage());
                break;
            case "rm":
                removeWatch(event, input);
                Messages.addOK(event.getMessage());
                break;
            default:
                throw new UserInvalidArgumentException("Unknown mode: " + input.getArg(0));
        }
    }

    private String getChannel(String channel) {
        final Pair<String, String> st = wlStatementToLink(channel);
        return String.format("[%s](%s)", st.getKey(), st.getValue());
    }

    private void removeWatch(@NotNull MessageReceivedEvent event, Commands.Input input) {
        String res = getValidReference(event, input);
        removeFromWatchingList(res);
    }

    private void addWatch(@NotNull MessageReceivedEvent event, Commands.Input input) {
        String res = getValidReference(event, input);
        addToWatchingList(res);
    }

    private String getValidReference(@NotNull MessageReceivedEvent event, Commands.Input input) {
        ArrayList<String> args = input.getArgs();
        if (args.size() < 2) throw new UserInvalidArgumentException(STR.getString("err.insufficient_args"));
        final String reference = args.get(1);

        String channel;
        String service = "yt:";
        try {
            channel = getYTChannelID(reference);
        } catch (Exception e) {
            try {
                service = "ttv:";
                channel = getTwitchChannel(reference);
            } catch (Throwable ee) {
                e.printStackTrace();
                ee.printStackTrace();
                throw new DescribedException(
                        STR.getString("comm.streams.err.add.title"),
                        String.format(STR.getString("comm.streams.err.add"),
                                Messages.describeException(e)));
            }
        }
        return service + channel;
    }

    @Override
    public String getCommandName() {
        return "streams";
    }

    @Override
    public String getSynopsis() {
        return "streams get|list|add|rm ...\n" +
                "streams get|list\n" +
                "streams add <link>|<channelID>|<username>\n" +
                "streams rm <link>|<channelID>|<username>";
    }

    @Override
    public String getDescription() {
        return STR.getString("comm.streams.desc");
    }

    @Override
    protected Properties getPropsDefaults() {
        return null;
    }

    @Override
    public boolean checkPermission(MessageReceivedEvent event) {
        Commands.Input input = Commands.parseInput(this, event);
        boolean b = false;
        for (String comm: Arrays.asList("add", "rm"))
            b = b || comm.equalsIgnoreCase(input.getArg(0));
        if (b) {
            Member member = event.getMember();
            if (member == null) return false;
            return member.hasPermission(Permission.ADMINISTRATOR);
        } else return true;
    }
}
