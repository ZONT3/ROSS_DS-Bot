package ru.rossteam.dsbot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import ru.rossteam.dsbot.listeners.HClientsTS;
import ru.rossteam.dsbot.listeners.HNews;
import ru.rossteam.dsbot.listeners.HStreams;
import ru.rossteam.dsbot.tools.Globals;
import ru.rossteam.dsbot.command.CommandAdapter;
import ru.rossteam.dsbot.tools.Configs;
import ru.rossteam.dsbot.tools.Messages;
import ru.rossteam.dsbot.tools.Strings;

import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Main extends ListenerAdapter {

    public static void main(String[] args) throws LoginException, InterruptedException, IllegalArgumentException {
        Globals.commandAdapters = registerCommands();
        Configs.writeDefaultGlobalProps();

        handleArguments(args);

        Globals.jda = JDABuilder.createLight(args[0], GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MEMBERS)
                .addEventListeners(
                        new Main()
                        ,Globals.ytStreams = new HStreams()
                        ,Globals.usersMonitoring = new HClientsTS()
                        ,Globals.news = new HNews()
                )
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setChunkingFilter(ChunkingFilter.ALL)
                .build();
        Globals.jda.awaitReady();
    }

    private static void handleArguments(String[] args) throws LoginException, IllegalArgumentException {
        if (args.length < 4) throw new LoginException("Too few arguments");

        String[] tsqArgs = args[1].split(";");
        if (tsqArgs.length < 3) throw new IllegalArgumentException("Too few TS Query arguments");
        Globals.tsq_host  = tsqArgs[0];
        Globals.tsq_login = tsqArgs[1];
        Globals.tsq_pass  = tsqArgs[2];

        Globals.GOOGLE_API = args[2];
        Globals.TWITCH_API_SECRET = args[3];
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        try {
            CommandAdapter.onMessageReceived(event, Globals.commandAdapters);
        } catch (Exception e) {
            e.printStackTrace();
            event.getChannel().sendMessage(
                    new EmbedBuilder( Messages.error(
                            Strings.STR.getString("err.unexpected"),
                            Messages.describeException(e)))
                    .setFooter(Strings.STR.getString("err.unexpected.foot"))
                    .build()).queue();
        }
    }

    private static CommandAdapter[] registerCommands() {
        if (Configs.DIR_PROPS.exists() && !Configs.DIR_PROPS.isDirectory())
            if (!Configs.DIR_PROPS.delete())
                throw new RuntimeException("Cannot remove file named as dir 'properties'");
        if (!Configs.DIR_PROPS.exists())
            if (!Configs.DIR_PROPS.mkdir())
                throw new RuntimeException("Cannot create properties dir");

        List<ClassLoader> classLoadersList = new LinkedList<>();
        classLoadersList.add(ClasspathHelper.contextClassLoader());
        classLoadersList.add(ClasspathHelper.staticClassLoader());

        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setScanners(new SubTypesScanner(false /* don't exclude Object.class */), new ResourcesScanner())
                .setUrls(ClasspathHelper.forClassLoader(classLoadersList.toArray(new ClassLoader[0])))
                .filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix("ru.rossteam.dsbot.command"))));
        Set<Class<? extends CommandAdapter>> allClasses =
                reflections.getSubTypesOf(CommandAdapter.class);

        ArrayList<CommandAdapter> res = new ArrayList<>();
        for (Class<? extends CommandAdapter> klass: allClasses) {
            if (Modifier.isAbstract(klass.getModifiers())) continue;
            try {
                System.out.printf("Registering CommandAdapter class: %s\n", klass.getSimpleName());
                CommandAdapter adapter = klass.newInstance();
                res.add(adapter);
                System.out.printf("Successfully registered adapter #%d, commandName: %s\n", res.size(), adapter.getCommandName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return res.toArray(new CommandAdapter[0]);
    }
}
