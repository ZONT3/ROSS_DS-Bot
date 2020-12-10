package ru.rossteam.dsbot;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchResult;
import ru.rossteam.dsbot.tools.Globals;
import ru.rossteam.dsbot.tools.Streams;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public class Test {

    public static void main(String[] args) throws GeneralSecurityException, IOException {
        Globals.GOOGLE_API = args[0];

        Streams.setupYouTube();
        YouTube.Search.List search = Streams.api.search().list("snippet");
        final List<SearchResult> items = search.setKey(Globals.GOOGLE_API)
                .setChannelId("UCys2ksPVgr2t3eUDGX4h8ow")
                .setType("video")
                .execute().getItems();

        System.out.println(items);


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
