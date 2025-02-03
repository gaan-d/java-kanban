package HttpServer;

import com.google.gson.Gson;
import httpserver.server.HttpTaskServer;
import manager.InMemoryTaskManager;
import manager.Managers;
import manager.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Status;
import task.Task;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TaskHandlerTest {
    TaskManager manager = new InMemoryTaskManager(Managers.getDefaultHistory());
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    Gson gson = HttpTaskServer.getGson();
    HttpClient client;

    public TaskHandlerTest() throws IOException {
    }

    @BeforeEach
    public void setUp() {
        manager.deleteTasks();
        manager.deleteSubtasks();
        manager.deleteEpics();
        taskServer.start();
        client = HttpClient.newHttpClient();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    public void testPostTask() throws IOException, InterruptedException {
        LocalDateTime startTime = LocalDateTime.of(2024, 10, 1, 1, 0);
        Task task = new Task("Task1", "Testing task1", startTime,
                Duration.ofMinutes(5));
        String taskJson = gson.toJson(task);

        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа
        assertEquals(201, response.statusCode());

        // проверяем, что создано корректное количество задач и получаем список созданных задач
        List<Task> tasksFromManager = checkTaskFromManager(1);
        System.out.println("Response body: " + response.body());
        task.setId(1);
        // проверяем идентичность задач
        checkTaskEquality(task, tasksFromManager.getFirst());
    }

    @Test
    public void testPostErrValidateTask() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks");

        LocalDateTime startTime1 = LocalDateTime.of(2024, 10, 1, 1, 0);
        Task task1 = new Task("Task1", "Testing task1", startTime1,
                Duration.ofMinutes(5));
        String taskJson = gson.toJson(task1);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        client.send(request, HttpResponse.BodyHandlers.ofString());
        task1.setId(1);

        LocalDateTime startTime2 = LocalDateTime.of(2024, 10, 1, 1, 1);
        Task task2 = new Task("Task2", "Testing task2", startTime2,
                Duration.ofMinutes(6));
        taskJson = gson.toJson(task2);
        request = HttpRequest.newBuilder()
                .uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        task2.setId(2);

        // проверяем код ответа при пересечении времени
        assertEquals(406, response.statusCode());

        // проверяем, что создано корректное количество задач и получаем список созданных задач
        List<Task> tasksFromManager = checkTaskFromManager(1);

        // проверяем идентичность задач
        checkTaskEquality(task1, tasksFromManager.getFirst());
    }

    @Test
    public void testPostTaskById() throws IOException, InterruptedException {
        LocalDateTime startTime = LocalDateTime.of(2024, 10, 1, 1, 0);
        Task task = new Task("Task1", "Testing task1", startTime,
                Duration.ofMinutes(5));
        String taskJson = gson.toJson(task);
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        client.send(request, HttpResponse.BodyHandlers.ofString());

        task.setId(1);
        task.setStatus(Status.DONE);

        taskJson = gson.toJson(task);
        request = HttpRequest.newBuilder()
                .uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа
        assertEquals(201, response.statusCode());

        // проверяем, что создано и обновлено корректное количество задач и получаем список созданных задач
        List<Task> tasksFromManager = checkTaskFromManager(1);

        // проверяем идентичность задач
        checkTaskEquality(task, tasksFromManager.getFirst());
    }

    @Test
    public void testPostTaskErrById() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks");
        LocalDateTime startTime = LocalDateTime.of(2024, 10, 1, 1, 0);
        Task task = new Task("Task1", "Testing task1", startTime,
                Duration.ofMinutes(5));
        String taskJson = gson.toJson(task);
        task.setId(1);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        client.send(request, HttpResponse.BodyHandlers.ofString());

        task.setId(2);
        task.setStatus(Status.DONE);

        taskJson = gson.toJson(task);
        request = HttpRequest.newBuilder()
                .uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа
        assertEquals(404, response.statusCode());
    }

    @Test
    public void testPutTaskById() throws IOException, InterruptedException {
        LocalDateTime startTime = LocalDateTime.of(2024, 10, 1, 1, 0);
        Task task = new Task("Task1", "Testing task1", startTime,
                Duration.ofMinutes(5));
        String taskJson = gson.toJson(task);
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        client.send(request, HttpResponse.BodyHandlers.ofString());

        task.setId(1);
        task.setStatus(Status.DONE);

        taskJson = gson.toJson(task);
        url = URI.create("http://localhost:8080/tasks?id=1");
        request = HttpRequest.newBuilder()
                .uri(url).PUT(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа
        assertEquals(201, response.statusCode());

        // проверяем, что создано и обновлено корректное количество задач и получаем список созданных задач
        List<Task> tasksFromManager = checkTaskFromManager(1);

        // проверяем идентичность задач
        checkTaskEquality(task, tasksFromManager.getFirst());
    }

    @Test
    public void testPutErrTaskById() throws IOException, InterruptedException {
        LocalDateTime startTime = LocalDateTime.of(2024, 10, 1, 1, 0);
        Task task = new Task("Task1", "Testing task1", startTime,
                Duration.ofMinutes(5));
        String taskJson = gson.toJson(task);
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        client.send(request, HttpResponse.BodyHandlers.ofString());

        task.setStatus(Status.DONE);

        taskJson = gson.toJson(task);
        url = URI.create("http://localhost:8080/tasks?id=2");
        request = HttpRequest.newBuilder()
                .uri(url).PUT(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа
        assertEquals(404, response.statusCode());
    }

    @Test
    public void testGetTasks() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks");

        LocalDateTime startTime1 = LocalDateTime.of(2024, 10, 1, 1, 0);
        Task task1 = new Task("Task1", "Testing task1", startTime1,
                Duration.ofMinutes(5));
        String taskJson = gson.toJson(task1);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        client.send(request, HttpResponse.BodyHandlers.ofString());

        task1.setId(1);

        LocalDateTime startTime2 = LocalDateTime.of(2024, 10, 1, 5, 1);
        Task task2 = new Task("Task2", "Testing task2", startTime2,
                Duration.ofMinutes(6));
        taskJson = gson.toJson(task2);
        request = HttpRequest.newBuilder()
                .uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        client.send(request, HttpResponse.BodyHandlers.ofString());

        task2.setId(2);

        request = HttpRequest.newBuilder()
                .uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа
        assertEquals(200, response.statusCode());

        // проверяем, что создано и обновлено корректное количество задач и получаем список созданных задач
        List<Task> tasksFromManager = checkTaskFromManager(2);

        // проверяем идентичность задач
        checkTaskEquality(task1, tasksFromManager.get(0));
        checkTaskEquality(task2, tasksFromManager.get(1));
    }

    @Test
    public void testGetTasksById() throws IOException, InterruptedException {
        LocalDateTime startTime1 = LocalDateTime.of(2024, 10, 1, 1, 0);
        Task task = new Task("Task1", "Testing task1", startTime1,
                Duration.ofMinutes(5));
        String taskJson = gson.toJson(task);
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        client.send(request, HttpResponse.BodyHandlers.ofString());

        task.setId(1);

        url = URI.create("http://localhost:8080/tasks?id=1");
        request = HttpRequest.newBuilder()
                .uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа
        assertEquals(200, response.statusCode());

        // проверяем, что создано корректное количество задач и получаем список созданных задач
        List<Task> tasksFromManager = checkTaskFromManager(1);

        // проверяем идентичность задач
        checkTaskEquality(task, tasksFromManager.getFirst());
    }

    @Test
    public void testGetErrTasksById() throws IOException, InterruptedException {
        LocalDateTime startTime1 = LocalDateTime.of(2024, 10, 1, 1, 0);
        Task task = new Task("Task1", "Testing task1", startTime1,
                Duration.ofMinutes(5));
        String taskJson = gson.toJson(task);
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        client.send(request, HttpResponse.BodyHandlers.ofString());

        task.setId(1);

        url = URI.create("http://localhost:8080/tasks?id=2");
        request = HttpRequest.newBuilder()
                .uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа
        assertEquals(404, response.statusCode());
    }

    @Test
    public void testDeleteTaskById() throws IOException, InterruptedException {
        LocalDateTime startTime1 = LocalDateTime.of(2024, 10, 1, 1, 0);
        Task task1 = new Task("Task1", "Testing task1", startTime1,
                Duration.ofMinutes(5));
        String taskJson = gson.toJson(task1);
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        client.send(request, HttpResponse.BodyHandlers.ofString());

        task1.setId(1);

        LocalDateTime startTime2 = LocalDateTime.of(2024, 10, 1, 10, 1);
        Task task2 = new Task("Task2", "Testing task2", startTime2,
                Duration.ofMinutes(6));
        taskJson = gson.toJson(task2);
        request = HttpRequest.newBuilder()
                .uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        client.send(request, HttpResponse.BodyHandlers.ofString());

        task2.setId(2);

        //удаляем первую задачу
        url = URI.create("http://localhost:8080/tasks?id=1");
        request = HttpRequest.newBuilder()
                .uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа
        assertEquals(200, response.statusCode());

        // проверяем, что осталось корректное количество задач и получаем список созданных задач
        List<Task> tasksFromManager = checkTaskFromManager(1);

        // проверяем идентичность задач
        checkTaskEquality(task2, tasksFromManager.getFirst());
    }

    private void checkTaskEquality(Task expected, Task actual) {
        assertEquals(expected.getId(), actual.getId(), "ID задач не совпадают");
        assertEquals(expected.getType(), actual.getType(), "Типы задач не совпадают");
        assertEquals(expected.getName(), actual.getName(), "Некорректное имя задачи");
        assertEquals(expected.getDescription(), actual.getDescription(), "Некорректное описание");
        assertEquals(expected.getStatus(), actual.getStatus(), "Статусы задач не совпадают");

        // Проверяем Duration
        Duration taskDuration = actual.getDuration();
        assertNotNull(taskDuration, "Продолжительность задачи не должна быть null");
        assertEquals(expected.getDuration(), taskDuration, "Duration задач не совпадают");

        // Проверяем LocalDateTime
        LocalDateTime taskStartTime = actual.getStartTime();
        assertNotNull(taskStartTime, "Время начала задачи не должно быть null");
        assertEquals(expected.getStartTime(), taskStartTime, "StartTime задач не совпадают");
    }

    private List<Task> checkTaskFromManager(int count) {
        List<Task> tasksFromManager = manager.getAllTasks();
        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(count, tasksFromManager.size(), "Некорректное количество задач");
        return tasksFromManager;
    }
}
