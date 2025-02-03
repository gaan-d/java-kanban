package httpserver.server;

import com.google.gson.JsonParseException;
import com.sun.net.httpserver.HttpExchange;
import exception.BadRequestException;
import exception.InvalidTaskIdException;
import exception.ManagerValidatePriorityException;
import exception.NotFoundException;
import manager.TaskManager;
import task.Subtask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;

public class SubtaskHandler extends TaskHandler {
    public SubtaskHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    protected void handleGet(String query, HttpExchange exchange) throws IOException {
        try {
            String response;
            if (query == null || query.isEmpty()) {
                List<Subtask> allTasks = taskManager.getAllSubtasks();
                response = allTasks.stream()
                        .map(Subtask::toString)
                        .collect(Collectors.joining("\n"));
            } else {
                int taskId = getTaskIdFromRequest(query);
                Subtask task = taskManager.getSubtask(taskId);
                response = task.toString();
            }
            sendText(exchange, response, 200);
        } catch (JsonParseException | InvalidTaskIdException | IllegalArgumentException | URISyntaxException e) {
            handleErrorResponse(e, 400, exchange);
        } catch (NotFoundException e) {
            handleErrorResponse(e, 404, exchange);
        }
    }

    @Override
    protected void handlePost(HttpExchange exchange) throws IOException {
        try {
            Subtask newTask = readTaskFromRequest(exchange);
            int taskId = newTask.getId();
            String response;
            if (taskId == 0) {
                taskId = taskManager.addNewSubtask(newTask);
                if (taskId == -1) {
                    //throw new NotFoundException("Подзадача не может быть создана без эпика");
                    response = "Подзадача не может быть создана без эпика";
                    sendText(exchange, response, 404);
                }
                response = "Задача успешно добавлена с ID: " + taskId;
            } else {
                taskManager.updateSubtask(newTask);
                response = "Задача с ID " + taskId + " успешно обновлена.";
            }
            sendText(exchange, response, 201);
        } catch (JsonParseException | BadRequestException e) {
            handleErrorResponse(e, 400, exchange);
        } catch (NotFoundException e) {
            handleErrorResponse(e, 404, exchange);
        } catch (ManagerValidatePriorityException e) {
            handleErrorResponse(e, 406, exchange);
        }
    }

    @Override
    protected void handlePut(String query, HttpExchange exchange) throws IOException {
        try {
            int taskId = getTaskIdFromRequest(query);
            Subtask updatedTask = readTaskFromRequest(exchange);
            updatedTask.setId(taskId);
            taskManager.updateSubtask(updatedTask);
            String response = "Задача с ID " + taskId + " успешно обновлена.";
            sendText(exchange, response, 201);
        } catch (JsonParseException | InvalidTaskIdException | IllegalArgumentException | URISyntaxException
                 | BadRequestException e) {
            handleErrorResponse(e, 400, exchange);
        } catch (NotFoundException e) {
            handleErrorResponse(e, 404, exchange);
        } catch (ManagerValidatePriorityException e) {
            handleErrorResponse(e, 406, exchange);
        }
    }

    @Override
    protected void handleDelete(String query, HttpExchange exchange) throws IOException {
        try {
            int taskIdToDelete = getTaskIdFromRequest(query);
            taskManager.deleteSubtask(taskIdToDelete);
            String response = "Задача с ID: " + taskIdToDelete + " удалена.";
            sendText(exchange, response, 200);
        } catch (JsonParseException | InvalidTaskIdException | IllegalArgumentException | URISyntaxException e) {
            handleErrorResponse(e, 400, exchange);
        }
    }

    @Override
    protected Subtask readTaskFromRequest(HttpExchange exchange) {
        InputStream inputStream = exchange.getRequestBody();
        String requestBody = new BufferedReader(new InputStreamReader(inputStream))
                .lines().collect(Collectors.joining("\n"));
        if (requestBody.isEmpty()) {
            throw new BadRequestException("Ошибка: тело запроса не может быть пустым.");
        }
        return gson.fromJson(requestBody, Subtask.class);
    }
}