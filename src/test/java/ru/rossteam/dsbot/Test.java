package ru.rossteam.dsbot;

import ru.rossteam.dsbot.tools.Globals;
import ru.rossteam.dsbot.tools.Streams;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Scanner;

public class Test {

    public static void main(String[] args) throws GeneralSecurityException, IOException {
        Globals.GOOGLE_API = args[0];
        Streams.getYouTube();
        Scanner s = new Scanner(System.in);
        String line;
        while (s.hasNext() && !(line = s.nextLine()).equals("q")) {
            try {
                String channelID = Streams.getChannelID(line);
                System.out.println("Channel ID: " + channelID);
                System.out.println(Streams.getStreams(channelID).size());
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

}
