package ru.rossteam.dsbot.tools;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.dv8tion.jda.api.EmbedBuilder;
import ru.zont.dsbot.core.ZDSBot;
import ru.zont.dsbot.core.tools.Tools;

import java.util.Map;

public class Forms {
    public static void newForm(ZDSBot bot, JsonObject form) {
        EmbedBuilder builder = new EmbedBuilder();
        for (Map.Entry<String, JsonElement> entry: form.entrySet()) {
            if ("#Title".equalsIgnoreCase(entry.getKey())) {
                builder.setTitle(entry.getValue().getAsJsonObject().get("data").getAsString());
                continue;
            }
            builder.addField(entry.getKey(), parseField(entry.getValue().getAsJsonObject().get("data")), false);
        }

        builder.setColor(0xf01010);
        Tools.tryFindTChannel(Commons.getReportsChannelID(), bot.jda).sendMessage(builder.build()).complete();
    }

    private static String parseField(JsonElement data) {
        if (data.isJsonArray()) {
            StringBuilder builder = new StringBuilder();
            boolean f = true;
            for (JsonElement d: data.getAsJsonArray()) {
                if (!f) builder.append(", ");
                else f = false;
                if (d.isJsonArray())
                    builder.append(d.getAsJsonArray().toString());
                else if (d.isJsonPrimitive())
                    builder.append(d.getAsString());
            }
            return builder.toString();
        } else return data.getAsString();
    }
}
