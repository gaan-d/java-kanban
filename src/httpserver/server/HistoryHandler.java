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

public class HistoryHandler extends TaskListHandler {
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    public HistoryHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    protected List<Task> getTaskList() {
        return taskManager.getHistory(); // возвращаем историю задач
    }

    @Override
    protected void handleGet(String response, HttpExchange exchange) throws IOException {
        try {
            List<Task> history = taskManager.getHistory();
            response = gson.toJson(history);
            sendText(exchange, response, 200);
        } catch (JsonParseException | InvalidTaskIdException | IllegalArgumentException e) {
            handleErrorResponse(e, "Ошибка в запросе", 400, exchange);
        } catch (NotFoundException e) {
            handleErrorResponse(e, "История не найдена", 404, exchange);
        }
    }
}