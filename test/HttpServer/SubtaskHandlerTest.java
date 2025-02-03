package HttpServer;

import com.google.gson.Gson;
import httpserver.server.HttpTaskServer;
import manager.InMemoryTaskManager;
import manager.Managers;
import manager.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.Status;
import task.Subtask;

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

public class SubtaskHandlerTest {
    TaskManager manager = new InMemoryTaskManager(Managers.getDefaultHistory());
    // передаём его в качестве аргумента в конструктор HttpTaskServer
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    Gson gson = HttpTaskServer.getGson();
    HttpClient client;

    public SubtaskHandlerTest() throws IOException {
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
    public void testPostSubtask() throws IOException, InterruptedException {
        Epic epic1 = createEpic();

        LocalDateTime startTimeId2 = LocalDateTime.of(2024, 10, 1, 10, 0);
        Subtask subtaskId2 = new Subtask("Subtask2", "Testing subtask2", startTimeId2,
                Duration.ofMinutes(5), epic1.getId());
        String taskJson = gson.toJson(subtaskId2);
        subtaskId2.setId(2);
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа
        assertEquals(201, response.statusCode());

        // проверяем, что создано корректное количество подзадач и получаем список созданных подзадач
        List<Subtask> tasksFromManager = checkTaskFromManager(1);

        // проверяем идентичность подзадач
        checkSubtaskEquality(subtaskId2, tasksFromManager.getFirst());
    }

    @Test
    public void testPostErrValidateSubtask() throws IOException, InterruptedException {
        Epic epic1 = createEpic();

        LocalDateTime startTimeId2 = LocalDateTime.of(2024, 10, 1, 10, 0);
        Subtask subtaskId2 = new Subtask("Subtask1", "Testing subtask1", startTimeId2,
                Duration.ofMinutes(5), epic1.getId());
        String taskJson = gson.toJson(subtaskId2);
        subtaskId2.setId(2);
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        client.send(request, HttpResponse.BodyHandlers.ofString());

        LocalDateTime startTimeId3 = LocalDateTime.of(2024, 10, 1, 10, 1);
        Subtask subtaskId3 = new Subtask("Subtask2", "Testing subtask2", startTimeId3,
                Duration.ofMinutes(3), epic1.getId());
        taskJson = gson.toJson(subtaskId3);
        subtaskId3.setId(3);
        request = HttpRequest.newBuilder()
                .uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа при пересечении времени
        assertEquals(406, response.statusCode());

        // проверяем, что создано корректное количество подзадач и получаем список созданных подзадач
        List<Subtask> tasksFromManager = checkTaskFromManager(1);

        // проверяем идентичность подзадач
        checkSubtaskEquality(subtaskId2, tasksFromManager.getFirst());
    }

    @Test
    public void testPostSubtaskById() throws IOException, InterruptedException {
        Epic epic1 = createEpic();

        LocalDateTime startTimeId2 = LocalDateTime.of(2024, 10, 1, 10, 0);
        Subtask subtaskId2 = new Subtask("Subtask1", "Testing subtask1", startTimeId2,
                Duration.ofMinutes(5), epic1.getId());
        String taskJson = gson.toJson(subtaskId2);
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        client.send(request, HttpResponse.BodyHandlers.ofString());

        subtaskId2.setId(2);
        subtaskId2.setStatus(Status.DONE);

        taskJson = gson.toJson(subtaskId2);
        request = HttpRequest.newBuilder()
                .uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа
        assertEquals(201, response.statusCode());

        // проверяем, что создано и обновлено корректное количество подзадач и получаем список созданных подзадач
        List<Subtask> tasksFromManager = checkTaskFromManager(1);

        // проверяем идентичность подзадач
        checkSubtaskEquality(subtaskId2, tasksFromManager.getFirst());
    }

    @Test
    public void testPostSubtaskErrById() throws IOException, InterruptedException {
        Epic epic1 = createEpic();

        LocalDateTime startTimeId2 = LocalDateTime.of(2024, 10, 1, 10, 0);
        Subtask subtaskId2 = new Subtask(0, "Subtask1", "Testing subtask1", Status.NEW, startTimeId2,
                Duration.ofMinutes(5), epic1.getId());

        String taskJson = gson.toJson(subtaskId2);
        subtaskId2.setId(2);
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        client.send(request, HttpResponse.BodyHandlers.ofString());

        subtaskId2.setId(3);
        subtaskId2.setStatus(Status.DONE);

        taskJson = gson.toJson(subtaskId2);
        request = HttpRequest.newBuilder()
                .uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа
        assertEquals(404, response.statusCode());
    }

    @Test
    public void testPutSubtaskById() throws IOException, InterruptedException {
        Epic epic1 = createEpic();

        LocalDateTime startTimeId2 = LocalDateTime.of(2024, 10, 1, 10, 0);
        Subtask subtaskId2 = new Subtask(0, "Subtask1", "Testing subtask1", Status.NEW, startTimeId2,
                Duration.ofMinutes(5), epic1.getId());

        String taskJson = gson.toJson(subtaskId2);
        subtaskId2.setId(2);
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        client.send(request, HttpResponse.BodyHandlers.ofString());

        subtaskId2.setId(2);
        subtaskId2.setStatus(Status.DONE);

        taskJson = gson.toJson(subtaskId2);
        url = URI.create("http://localhost:8080/subtasks?id=2");
        request = HttpRequest.newBuilder()
                .uri(url).PUT(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа
        assertEquals(201, response.statusCode());

        // проверяем, что создано и обновлено корректное количество подзадач и получаем список созданных подзадач
        List<Subtask> tasksFromManager = checkTaskFromManager(1);

        // проверяем идентичность подзадач
        checkSubtaskEquality(subtaskId2, tasksFromManager.getFirst());
    }

    @Test
    public void testPutErrSubtaskById() throws IOException, InterruptedException {
        Epic epic1 = createEpic();

        LocalDateTime startTimeId2 = LocalDateTime.of(2024, 10, 1, 10, 0);
        Subtask subtaskId2 = new Subtask(0, "Subtask1", "Testing subtask1", Status.NEW, startTimeId2,
                Duration.ofMinutes(5), epic1.getId());

        String taskJson = gson.toJson(subtaskId2);
        subtaskId2.setId(2);
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        client.send(request, HttpResponse.BodyHandlers.ofString());
        subtaskId2.setStatus(Status.DONE);

        taskJson = gson.toJson(subtaskId2);
        url = URI.create("http://localhost:8080/subtasks?id=3");
        request = HttpRequest.newBuilder().uri(url).PUT(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа
        assertEquals(404, response.statusCode());
    }

    @Test
    public void testGetSubtasks() throws IOException, InterruptedException {
        Epic epic1 = createEpic();

        LocalDateTime startTimeId2 = LocalDateTime.of(2024, 10, 1, 10, 0);
        Subtask subtaskId2 = new Subtask("Subtask1", "Testing subtask1", startTimeId2,
                Duration.ofMinutes(5), epic1.getId());
        String taskJson = gson.toJson(subtaskId2);
        subtaskId2.setId(2);
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        client.send(request, HttpResponse.BodyHandlers.ofString());

        LocalDateTime startTimeId3 = LocalDateTime.of(2024, 10, 1, 10, 20);
        Subtask subtaskId3 = new Subtask("Subtask2", "Testing subtask2", startTimeId3,
                Duration.ofMinutes(3), epic1.getId());
        taskJson = gson.toJson(subtaskId3);
        subtaskId3.setId(3);
        request = HttpRequest.newBuilder()
                .uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        client.send(request, HttpResponse.BodyHandlers.ofString());

        request = HttpRequest.newBuilder()
                .uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа
        assertEquals(200, response.statusCode());

        // проверяем, что создано корректное количество подзадач и получаем список созданных подзадач
        List<Subtask> tasksFromManager = checkTaskFromManager(2);

        // проверяем идентичность подзадач
        checkSubtaskEquality(subtaskId2, tasksFromManager.get(0));
        checkSubtaskEquality(subtaskId3, tasksFromManager.get(1));
    }

    @Test
    public void testGetSubtasksById() throws IOException, InterruptedException {
        Epic epic1 = createEpic();

        LocalDateTime startTimeId2 = LocalDateTime.of(2024, 10, 1, 10, 0);
        Subtask subtaskId2 = new Subtask(0, "Subtask1", "Testing subtask1", Status.NEW, startTimeId2,
                Duration.ofMinutes(5), epic1.getId());

        String taskJson = gson.toJson(subtaskId2);
        subtaskId2.setId(2);
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        client.send(request, HttpResponse.BodyHandlers.ofString());

        url = URI.create("http://localhost:8080/subtasks?id=2");
        request = HttpRequest.newBuilder()
                .uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа
        assertEquals(200, response.statusCode());

        // проверяем, что создано корректное количество подзадач и получаем список созданных подзадач
        List<Subtask> tasksFromManager = checkTaskFromManager(1);

        // проверяем идентичность подзадач
        checkSubtaskEquality(subtaskId2, tasksFromManager.getFirst());
    }

    @Test
    public void testGetErrSubtasksById() throws IOException, InterruptedException {
        Epic epic1 = createEpic();

        LocalDateTime startTimeId2 = LocalDateTime.of(2024, 10, 1, 10, 0);
        Subtask subtaskId2 = new Subtask(0, "Subtask1", "Testing subtask1", Status.NEW, startTimeId2,
                Duration.ofMinutes(5), epic1.getId());

        String taskJson = gson.toJson(subtaskId2);
        subtaskId2.setId(2);
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        client.send(request, HttpResponse.BodyHandlers.ofString());

        url = URI.create("http://localhost:8080/subtasks?id=3");
        request = HttpRequest.newBuilder()
                .uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа
        assertEquals(404, response.statusCode());
    }

    @Test
    public void testDeleteSubtaskById() throws IOException, InterruptedException {
        Epic epic1 = createEpic();

        LocalDateTime startTimeId2 = LocalDateTime.of(2024, 10, 1, 10, 0);
        Subtask subtaskId2 = new Subtask(0, "Subtask1", "Testing subtask1", Status.NEW, startTimeId2,
                Duration.ofMinutes(5), epic1.getId());
        String taskJson = gson.toJson(subtaskId2);
        subtaskId2.setId(2);
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        client.send(request, HttpResponse.BodyHandlers.ofString());

        LocalDateTime startTimeId3 = LocalDateTime.of(2024, 10, 1, 10, 20);
        Subtask subtaskId3 = new Subtask("Subtask2", "Testing subtask2", startTimeId3,
                Duration.ofMinutes(3), epic1.getId());
        taskJson = gson.toJson(subtaskId3);
        subtaskId3.setId(3);
        request = HttpRequest.newBuilder()
                .uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        client.send(request, HttpResponse.BodyHandlers.ofString());

        //удаляем первую подзадачу
        url = URI.create("http://localhost:8080/subtasks?id=2");
        request = HttpRequest.newBuilder()
                .uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа
        assertEquals(200, response.statusCode());

        // проверяем, что осталась только одна корректная задача
        List<Subtask> tasksFromManager = checkTaskFromManager(1);

        // проверяем идентичность подзадач
        checkSubtaskEquality(subtaskId3, tasksFromManager.getFirst());
    }

    private void checkSubtaskEquality(Subtask expected, Subtask actual) {
        assertEquals(expected.getId(), actual.getId(), "ID подзадач не совпадают");
        assertEquals(expected.getType(), actual.getType(), "Типы подзадач не совпадают");
        assertEquals(expected.getName(), actual.getName(), "Некорректное имя подзадачи");
        assertEquals(expected.getDescription(), actual.getDescription(), "Некорректное описание подзадачи");
        assertEquals(expected.getStatus(), actual.getStatus(), "Статусы подзадач не совпадают");
        assertEquals(expected.getParentId(), actual.getParentId(), "ID эпиков у подзадач не совпадают");

        // Проверяем Duration
        Duration taskDuration = actual.getDuration();
        assertNotNull(taskDuration, "Продолжительность подзадачи не должна быть null");
        assertEquals(expected.getDuration(), taskDuration, "Duration подзадач не совпадают");

        // Проверяем LocalDateTime
        LocalDateTime taskStartTime = actual.getStartTime();
        assertNotNull(taskStartTime, "Время начала подзадачи не должно быть null");
        assertEquals(expected.getStartTime(), taskStartTime, "StartTime подзадач не совпадают");
    }

    private Epic createEpic() throws IOException, InterruptedException {
        LocalDateTime startTime = LocalDateTime.now();
        Epic epic = new Epic(0, "Epic", "Testing epic", Status.NEW,
                startTime, Duration.ZERO, startTime.plus(Duration.ZERO));

        String taskJson = gson.toJson(epic);
        epic.setId(1);
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        client.send(request, HttpResponse.BodyHandlers.ofString());
        return epic;
    }

    private List<Subtask> checkTaskFromManager(int count) {
        List<Subtask> tasksFromManager = manager.getAllSubtasks();
        assertNotNull(tasksFromManager, "Подзадачи не возвращаются");
        assertEquals(count, tasksFromManager.size(), "Некорректное количество подзадач");
        return tasksFromManager;
    }
}
