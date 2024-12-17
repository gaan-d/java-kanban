import manager.FileBackedTaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.Status;
import task.Subtask;
import task.Task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {
    private FileBackedTaskManager fileBackedTaskManager;
    private Task task;
    private Epic epic;
    private File file;

    @BeforeEach
    void setUp() throws IOException {
        Path path = Files.createTempFile("file_auto-", ".csv");
        file = new File(String.valueOf(path));
        fileBackedTaskManager = new FileBackedTaskManager(file);
        task = new Task("Просто задача - 1", "Описание простой задачи - 1");
        epic = new Epic("Эпическая задача - 1",
                "Описание эпической задачи - 1");
    }

    @Test
    void addNewTaskTest() {
        final int taskId = fileBackedTaskManager.addNewTask(task);
        final Task savedTask = fileBackedTaskManager.getTask(taskId);

        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask, "Задачи не совпадают.");

        final List<Task> tasks = fileBackedTaskManager.getTasks();

        assertNotNull(tasks, "Задачи не возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(task, tasks.get(0), "Задачи не совпадают.");
    }

    @Test
    void loadFromFileTest() {
        final int taskId = fileBackedTaskManager.addNewTask(task);
        final int epicId = fileBackedTaskManager.addNewEpic(epic);
        final Subtask subtask = new Subtask("Подзадача - 1",
                "Описание подзадачи - 1 эпической задачи - 1", epicId);
        final int subtaskId = fileBackedTaskManager.addNewSubtask(subtask);

        final FileBackedTaskManager backedTaskManager2 = FileBackedTaskManager.loadFromFile(file);
        final Task taskLoad = backedTaskManager2.getTask(taskId);
        final Epic epicLoad = backedTaskManager2.getEpic(epicId);
        final Subtask subtaskLoad = backedTaskManager2.getSubtask(subtaskId);

        assertEquals(task.getId(), taskLoad.getId(), "ID задач не совпадают");
        assertEquals(task.getType(), taskLoad.getType(), "Типы задач не совпадают");
        assertEquals(task.getName(), taskLoad.getName(), "Title задач не совпадают");
        assertEquals(task.getDescription(), taskLoad.getDescription(), "Описания задач не совпадают");
        assertEquals(task.getStatus(), taskLoad.getStatus(), "Статусы задач не совпадают");

        assertEquals(epic.getId(), epicLoad.getId(), "ID эпиков не совпадают");
        assertEquals(epic.getType(), epicLoad.getType(), "Типы эпиков не совпадают");
        assertEquals(epic.getName(), epicLoad.getName(), "Title эпиков не совпадают");
        assertEquals(epic.getDescription(), epicLoad.getDescription(), "Описания эпиков не совпадают");
        assertEquals(epic.getStatus(), epicLoad.getStatus(), "Статусы эпиков не совпадают");
        assertEquals(epic.getSubtaskIds(), epicLoad.getSubtaskIds(), "ID подзадач у эпиков не совпадают");

        assertEquals(subtask.getId(), subtaskLoad.getId(), "ID подзадач не совпадают");
        assertEquals(subtask.getType(), subtaskLoad.getType(), "Типы подзадач не совпадают");
        assertEquals(subtask.getName(), subtaskLoad.getName(), "Title подзадач не совпадают");
        assertEquals(subtask.getDescription(), subtaskLoad.getDescription(), "Описания подзадач не совпадают");
        assertEquals(subtask.getStatus(), subtaskLoad.getStatus(), "Статусы подзадач не совпадают");
        assertEquals(subtask.getParentId(), subtaskLoad.getParentId(), "ID эпиков у подзадач не совпадают");
    }

    @Test
    void loadFromEmptyFileTest() {
        FileBackedTaskManager backedTaskManager2 = FileBackedTaskManager.loadFromFile(file);
        final List<Task> tasks = backedTaskManager2.getTasks();
        final List<Epic> epics = backedTaskManager2.getEpics();
        final List<Subtask> subtasks = backedTaskManager2.getSubtasks();

        assertEquals(0, tasks.size(), "Количество задач не верное");
        assertEquals(0, epics.size(), "Количество эпиков не верное");
        assertEquals(0, subtasks.size(), "Количество подзадач не верное");

    }

    @Test
    void updateTaskTest() {
        final int taskId = fileBackedTaskManager.addNewTask(task);
        final Task savedTask = fileBackedTaskManager.getTask(taskId);

        savedTask.setStatus(Status.DONE);
        fileBackedTaskManager.updateTask(savedTask);

        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask, "Задачи не совпадают.");
        assertEquals(task.getStatus(), savedTask.getStatus(), "Статусы задач не совпадают");
    }

    @Test
    void deleteTaskTest() {
        fileBackedTaskManager.addNewTask(task);
        fileBackedTaskManager.deleteTask(task.getId());

        assertTrue(fileBackedTaskManager.getTasks().isEmpty(), "Задача не удалилась");
        assertEquals(0, fileBackedTaskManager.getTasks().size(), "Задача не удалилась");
    }

    @Test
    void deleteAllTasksTest() {
        fileBackedTaskManager.addNewTask(task);
        fileBackedTaskManager.addNewTask(task);
        fileBackedTaskManager.deleteTasks();

        assertTrue(fileBackedTaskManager.getTasks().isEmpty(), "Задачи не удалились");
        assertEquals(0, fileBackedTaskManager.getTasks().size(), "Задачи не удалились");
    }

    @Test
    void addNewEpicTest() {
        final int epicId = fileBackedTaskManager.addNewEpic(epic);
        final Epic savedEpic = fileBackedTaskManager.getEpic(epicId);

        assertNotNull(savedEpic, "Эпик не найден.");
        assertEquals(epic, savedEpic, "Эпики не совпадают.");

        final List<Epic> epics = fileBackedTaskManager.getEpics();

        assertNotNull(epics, "Эпики не возвращаются.");
        assertEquals(1, epics.size(), "Неверное количество Эпиков.");
        assertEquals(epic, epics.get(0), "Эпики не совпадают.");
    }

    @Test
    void addNewSubtaskTest() {
        final int epicId = fileBackedTaskManager.addNewEpic(epic);
        final Subtask subtask = new Subtask("Подзадача - 1",
                "Описание подзадачи - 1, эпической задачи - 1", epicId);
        final int subtaskId = fileBackedTaskManager.addNewSubtask(subtask);
        final Subtask savedSubtask = fileBackedTaskManager.getSubtask(subtaskId);

        assertNotNull(savedSubtask, "Подзадача не найдена.");
        assertEquals(subtask, savedSubtask, "Подзадачи не совпадают.");

        final int savedEpicId = savedSubtask.getParentId();

        assertEquals(subtask.getParentId(), savedEpicId, "Эпики у подзадач не совпадают.");

        final List<Subtask> subtasks = fileBackedTaskManager.getSubtasks();

        assertNotNull(subtasks, "Подзадачи не возвращаются.");
        assertEquals(1, subtasks.size(), "Неверное количество подзадач.");
        assertEquals(subtask, subtasks.get(0), "Подзадачи не совпадают.");
    }

    @Test
    void updateEpicTest() {
        int epicId = fileBackedTaskManager.addNewEpic(epic);
        final Epic savedEpic = fileBackedTaskManager.getEpic(epicId);
        Epic epic2 = new Epic(epic.getId(), "Эпическая задача - 2", "Ставим вместо эпической задачи - 1", epic.getStatus());
        fileBackedTaskManager.updateEpic(epic2);

        assertNotNull(savedEpic, "Эпик не найден.");
        assertEquals(epic2, savedEpic, "Эпики не совпадают.");

        final List<Epic> epics = fileBackedTaskManager.getEpics();

        assertNotNull(epics, "Эпики на возвращаются.");
        assertEquals(1, epics.size(), "Неверное количество эпиков.");
        assertEquals(epic2, epics.get(0), "Эпики не совпадают.");
    }

    @Test
    void updateSubtaskAndEpicTest() {
        final int epicId = fileBackedTaskManager.addNewEpic(epic);
        final Subtask subtask = new Subtask("Подзадача - 1",
                "Описание подзадачи - 1, эпической задачи - 1", epicId);
        final int subtaskId = fileBackedTaskManager.addNewSubtask(subtask);
        final Subtask savedSubtask = fileBackedTaskManager.getSubtask(subtaskId);

        savedSubtask.setStatus(Status.DONE);
        fileBackedTaskManager.updateSubtask(subtask);

        assertNotNull(savedSubtask, "Подзадачи не найдена.");
        assertEquals(subtask, savedSubtask, "Подзадачи не совпадают.");

        final List<Subtask> subtasks = fileBackedTaskManager.getSubtasks();

        assertNotNull(subtasks, "Подзадачи на возвращаются.");
        assertEquals(1, subtasks.size(), "Неверное количество подзадач.");
        assertEquals(subtask, subtasks.get(0), "Подзадачи не совпадают.");
        assertEquals(fileBackedTaskManager.getEpic(epicId).getStatus(), savedSubtask.getStatus(), "Статусы подзадач не совпадают");
    }

    @Test
    void deleteEpicTest() {
        fileBackedTaskManager.addNewEpic(epic);
        fileBackedTaskManager.deleteEpic(epic.getId());

        assertTrue(fileBackedTaskManager.getEpics().isEmpty(), "Эпик не удалился");
        assertEquals(0, fileBackedTaskManager.getEpics().size(), "Эпик не удалился");
    }

    @Test
    void deleteAllEpicsTest() {
        fileBackedTaskManager.addNewEpic(epic);
        fileBackedTaskManager.addNewEpic(epic);
        fileBackedTaskManager.deleteEpics();

        assertTrue(fileBackedTaskManager.getEpics().isEmpty(), "Эпики не удалились");
        assertEquals(0, fileBackedTaskManager.getEpics().size(), "Эпики не удалились");
    }

    @Test
    void deleteSubtaskTest() {
        fileBackedTaskManager.addNewEpic(epic);
        Subtask subtask = new Subtask("Подзадача - 1",
                "Описание подзадачи - 1, эпической задачи - 1", epic.getId());
        Integer subtaskId = fileBackedTaskManager.addNewSubtask(subtask);

        assertEquals(1, epic.getSubtaskIds().size(), "ID подзадачи не зарегистрировался у эпика");

        fileBackedTaskManager.deleteSubtask(subtaskId);

        assertTrue(fileBackedTaskManager.getSubtasks().isEmpty(), "Подзадача не удалилася");
        assertEquals(0, fileBackedTaskManager.getSubtasks().size(), "Подзадача не удалилася");
        assertTrue(epic.getSubtaskIds().isEmpty(), "ID подзадачи не удалился из списка у эпика");
    }

    @Test
    void deleteAllSubtaskTest() {
        Epic epic = new Epic("Эпическая задача - 1", "Описание эпической задачи");
        fileBackedTaskManager.addNewEpic(epic);
        Subtask subtask = new Subtask("Подзадача - 1",
                "Описание подзадачи - 1, эпической задачи - 1", epic.getId());
        fileBackedTaskManager.addNewSubtask(subtask);
        fileBackedTaskManager.addNewSubtask(subtask);
        fileBackedTaskManager.deleteSubtasks();

        assertTrue(fileBackedTaskManager.getSubtasks().isEmpty(), "Подзадачи не удалилися");
        assertEquals(0, fileBackedTaskManager.getSubtasks().size(), "Подзадачи не удалилися");
    }

    @Test
    void updateStatusEpicTest() {
        final int epicId = fileBackedTaskManager.addNewEpic(epic);

        assertEquals(Status.NEW, epic.getStatus(), "Неверный статус NEW");

        Subtask subtask = new Subtask("Подзадача - 1",
                "Описание подзадачи - 1, эпической задачи - 1", epic.getId());
        fileBackedTaskManager.addNewSubtask(subtask);
        subtask.setStatus(Status.DONE);
        fileBackedTaskManager.updateSubtask(subtask);

        assertEquals(Status.DONE, epic.getStatus(), "Неверный статус DONE");

        Subtask subtask2 = new Subtask("Подзадача - 1",
                "Описание подзадачи - 1, эпической задачи - 1", epic.getId());
        fileBackedTaskManager.addNewSubtask(subtask2);

        assertEquals(Status.IN_PROGRESS, epic.getStatus(), "Неверный статус IN_PROGRESS");
    }

    @AfterEach
    void afterEach() {
        file.deleteOnExit();
    }
}