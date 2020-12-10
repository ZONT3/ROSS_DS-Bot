package ru.rossteam.dsbot;

import com.github.twitch4j.helix.domain.Stream;
import com.github.twitch4j.helix.domain.StreamList;
import ru.rossteam.dsbot.tools.Globals;
import ru.rossteam.dsbot.tools.Streams;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static ru.rossteam.dsbot.tools.Streams.*;

public class Test {

    public static void main(String[] args) throws GeneralSecurityException, IOException {
        Globals.GOOGLE_API = args[0];
        Globals.TWITCH_API_SECRET = args[1];

        setupTwitch();
        final List<Stream> streams = helix.getStreams(null, null, null, null, null, null, null, Collections.singletonList("elwycco")).execute().getStreams();

        System.out.println(streams);
//        Streams.getYouTube();
//        Scanner s = new Scanner(System.in);
//        String line;
//        while (s.hasNext() && !(line = s.nextLine()).equals("q")) {
//            try {
//                String channelID = Streams.getChannelID(line);
//                System.out.println("Channel ID: " + channelID);
//                System.out.println(Streams.getStreams(channelID).size());
//            } catch (Exception e) { e.printStackTrace(); }
//        }
    }

}
