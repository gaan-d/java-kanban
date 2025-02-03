package httpserver.server;

import com.google.gson.JsonParseException;
import com.sun.net.httpserver.HttpExchange;
import exception.BadRequestException;
import exception.InvalidTaskIdException;
import exception.ManagerValidatePriorityException;
import exception.NotFoundException;
import manager.TaskManager;
import task.Epic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;

public class EpicHandler extends TaskHandler {
    public EpicHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    protected void handleGet(String query, HttpExchange exchange) throws IOException {
        try {
            StringBuilder response = new StringBuilder();

            if (query == null || query.isEmpty()) {
                List<Epic> allTasks = taskManager.getAllEpics();
                for (Epic task : allTasks) {
                    response.append(task.toString()).append("\n");
                }
            } else {
                int taskId = getTaskIdFromRequest(query);
                Epic task = taskManager.getEpic(taskId);

                if (query.contains("/subtasks")) {
                    List<Integer> subtaskIds = task.getSubtaskIds();
                    response.append("Subtask IDs: ").append(subtaskIds.toString()).append("\n");
                } else {
                    response.append(task.toString());
                }
            }
            sendText(exchange, response.toString(), 200);
        } catch (JsonParseException | InvalidTaskIdException | IllegalArgumentException | URISyntaxException e) {
            handleErrorResponse(e, 400, exchange);
        } catch (NotFoundException e) {
            handleErrorResponse(e, 404, exchange);
        }
    }


    @Override
    protected void handlePost(HttpExchange exchange) throws IOException {
        try {
            Epic newTask = readTaskFromRequest(exchange);

            // Проверяем, передаётся ли ID в JSON
            if (newTask.getId() == -1) {  // -1 вместо 0
                int taskId = taskManager.addNewEpic(newTask);
                newTask.setId(taskId); // Обновляем ID объекта Epic

                String response = "Эпическая задача успешно добавлена с ID: " + taskId;
                sendText(exchange, response, 201);
            } else {
                throw new BadRequestException("Ошибка: нельзя отправлять эпик с установленным ID!");
            }
        } catch (JsonParseException | BadRequestException e) {
            handleErrorResponse(e, 400, exchange);
        } catch (ManagerValidatePriorityException e) {
            handleErrorResponse(e, 406, exchange);
        }
    }

    @Override
    protected void handlePut(String query, HttpExchange exchange) throws IOException {
        try {
            int taskId = getTaskIdFromRequest(query);
            Epic updatedEpic = readTaskFromRequest(exchange);
            updatedEpic.setId(taskId);
            taskManager.updateEpic(updatedEpic);
            sendText(exchange, "Эпик с ID " + taskId + " успешно обновлён.", 201);
        } catch (JsonParseException | InvalidTaskIdException | IllegalArgumentException | URISyntaxException
                 | BadRequestException e) {
            handleErrorResponse(e, 400, exchange);
        } catch (NotFoundException e) {
            handleErrorResponse(e, 404, exchange);
        }
    }


    @Override
    protected void handleDelete(String query, HttpExchange exchange) throws IOException {
        try {
            int taskIdToDelete = getTaskIdFromRequest(query);
            taskManager.deleteEpic(taskIdToDelete);
            String response = "Задача с ID: " + taskIdToDelete + " удалена.";
            sendText(exchange, response, 200);
        } catch (JsonParseException | InvalidTaskIdException | IllegalArgumentException | URISyntaxException e) {
            handleErrorResponse(e, 400, exchange);
        }
    }

    @Override
    protected Epic readTaskFromRequest(HttpExchange exchange) {
        InputStream inputStream = exchange.getRequestBody();
        String requestBody = new BufferedReader(new InputStreamReader(inputStream))
                .lines().collect(Collectors.joining("\n"));

        if (requestBody.isEmpty()) {
            throw new BadRequestException("Ошибка: тело запроса не может быть пустым.");
        }

        Epic epic = gson.fromJson(requestBody, Epic.class);

        // Если ID не передан (нулевой), явно его сбрасываем
        if (epic.getId() == 0) {
            epic.setId(-1); // Устанавливаем временное значение, чтобы избежать 0
        }

        return epic;
    }
}
