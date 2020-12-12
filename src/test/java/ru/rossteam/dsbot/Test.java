package ru.rossteam.dsbot;

import ru.rossteam.dsbot.tools.Streams;

import java.io.IOException;
import java.util.Scanner;

public class Test {

    public static void main(String[] args) throws IOException {
        Scanner s = new Scanner(System.in);

        String id;
        while ((id = s.nextLine()).matches(Streams.REGEX_CHANNEL_ID)) {
            final Streams.YTStream streams =
                    Streams.getYTStream(id);
            System.out.println(streams);
        }
    }

}
