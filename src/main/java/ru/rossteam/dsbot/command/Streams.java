package ru.rossteam.dsbot.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import ru.rossteam.dsbot.tools.Commands;
import ru.rossteam.dsbot.tools.Messages;
import ru.rossteam.dsbot.tools.Strings;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Properties;

import static ru.rossteam.dsbot.tools.Streams.*;
import static ru.rossteam.dsbot.tools.Strings.STR;

public class Streams extends CommandAdapter {
    @Override
    public void onRequest(@NotNull MessageReceivedEvent event) throws UserInvalidArgumentException {
        Commands.Input input = Commands.parseInput(this, event);

        ArrayList<String> args = input.getArgs();
        if (args.size() < 1) throw new UserInvalidArgumentException(STR.getString("err.insufficient_args"));

        switch (input.getArg(0)) {
            case "list":
            case "get":
                StringBuilder builder = new StringBuilder();
                for (String channel: retrieveWatchingList())
                    builder.append(channel).append('\n');
                event.getChannel().sendMessage(new EmbedBuilder()
                        .setTitle(STR.getString("comm.streams.list.title"))
                        .setDescription(builder)
                        .build()).queue();
                break;
            case "add":
                addWatch(event, input);
                break;
            default:
                throw new UserInvalidArgumentException("Unknown mode: " + input.getArg(0));
        }
    }

    private void addWatch(@NotNull MessageReceivedEvent event, Commands.Input input) {
        ArrayList<String> args = input.getArgs();
        if (args.size() < 2) throw new UserInvalidArgumentException(STR.getString("err.insufficient_args"));
        String channelID;
        try {
            channelID = getChannelID(args.get(1));
        } catch (IOException e) {
            e.printStackTrace();
            Messages.printError(event.getChannel(),
                    STR.getString("err.general"),
                    String.format(STR.getString("comm.streams.err.add"),
                            Messages.describeException(e)));
            return;
        }

        addToWatchingList("https://www.youtube.com/channel/" + channelID);
    }

    @Override
    public String getCommandName() {
        return "streams";
    }

    @Override
    public String getSynopsis() {
        return "streams <get|list|add> ...\n" +
                "streams get|list\n" +
                "streams add <link|channelID|username>";
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
        if ("add".equalsIgnoreCase(input.getArg(0))) {
            Member member = event.getMember();
            if (member == null) return false;
            return member.hasPermission(Permission.ADMINISTRATOR);
        } else return true;
    }
}
