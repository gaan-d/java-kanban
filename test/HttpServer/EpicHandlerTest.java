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

public class EpicHandlerTest {
    // создаём экземпляр InMemoryTaskManager
    TaskManager manager = new InMemoryTaskManager(Managers.getDefaultHistory());
    // передаём его в качестве аргумента в конструктор HttpTaskServer
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    Gson gson = HttpTaskServer.getGson();
    HttpClient client;

    public EpicHandlerTest() throws IOException {
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
    public void testPostEpic() throws IOException, InterruptedException {
        LocalDateTime startTime1 = LocalDateTime.of(2024, 10, 1, 1, 0);
        Epic epic1 = new Epic(0, "Epic1", "Testing Epic1", Status.NEW, startTime1,
                Duration.ofHours(2), startTime1.plus(Duration.ofHours(2)));
        String taskJson = gson.toJson(epic1);

        //устанавливаем ID в ручную для последующей проверки,
        // т.к. ID устанавливается непосредственно менеджерами, а не конструкторами.
        epic1.setId(1);

        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа
        assertEquals(201, response.statusCode());

        // проверяем, что создано корректное количество эпиков и получаем список созданных эпиков
        List<Epic> tasksFromManager = checkTaskFromManager(1);

        //вызываем метод проверки идентичности задач
        checkTaskEquality(epic1, tasksFromManager.getFirst());
    }

    @Test
    public void testPutEpicById() throws IOException, InterruptedException {
        LocalDateTime startTime1 = LocalDateTime.of(2024, 10, 1, 1, 0);
        Epic epic1 = new Epic(0, "Epic1", "Testing epic1", Status.NEW, startTime1,
                Duration.ofHours(2), startTime1.plus(Duration.ofHours(2)));
        String taskJson = gson.toJson(epic1);
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        client.send(request, HttpResponse.BodyHandlers.ofString());

        epic1.setId(1);
        epic1.setName("EpicNew");

        taskJson = gson.toJson(epic1);
        url = URI.create("http://localhost:8080/epics?id=1");
        request = HttpRequest.newBuilder()
                .uri(url).PUT(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа
        assertEquals(201, response.statusCode());

        // проверяем, что создано и обновлено корректное количество эпиков и получаем список созданных эпиков
        List<Epic> tasksFromManager = checkTaskFromManager(1);

        //вызываем метод проверки идентичности задач
        checkTaskEquality(epic1, tasksFromManager.getFirst());
    }

    @Test
    public void testPutErrEpicById() throws IOException, InterruptedException {
        LocalDateTime startTime1 = LocalDateTime.of(2024, 10, 1, 1, 0);
        Epic epic1 = new Epic("Epic1", "Testing epic1", Status.NEW, startTime1,
                Duration.ofHours(2), startTime1.plus(Duration.ofHours(2)));

        String taskJson = gson.toJson(epic1);
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        client.send(request, HttpResponse.BodyHandlers.ofString());

        epic1.setName("EpicNew");

        taskJson = gson.toJson(epic1);
        url = URI.create("http://localhost:8080/epics?id=2");
        request = HttpRequest.newBuilder()
                .uri(url).PUT(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Ответ сервера: " + response.body());
        // проверяем код ответа
        assertEquals(404, response.statusCode());
    }

    @Test
    public void testGetEpics() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/epics");

        LocalDateTime startTime1 = LocalDateTime.of(2024, 10, 1, 1, 0);
        Epic epic1 = new Epic(0, "Epic1", "Testing epic1", Status.NEW, startTime1,
                Duration.ofHours(2), startTime1.plus(Duration.ofHours(2)));
        String taskJson = gson.toJson(epic1);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        client.send(request, HttpResponse.BodyHandlers.ofString());

        epic1.setId(1);

        LocalDateTime startTime2 = LocalDateTime.of(2024, 10, 1, 5, 1);
        Epic epic2 = new Epic(0, "Epic2", "Testing epic2", Status.NEW, startTime2,
                Duration.ofHours(2), startTime2.plus(Duration.ofHours(2)));
        taskJson = gson.toJson(epic2);
        request = HttpRequest.newBuilder()
                .uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        client.send(request, HttpResponse.BodyHandlers.ofString());

        epic2.setId(2);

        request = HttpRequest.newBuilder()
                .uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа
        assertEquals(200, response.statusCode());

        // проверяем, что создано корректное количество эпиков и получаем список созданных эпиков
        List<Epic> tasksFromManager = checkTaskFromManager(2);

        //вызываем метод проверки идентичности задач
        checkTaskEquality(epic1, tasksFromManager.get(0));

        checkTaskEquality(epic2, tasksFromManager.get(1));
    }

    @Test
    public void testGetEpicsById() throws IOException, InterruptedException {
        LocalDateTime startTime1 = LocalDateTime.of(2024, 10, 1, 1, 0);
        Epic epic1 = new Epic(0, "Task1", "Testing epic1", Status.NEW, startTime1,
                Duration.ofHours(2), startTime1.plus(Duration.ofHours(2)));

        String taskJson = gson.toJson(epic1);
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        client.send(request, HttpResponse.BodyHandlers.ofString());

        epic1.setId(1);

        url = URI.create("http://localhost:8080/epics?id=1");
        request = HttpRequest.newBuilder()
                .uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа
        assertEquals(200, response.statusCode());

        // проверяем, что создано корректное количество эпиков и получаем список созданных эпиков
        List<Epic> tasksFromManager = checkTaskFromManager(1);

        //вызываем метод проверки идентичности задач
        checkTaskEquality(epic1, tasksFromManager.getFirst());
    }

    @Test
    public void testGetErrEpicsById() throws IOException, InterruptedException {
        LocalDateTime startTime1 = LocalDateTime.of(2024, 10, 1, 1, 0);
        Epic epic1 = new Epic(0, "Epic1", "Testing epic1", Status.NEW, startTime1,
                Duration.ofHours(2), startTime1.plus(Duration.ofHours(2)));

        String taskJson = gson.toJson(epic1);
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        client.send(request, HttpResponse.BodyHandlers.ofString());

        epic1.setId(1);

        url = URI.create("http://localhost:8080/epics?id=2");
        request = HttpRequest.newBuilder()
                .uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа
        assertEquals(404, response.statusCode());
    }

    @Test
    public void testGetEpicsByIdSubtasks() throws IOException, InterruptedException {
        LocalDateTime startTime1 = LocalDateTime.of(2024, 10, 1, 1, 0);
        Epic epic1 = new Epic(0, "Task1", "Testing epic1", Status.NEW, startTime1,
                Duration.ofHours(2), startTime1.plus(Duration.ofHours(2)));

        String taskJson = gson.toJson(epic1);
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        client.send(request, HttpResponse.BodyHandlers.ofString());

        epic1.setId(1);

        LocalDateTime startTimeId2 = LocalDateTime.of(2024, 10, 1, 10, 0);
        Subtask subtaskId2 = new Subtask(0, "Subtask1", "Testing subtask1", Status.DONE, startTimeId2,
                Duration.ofMinutes(5), epic1.getId());
        taskJson = gson.toJson(subtaskId2);
        subtaskId2.setId(2);
        url = URI.create("http://localhost:8080/subtasks");
        request = HttpRequest.newBuilder()
                .uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        client.send(request, HttpResponse.BodyHandlers.ofString());

        url = URI.create("http://localhost:8080/epics?id=1/subtasks");
        request = HttpRequest.newBuilder()
                .uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа
        assertEquals(200, response.statusCode());

        // проверяем, что создано корректное количество подзадач и получаем список созданных подзадач
        List<Subtask> tasksFromManager = manager.getEpicSubtasks(epic1.getId());
        assertNotNull(tasksFromManager, "Подзадачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество подзадач");

        //вызываем метод проверки идентичности задач
        assertEquals(subtaskId2.getId(), tasksFromManager.getFirst().getId(), "ID подзадач не совпадают");
        assertEquals(subtaskId2.getType(), tasksFromManager.getFirst().getType(), "Типы подзадач не совпадают");
        assertEquals(subtaskId2.getName(), tasksFromManager.getFirst().getName(), "Некорректное имя подзадачи");
        assertEquals(subtaskId2.getDescription(), tasksFromManager.getFirst().getDescription(), "Некорректное описание подзадачи");
        assertEquals(subtaskId2.getStatus(), tasksFromManager.getFirst().getStatus(), "Статусы подзадач не совпадают");
        assertEquals(subtaskId2.getParentId(), tasksFromManager.getFirst().getParentId(), "ID эпиков у подзадач не совпадают");

        // Проверяем Duration
        Duration taskDuration = tasksFromManager.getFirst().getDuration();
        assertNotNull(taskDuration, "Продолжительность подзадачи не должна быть null");
        assertEquals(subtaskId2.getDuration(), taskDuration, "Duration подзадач не совпадают");

        // Проверяем LocalDateTime
        LocalDateTime taskStartTime = tasksFromManager.getFirst().getStartTime();
        assertNotNull(taskStartTime, "Время начала подзадачи не должно быть null");
        assertEquals(subtaskId2.getStartTime(), taskStartTime, "StartTime подзадач не совпадают");
    }

    @Test
    public void testDeleteEpicById() throws IOException, InterruptedException {
        LocalDateTime startTime1 = LocalDateTime.of(2024, 10, 1, 1, 0);
        Epic epic1 = new Epic(0, "Epic1", "Testing epic1", Status.NEW, startTime1,
                Duration.ofHours(2), startTime1.plus(Duration.ofHours(2)));

        String taskJson = gson.toJson(epic1);
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        client.send(request, HttpResponse.BodyHandlers.ofString());

        epic1.setId(1);

        LocalDateTime startTime2 = LocalDateTime.of(2024, 10, 1, 20, 1);
        Epic epic2 = new Epic(0, "Epic2", "Testing epic2", Status.NEW, startTime2,
                Duration.ofHours(2), startTime2.plus(Duration.ofHours(2)));

        taskJson = gson.toJson(epic2);
        request = HttpRequest.newBuilder()
                .uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        client.send(request, HttpResponse.BodyHandlers.ofString());

        epic2.setId(2);

        //удаляем первую задачу
        url = URI.create("http://localhost:8080/epics?id=1");
        request = HttpRequest.newBuilder()
                .uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа
        assertEquals(200, response.statusCode());

        // проверяем, что создано и обновлено корректное количество эпиков и получаем список созданных эпиков
        List<Epic> tasksFromManager = checkTaskFromManager(1);

        //вызываем метод проверки идентичности задач
        checkTaskEquality(epic2, tasksFromManager.getFirst());
    }

    private void checkTaskEquality(Epic expected, Epic actual) {
        assertEquals(expected.getId(), actual.getId(), "ID эпиков не совпадают");
        assertEquals(expected.getType(), actual.getType(), "Типы эпиков не совпадают");
        assertEquals(expected.getName(), actual.getName(), "Некорректное имя эпика");
        assertEquals(expected.getDescription(), actual.getDescription(), "Некорректное описание эпика");
        assertEquals(expected.getStatus(), actual.getStatus(), "Статусы эпиков не совпадают");

        // Проверяем Duration
        Duration taskDuration = actual.getDuration();
        assertNotNull(taskDuration, "Продолжительность эпика не должна быть null");
        assertEquals(expected.getDuration(), taskDuration, "Duration эпиков не совпадают");

        // Проверяем LocalDateTime
        LocalDateTime taskStartTime = actual.getStartTime();
        assertNotNull(taskStartTime, "Время начала эпика не должно быть null");
        assertEquals(expected.getStartTime(), taskStartTime, "StartTime эпиков не совпадают");
    }

    private List<Epic> checkTaskFromManager(int count) {
        List<Epic> tasksFromManager = manager.getAllEpics();
        assertNotNull(tasksFromManager, "Эпики не возвращаются");
        assertEquals(count, tasksFromManager.size(), "Некорректное количество эпиков");
        return tasksFromManager;
    }
}
