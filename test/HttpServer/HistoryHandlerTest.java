package HttpServer;

import com.google.gson.Gson;
import httpserver.server.HttpTaskServer;
import manager.InMemoryTaskManager;
import manager.Managers;
import manager.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

public class HistoryHandlerTest {
    TaskManager manager = new InMemoryTaskManager(Managers.getDefaultHistory());
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    Gson gson = HttpTaskServer.getGson();
    HttpClient client;

    public HistoryHandlerTest() throws IOException {
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
    public void testHistory() throws IOException, InterruptedException {
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

        url = URI.create("http://localhost:8080/tasks?id=1");
        request = HttpRequest.newBuilder()
                .uri(url).GET().build();
        client.send(request, HttpResponse.BodyHandlers.ofString());

        url = URI.create("http://localhost:8080/tasks?id=2");
        request = HttpRequest.newBuilder()
                .uri(url).GET().build();
        client.send(request, HttpResponse.BodyHandlers.ofString());

        url = URI.create("http://localhost:8080/history");
        request = HttpRequest.newBuilder()
                .uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа
        assertEquals(200, response.statusCode());

        List<Task> tasksFromManager = manager.getHistory();
        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(2, tasksFromManager.size(), "Некорректное количество задач");

        // проверяем идентичность задач
        checkTaskEquality(task1, tasksFromManager.get(0));
        checkTaskEquality(task2, tasksFromManager.get(1));
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
}