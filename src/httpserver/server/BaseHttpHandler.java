package httpserver.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public abstract class BaseHttpHandler implements HttpHandler {
    protected void sendText(HttpExchange exchange, String text, int statusCode) throws IOException{
        String responseText = switch (statusCode){
            case 404 -> "404 Not Found - " + text;
            case 406 -> "406 Not Acceptable - " + text;
            default -> text;
        };
        byte[] response = responseText.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }
}
