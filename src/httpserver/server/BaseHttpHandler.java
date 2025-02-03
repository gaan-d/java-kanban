package httpserver.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public abstract class BaseHttpHandler implements HttpHandler {
    protected void sendText(HttpExchange exchange, String text, int statusCode) throws IOException {
        switch (statusCode) {
            case 200, 201 -> sendJson(exchange, text, statusCode);
            case 400 -> sendBadRequest(exchange, text);
            case 404 -> sendNotFound(exchange, text);
            case 406 -> sendNotAcceptable(exchange, text);
            default -> sendGenericResponse(exchange, text, statusCode);
        }
    }

    private void sendJson(HttpExchange exchange, String json, int statusCode) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=UTF-8");
        sendResponse(exchange, json, statusCode);
    }

    private void sendBadRequest(HttpExchange exchange, String message) throws IOException {
        sendResponse(exchange, "400 Bad Request - " + message, 400);
    }

    private void sendNotFound(HttpExchange exchange, String message) throws IOException {
        sendResponse(exchange, "404 Not Found - " + message, 404);
    }

    private void sendNotAcceptable(HttpExchange exchange, String message) throws IOException {
        sendResponse(exchange, "406 Not Acceptable - " + message, 406);
    }

    private void sendGenericResponse(HttpExchange exchange, String message, int statusCode) throws IOException {
        sendResponse(exchange, message, statusCode);
    }

    private void sendResponse(HttpExchange exchange, String message, int statusCode) throws IOException {
        byte[] response = message.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }
}

