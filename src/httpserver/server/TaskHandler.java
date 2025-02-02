package httpserver.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.sun.net.httpserver.HttpExchange;
import exception.BadRequestException;
import exception.InvalidTaskIdException;
import exception.ManagerValidatePriorityException;
import exception.NotFoundException;
import httpserver.adapter.DurationAdapter;
import httpserver.adapter.LocalDateTimeAdapter;
import manager.TaskManager;
import task.Task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class TaskHandler extends BaseHttpHandler {
    protected final TaskManager taskManager;
    protected final Gson gson;

    public TaskHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .setPrettyPrinting()
                .create();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            URI requesURI = exchange.getRequestURI();
            String query = requesURI.getQuery();
            String method = exchange.getRequestMethod();
            switch (method) {
                case "GET":
                    handleGet(query, exchange);
                    break;
                case "POST":
                    handlePost(exchange);
                    break;
                case "PUT":
                    handlePut(query, exchange);
                    break;
                case "DELETE":
                    handleDelete(query, exchange);
                    break;
                default:
                    sendUnsupportedMethod(exchange);
                    break;
            }
        } catch (Exception e) {
            sendText(exchange, "Internal server error" + e.getMessage(), 500);
        } finally {
            exchange.close();
        }
    }

    protected void handleGet(String query, HttpExchange exchange) throws IOException {
        try {
            String response;
            if (query == null || query.isEmpty()) {
                List<Task> allTasks = taskManager.getAllTasks();
                response = allTasks.stream()
                        .map(Task::toString)
                        .collect(Collectors.joining("\n"));
            } else {
                int taskId = getTaskIdFromRequest(query);
                Task task = taskManager.getTask(taskId);
                response = task.toString();
            }
            sendText(exchange, response, 200);
        } catch (JsonParseException | InvalidTaskIdException | IllegalArgumentException | URISyntaxException e) {
            handleErrorResponse(e, 400, exchange);
        } catch (NotFoundException e) {
            handleErrorResponse(e, 404, exchange);
        }
    }

    protected void handlePost(HttpExchange exchange) throws IOException {
        try {
            Task newTask = readTaskFromRequest(exchange);
            String response;
            if (newTask.getId() == 0 || newTask.getId() <= 0) {
                int generatedId = taskManager.addNewTask(newTask);
                response = "Задача успешно добавлена с ID: " + generatedId;
            } else {
                taskManager.updateTask(newTask);
                response = "Задача с ID" + newTask.getId() + "обновлена.";
            }
            sendText(exchange, response, 201);
        } catch (JsonParseException | BadRequestException e) {
            handleErrorResponse(e, 400, exchange);
        } catch (ManagerValidatePriorityException e) {
            handleErrorResponse(e, 406, exchange);
        } catch (NotFoundException e) {
            handleErrorResponse(e, 404, exchange);
        }
    }

    protected void handlePut(String query, HttpExchange exchange) throws IOException {
        try {
            int taskId = getTaskIdFromRequest(query);
            Task taskToUpdate = readTaskFromRequest(exchange);
            taskToUpdate.setId(taskId);
            taskManager.updateTask(taskToUpdate);
            String response = "Задача с ID " + taskId + " обновлена.";
            sendText(exchange, response, 201);
        } catch (JsonParseException | InvalidTaskIdException | IllegalArgumentException | URISyntaxException
                 | BadRequestException e) {
            handleErrorResponse(e, 400, exchange);
        } catch (ManagerValidatePriorityException e) {
            handleErrorResponse(e, 406, exchange);
        } catch (NotFoundException e) {
            handleErrorResponse(e, 404, exchange);
        }
    }

    protected void handleDelete(String query, HttpExchange exchange) throws IOException {
        try {
            int taskIdToRemove = getTaskIdFromRequest(query);
            taskManager.deleteTask(taskIdToRemove);
            String response = "Задача с ID: " + taskIdToRemove + " удалена.";
            sendText(exchange, response, 200);
        } catch (JsonParseException | InvalidTaskIdException | IllegalArgumentException | URISyntaxException e) {
            handleErrorResponse(e, 400, exchange);
        }
    }

    protected int getTaskIdFromRequest(String query) throws URISyntaxException {
        if (query != null && query.startsWith("id=")) {
            String[] params = query.split("=");
            if (params.length == 2) {
                String taskId = params[1];
                if (taskId == null || taskId.isEmpty()) {
                    throw new InvalidTaskIdException("Пустой id задачи.");
                }
                try {
                    if (taskId.contains("/subtasks")) {
                        String idWithoutSubtasks = taskId.substring(0, taskId.indexOf("/s"));
                        return Integer.parseInt(idWithoutSubtasks);
                    } else {
                        return Integer.parseInt(taskId);
                    }
                } catch (NumberFormatException e) {
                    throw new InvalidTaskIdException("Id должен быть целым числом");
                }
            }
        }
        throw new IllegalArgumentException("В параметрах запроса не указан id задачи.");
    }

    protected Task readTaskFromRequest(HttpExchange exchange) {
        InputStream stream = exchange.getRequestBody();
        String requestBody = new BufferedReader(new InputStreamReader(stream))
                .lines().collect(Collectors.joining("\n"));
        if (requestBody.isEmpty()) {
            throw new BadRequestException("Ошибка: тело запроса не может быть пустым.");
        }
        return gson.fromJson(requestBody, Task.class);
    }

    protected void handleErrorResponse(Exception e, int statusCode, HttpExchange exchange) throws IOException {
        String errorMessage = e.getMessage();
        sendText(exchange, errorMessage, statusCode);
    }

    protected void sendUnsupportedMethod(HttpExchange exchange) throws IOException {
        String response = "Метод не поддерживается.";
        sendText(exchange, response, 405);
    }
}

