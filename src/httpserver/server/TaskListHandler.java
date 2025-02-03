package httpserver.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.sun.net.httpserver.HttpExchange;
import exception.InvalidTaskIdException;
import exception.NotFoundException;
import httpserver.adapter.DurationAdapter;
import httpserver.adapter.LocalDateTimeAdapter;
import manager.TaskManager;
import task.Task;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public abstract class TaskListHandler extends BaseHttpHandler {
    protected final TaskManager taskManager;
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    public TaskListHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String response = "";
        try {
            String method = exchange.getRequestMethod();
            if (method.equals("GET")) {
                handleGet(response, exchange);
            } else {
                sendUnsupportedMethod(response, exchange);
            }
        } catch (NotFoundException e) {
            sendText(exchange, e.getMessage(), 404);
        } catch (Exception e) {
            sendText(exchange, "500 Internal Server Error", 500);
        } finally {
            exchange.close();
        }
    }

    // Абстрактный метод, который должен быть реализован в подклассах
    protected abstract List<Task> getTaskList();

    // Логика обработки GET-запроса
    protected void handleGet(String response, HttpExchange exchange) throws IOException {
        try {
            List<Task> tasks = getTaskList();
            response = gson.toJson(tasks);
            sendText(exchange, response, 200);
        } catch (JsonParseException | InvalidTaskIdException | IllegalArgumentException e) {
            handleErrorResponse(e, "Ошибка в запросе", 400, exchange);
        } catch (NotFoundException e) {
            handleErrorResponse(e, "Задачи не найдены", 404, exchange);
        }
    }

    // Логика обработки ошибок
    protected void handleErrorResponse(Exception e, String response, int statusCode, HttpExchange exchange) throws IOException {
        response = e.getMessage();
        sendText(exchange, response, statusCode);
    }

    // Логика обработки не поддерживаемых методов
    protected void sendUnsupportedMethod(String response, HttpExchange exchange) throws IOException {
        response = "Метод не поддерживается.";
        sendText(exchange, response, 405);
    }
}
