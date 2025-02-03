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

import static org.junit.jupiter.api.Assertions.*;

public class PrioritizedHandlerTest {
    TaskManager manager = new InMemoryTaskManager(Managers.getDefaultHistory());
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    Gson gson = HttpTaskServer.getGson();
    HttpClient client;

    public PrioritizedHandlerTest() throws IOException {
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
    public void testPrioritized() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks");

        LocalDateTime startTime1 = LocalDateTime.of(2024, 10, 1, 5, 0);
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
        client.send(request, HttpResponse.BodyHandlers.ofString());

        task2.setId(2);

        url = URI.create("http://localhost:8080/prioritized");
        request = HttpRequest.newBuilder()
                .uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа
        assertEquals(200, response.statusCode());

        List<Task> tasksFromManager = manager.getPrioritizedTasks();
        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(2, tasksFromManager.size(), "Некорректное количество задач");

        // проверяем идентичность задач
        assertTrue(tasksFromManager.contains(task1), "Задача 1 не найдена в списке сортировки по времени");
        assertTrue(tasksFromManager.contains(task2), "Задача 2 не найдена в списке сортировки по времени");

        // Проверяем сортировку по времени
        LocalDateTime previousStartTime = null;
        for (Task task : tasksFromManager) {
            if (previousStartTime != null) {
                assertTrue(task.getStartTime().isAfter(previousStartTime),
                        "Задачи не отсортированы по времени. Задача '" + task.getId() +
                                " должна быть позже.");
            }
            previousStartTime = task.getStartTime();
        }
    }
}
