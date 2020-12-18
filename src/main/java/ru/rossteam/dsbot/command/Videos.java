package ru.rossteam.dsbot.command;

import javafx.util.Pair;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import ru.rossteam.dsbot.tools.TV;
import ru.zont.dsbot.core.ZDSBot;
import ru.zont.dsbot.core.commands.CommandAdapter;
import ru.zont.dsbot.core.commands.Commands;
import ru.zont.dsbot.core.tools.Messages;
import ru.zont.dsbot.core.tools.Tools;

import java.util.ArrayList;
import java.util.Properties;

import static ru.zont.dsbot.core.tools.Strings.STR;

public class Videos extends CommandAdapter {

    public Videos(ZDSBot bot) throws RegisterException {
        super(bot);
    }

    @Override
    public void onRequest(@NotNull MessageReceivedEvent event) throws UserInvalidArgumentException {
        Commands.Input input = Commands.parseInput(this, event);

        ArrayList<String> args = input.getArgs();
        if (args.size() < 1) throw new UserInvalidArgumentException(STR.getString("err.insufficient_args"));

        switch (input.getArg(0).toLowerCase()) {
            case "list":
            case "get":
                StringBuilder builder = new StringBuilder();
                for (String channel: TV.retrieveYTWatchingList())
                    builder.append("• ").append(getChannel(channel)).append('\n');
                event.getChannel().sendMessage(new EmbedBuilder()
                        .setTitle(STR.getString("comm.videos.list.title"))
                        .setDescription(builder)
                        .build()).queue();
                break;
            case "add":
                addWatch(input);
                Messages.addOK(event.getMessage());
                break;
            case "rm":
                removeWatch(input);
                Messages.addOK(event.getMessage());
                break;
            default:
                throw new UserInvalidArgumentException("Unknown mode: " + input.getArg(0));
        }
    }

    private static synchronized void removeWatch(Commands.Input input) {
        final ArrayList<String> wl = TV.retrieveYTWatchingList();
        wl.remove(TV.getValidReference(input));
        Tools.commitObject(TV.ytWatchlist, wl);
    }

    private static synchronized void addWatch(Commands.Input input) {
        final ArrayList<String> wl = TV.retrieveYTWatchingList();
        final String ref = TV.getValidReference(input);
        if (ref.startsWith("ttv:")) throw new UserInvalidArgumentException("Twitch is not supported");
        wl.add(ref);
        Tools.commitObject(TV.ytWatchlist, wl);
    }

    private String getChannel(String channel) {
        final Pair<String, String> s = TV.wlStatementToLink(channel);
        return String.format("[%s](%s)", s.getKey(), s.getValue());
    }

    @Override
    public String getCommandName() {
        return "yt";
    }

    @Override
    public String getSynopsis() {
        return "yt get|list|add|rm ...\n" +
                "yt get|list\n" +
                "yt add <link>|<channelID>|<username>\n" +
                "yt rm <link>|<channelID>|<username>";
    }

    @Override
    public String getDescription() {
        return STR.getString("comm.videos.desc");
    }

    @Override
    protected Properties getPropsDefaults() {
        return null;
    }
}
