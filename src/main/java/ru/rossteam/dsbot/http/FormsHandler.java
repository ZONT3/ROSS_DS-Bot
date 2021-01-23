package ru.rossteam.dsbot.http;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.commons.io.IOUtils;
import ru.rossteam.dsbot.tools.Forms;
import ru.zont.dsbot.core.ZDSBot;
import ru.zont.dsbot.core.tools.Messages;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

// {"Чекбокс-вопрос":{"data":["Вариант 1","Столбец 2"],"type":"CHECKBOX"},"Радио-вопрос":{"data":"Вариант 1","type":"MULTIPLE_CHOICE"},"Строка":{"data":"","type":"TEXT"},"Абзац":{"data":"привет я ебло крч\nхуйня\nэто\nвсе\nво\nмне","type":"PARAGRAPH_TEXT"},"Сетка":{"data":["Столбец 2",null],"type":"GRID"},"Дата":{"data":"2021-01-12","type":"DATE"},"Время":{"data":"19:55","type":"TIME"}}

//{"Чекбокс-вопрос":["Вариант 1","Столбец 2"],"Радио-вопрос":"я хуй","Список":"Вариант 2","Строка":"ебло","Абзац":"привет я ебло крч\nхуйня\nэто\nвсе\nво\nмне","Шкала":"2","Сетка":["Вариант 1","Столбец 2"],"Сетка ЧБ":[["Вариант 1","Столбец 2"],["Столбец 2"]],"Дата":"2021-01-12","Время":"19:55","Файл":["11mKsPR-ubAOJm54zsWlr10k5Kx30zZnt"]}

public class FormsHandler implements HttpHandler {

    private final ZDSBot bot;

    public FormsHandler(ZDSBot bot) {
        this.bot = bot;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (!"post".equalsIgnoreCase(exchange.getRequestMethod())) {
                response(exchange, "Only POST method is acceptable", 400);
                return;
            }

            final List<String> contentType = exchange.getRequestHeaders().get("Content-type");
            if (contentType.size() < 1 || !contentType.contains("application/json")) {
                response(exchange, "Content-type should be JSON", 400);
                return;
            }

            try {
                final String content = IOUtils.toString(exchange.getRequestBody(), StandardCharsets.UTF_8);
                new Committer(content).start();
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
                response(exchange, "Not a valid JSON", 400);
                return;
            }

            response(exchange, "OK", 200);
        } catch (Throwable e) {
            e.printStackTrace();
            response(exchange, "Internal server error: " + Messages.describeException(e), 500);
        }
    }

    private void response(HttpExchange exchange, String response, int code) throws IOException {
        exchange.sendResponseHeaders(code, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    public class Committer extends Thread {
        private final JsonObject form;

        public Committer(String formStr) throws JsonSyntaxException {
            super("Form Committer");
            form = new Gson().fromJson(formStr, JsonObject.class);
        }

        @Override
        public void run() {
            Forms.newForm(bot, form);
        }
    }

    //    private HashMap<String, String> getParams(HttpExchange exchange) {
//        HashMap<String, String> params = new HashMap<>();
//        for (String query: exchange.getRequestURI().getRawQuery().split("&")) {
//            String[] q = query.split("=");
//            if (q.length < 1) continue;
//            if (q.length > 2) System.err.println("WARNING: Probably bad query entry: " + query);
//            String k = q[0], v;
//            if (q.length == 2) v = q[1];
//            else v = "";
//
//            try {
//                k = URLDecoder.decode(k, "UTF-8");
//                v = URLDecoder.decode(k, "UTF-8");
//            } catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//            }
//            params.put(k, v);
//        }
//        return params;
//    }
}
