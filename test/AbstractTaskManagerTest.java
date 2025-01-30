import exception.ManagerValidatePriority;
import manager.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.Status;
import task.Subtask;
import task.Task;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class AbstractTaskManagerTest<T extends TaskManager> {
    protected T taskManager;
    protected Task task;
    protected Epic epic;

    @BeforeEach
    public abstract void setUp() throws IOException;

    @AfterEach
    public abstract void finish();

    @Test
    public void addNewTaskTest() {
        final int taskId = taskManager.addNewTask(task);
        final Task savedTask = taskManager.getTask(taskId);

        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask, "Задачи не совпадают.");
        final List<Task> tasks = taskManager.getTasks();
        assertNotNull(tasks, "Задачи не возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(task, tasks.getFirst(), "Задачи не совпадают.");
    }

    @Test
    void addNewSubtaskTest() {
        final int epicId = taskManager.addNewEpic(epic);
        final Subtask subtask = new Subtask("Подзадача - 1",
                "Описание подзадачи - 1, эпической задачи - 1", epicId);
        final int subtaskId = taskManager.addNewSubtask(subtask);
        final Subtask savedSubtask = taskManager.getSubtask(subtaskId);
        assertNotNull(savedSubtask, "Подзадача не найдена.");
        assertEquals(subtask, savedSubtask, "Подзадачи не совпадают.");
        final int savedEpicId = savedSubtask.getParentId();
        assertEquals(subtask.getParentId(), savedEpicId, "Эпики у подзадач не совпадают.");
        final List<Subtask> subtasks = taskManager.getSubtasks();
        assertNotNull(subtasks, "Подзадачи не возвращаются.");
        assertEquals(1, subtasks.size(), "Неверное количество подзадач.");
        assertEquals(subtask, subtasks.getFirst(), "Подзадачи не совпадают.");
    }

    @Test
    void addNewEpicTest() {
        final int epicId = taskManager.addNewEpic(epic);
        final Epic savedEpic = taskManager.getEpic(epicId);
        assertNotNull(savedEpic, "Эпик не найдена.");
        assertEquals(epic, savedEpic, "Эпики не совпадают.");
        final List<Epic> epics = taskManager.getEpics();
        assertNotNull(epics, "Эпики не возвращаются.");
        assertEquals(1, epics.size(), "Неверное количество Эпиков.");
        assertEquals(epic, epics.getFirst(), "Эпики не совпадают.");
    }

    @Test
    void updateTaskTest() {
        final int taskId = taskManager.addNewTask(task);
        final Task savedTask = taskManager.getTask(taskId);
        savedTask.setStatus(Status.DONE);
        taskManager.updateTask(savedTask);
        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask, "Задачи не совпадают.");
        assertEquals(task.getStatus(), savedTask.getStatus(), "Статусы задач не совпадают");
    }

    @Test
    void updateEpicTest() {
        int epicId = taskManager.addNewEpic(epic);
        final Epic savedEpic = taskManager.getEpic(epicId);
        Epic epic2 = new Epic(epic.getId(), "Эпическая задача - 2", "Ставим вместо эпической задачи - 1", epic.getStatus());
        taskManager.updateEpic(epic2);
        assertNotNull(savedEpic, "Эпик не найден.");
        assertEquals(epic2, savedEpic, "Эпики не совпадают.");
        final List<Epic> epics = taskManager.getEpics();
        assertNotNull(epics, "Эпики на возвращаются.");
        assertEquals(1, epics.size(), "Неверное количество эпиков.");
        assertEquals(epic2, epics.getFirst(), "Эпики не совпадают.");
    }

    @Test
    void updateSubtaskAndEpicTest() {
        final int epicId = taskManager.addNewEpic(epic);
        final Subtask subtask = new Subtask("Подзадача - 1",
                "Описание подзадачи - 1, эпической задачи - 1", epicId);
        final int subtaskId = taskManager.addNewSubtask(subtask);
        final Subtask savedSubtask = taskManager.getSubtask(subtaskId);
        savedSubtask.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask);
        assertNotNull(savedSubtask, "Подзадачи не найдена.");
        assertEquals(subtask, savedSubtask, "Подзадачи не совпадают.");
        final List<Subtask> subtasks = taskManager.getSubtasks();
        assertNotNull(subtasks, "Подзадачи на возвращаются.");
        assertEquals(1, subtasks.size(), "Неверное количество подзадач.");
        assertEquals(subtask, subtasks.getFirst(), "Подзадачи не совпадают.");
        assertEquals(taskManager.getEpic(epicId).getStatus(), savedSubtask.getStatus(), "Статусы подзадач не совпадают");
    }

    @Test
    void deleteTaskTest() {
        taskManager.addNewTask(task);
        taskManager.deleteTask(task.getId());
        assertTrue(taskManager.getTasks().isEmpty(), "Задача не удалилась");
        assertEquals(0, taskManager.getTasks().size(), "Задача не удалилась");
    }

    @Test
    void deleteAllTasksTest() {
        taskManager.addNewTask(task);
        taskManager.addNewTask(task);
        taskManager.deleteTasks();
        assertTrue(taskManager.getTasks().isEmpty(), "Задачи не удалились");
        assertEquals(0, taskManager.getTasks().size(), "Задачи не удалились");
    }

    @Test
    void deleteEpicTest() {
        taskManager.addNewEpic(epic);
        taskManager.deleteEpic(epic.getId());
        assertTrue(taskManager.getEpics().isEmpty(), "Эпик не удалился");
        assertEquals(0, taskManager.getEpics().size(), "Эпик не удалился");
    }

    @Test
    void deleteAllEpicsTest() {
        taskManager.addNewEpic(epic);
        taskManager.addNewEpic(epic);
        taskManager.deleteEpics();
        assertTrue(taskManager.getEpics().isEmpty(), "Эпики не удалились");
        assertEquals(0, taskManager.getEpics().size(), "Эпики не удалились");
    }

    @Test
    void deleteSubtaskTest() {
        taskManager.addNewEpic(epic);
        Subtask subtask = new Subtask("Подзадача - 1",
                "Описание подзадачи - 1, эпической задачи - 1", epic.getId());
        int subtaskId = taskManager.addNewSubtask(subtask);
        assertEquals(1, epic.getSubtaskIds().size(), "ID подзадачи не зарегистрировался у эпика");
        taskManager.deleteSubtask(subtaskId);
        assertTrue(taskManager.getSubtasks().isEmpty(), "Подзадача не удалилась");
        assertEquals(0, taskManager.getSubtasks().size(), "Подзадача не удалилась");
        assertTrue(epic.getSubtaskIds().isEmpty(), "ID подзадачи не удалился из списка у эпика");
    }

    @Test
    void deleteAllSubtaskTest() {
        taskManager.addNewEpic(epic);
        Subtask subtask = new Subtask("Подзадача - 1",
                "Описание подзадачи - 1, эпической задачи - 1", epic.getId());
        taskManager.addNewSubtask(subtask);
        taskManager.addNewSubtask(subtask);
        taskManager.deleteSubtasks();
        assertTrue(taskManager.getSubtasks().isEmpty(), "Подзадачи не удалились");
        assertEquals(0, taskManager.getSubtasks().size(), "Подзадачи не удалились");
    }

    @Test
    void updateStatusEpicTest() {
        final int epicId = taskManager.addNewEpic(epic);
        Subtask subtask = new Subtask("Подзадача - 1",
                "Описание подзадачи - 1, эпической задачи - 1", Status.NEW, epic.getId());
        taskManager.addNewSubtask(subtask);
        assertEquals(Status.NEW, epic.getStatus(), "Неверный статус NEW");
        Subtask subtask2 = new Subtask("Подзадача - 1",
                "Описание подзадачи - 1, эпической задачи - 1", Status.IN_PROGRESS, epic.getId());
        taskManager.addNewSubtask(subtask2);
        assertEquals(Status.IN_PROGRESS, epic.getStatus(), "Неверный статус IN_PROGRESS");
        subtask.setStatus(Status.DONE);
        subtask2.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask);
        taskManager.updateSubtask(subtask2);
        assertEquals(Status.DONE, epic.getStatus(), "Неверный статус DONE");
    }

    @Test
    public void validateTaskPriorityTest() {
        Task task1 = new Task("Задача- 1", "Описание 1", LocalDateTime.of(2023, 10,
                1, 10, 0), Duration.ofHours(2));
        Task task2 = new Task("Задача- 2", "Описание 2", LocalDateTime.of(2023, 10,
                1, 11, 0), Duration.ofHours(1));
        taskManager.addNewTask(task1);
        Exception exception = assertThrows(ManagerValidatePriority.class, () -> {
            taskManager.addNewTask(task2);
        });
        assertEquals("Невозможно добавить задачу из-за пересечений с уже имеющимися задачами",
                exception.getMessage());
    }

    @Test
    public void validateEpicAndSubtaskPriorityTest() {
        taskManager.addNewEpic(epic);
        Subtask subtask1 = new Subtask("Подзадача - 1",
                "Описание подзадачи - 1, эпической задачи - 1", LocalDateTime.of(2024, 10,
                1, 10, 0), Duration.ofHours(2), epic.getId());
        taskManager.addNewSubtask(subtask1);
        Subtask subtask2 = new Subtask("Подзадача - 1",
                "Описание подзадачи - 1, эпической задачи - 1", LocalDateTime.of(2024, 10,
                1, 11, 0), Duration.ofHours(1), epic.getId());
        Exception exception = assertThrows(ManagerValidatePriority.class, () -> {
            taskManager.addNewSubtask(subtask2);
        });
        assertEquals("Невозможно добавить задачу из-за пересечений с уже имеющимися задачами",
                exception.getMessage());
    }
}