import exception.ManagerLoadException;
import exception.ManagerSaveException;
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
import java.nio.file.Paths;
import java.util.List;

import static manager.FileBackedTaskManager.loadFromFile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FileBackedTaskManagerTest extends AbstractTaskManagerTest<FileBackedTaskManager> {
    private File file;

    @BeforeEach
    @Override
    public void setUp() throws IOException {
        Path path = Files.createTempFile("file_autosave", ".csv");
        file = new File(String.valueOf(path));
        taskManager = new FileBackedTaskManager(file);
        task = new Task("Просто задача - 1", "Описание простой задачи - 1", Status.NEW);
        epic = new Epic("Эпическая задача - 1",
                "Описание эпической задачи - 1");
    }

    @AfterEach
    @Override
    public void finish() {
        file.deleteOnExit();
    }


    @Test
    void loadFromEmptyFileTest() {
        Path pathEmpty = Paths.get("file_empty.csv");
        FileBackedTaskManager backedTaskManager2 = loadFromFile(new File(String.valueOf(pathEmpty)));
        final List<Task> tasks = backedTaskManager2.getTasks();
        final List<Epic> epics = backedTaskManager2.getEpics();
        final List<Subtask> subtasks = backedTaskManager2.getSubtasks();
        assertEquals(0, tasks.size(), "Количество задач не верное");
        assertEquals(0, epics.size(), "Количество эпиков не верное");
        assertEquals(0, subtasks.size(), "Количество подзадач не верное");
    }


    @Test
    void loadFromFileTest() {
        final int taskId = taskManager.addNewTask(task);
        final int epicId = taskManager.addNewEpic(epic);
        final Subtask subtask = new Subtask("Подзадача - 1",
                "Описание подзадачи - 1 эпической задачи - 1", epicId);
        final int subtaskId = taskManager.addNewSubtask(subtask);

        final FileBackedTaskManager backedTaskManager2 = loadFromFile(file);
        final Task taskLoad = backedTaskManager2.getTask(taskId);
        final Epic epicLoad = backedTaskManager2.getEpic(epicId);
        final Subtask subtaskLoad = backedTaskManager2.getSubtask(subtaskId);
        assertEquals(task.getId(), taskLoad.getId(), "ID задач не совпадают");
        assertEquals(task.getType(), taskLoad.getType(), "Типы задач не совпадают");
        assertEquals(task.getName(), taskLoad.getName(), "Названия задач не совпадают");
        assertEquals(task.getDescription(), taskLoad.getDescription(), "Описания задач не совпадают");
        assertEquals(task.getStatus(), taskLoad.getStatus(), "Статусы задач не совпадают");
        assertEquals(task.getDuration(), taskLoad.getDuration(), "Duration задач не совпадают");
        assertEquals(task.getStartTime(), taskLoad.getStartTime(), "StartTime задач не совпадают");

        assertEquals(epic.getId(), epicLoad.getId(), "ID эпиков не совпадают");
        assertEquals(epic.getType(), epicLoad.getType(), "Типы эпиков не совпадают");
        assertEquals(epic.getName(), epicLoad.getName(), "Названия эпиков не совпадают");
        assertEquals(epic.getDescription(), epicLoad.getDescription(), "Описания эпиков не совпадают");
        assertEquals(epic.getStatus(), epicLoad.getStatus(), "Статусы эпиков не совпадают");
        assertEquals(epic.getSubtaskIds(), epicLoad.getSubtaskIds(), "ID подзадач у эпиков не совпадают");
        assertEquals(epic.getDuration(), epicLoad.getDuration(), "Duration эпиков не совпадают");
        assertEquals(epic.getStartTime(), epicLoad.getStartTime(), "StartTime эпиков не совпадают");
        assertEquals(epic.getEndTime(), epicLoad.getEndTime(), "EndTime эпиков не совпадают");

        assertEquals(subtask.getId(), subtaskLoad.getId(), "ID подзадач не совпадают");
        assertEquals(subtask.getType(), subtaskLoad.getType(), "Типы подзадач не совпадают");
        assertEquals(subtask.getName(), subtaskLoad.getName(), "Названия подзадач не совпадают");
        assertEquals(subtask.getDescription(), subtaskLoad.getDescription(), "Описания подзадач не совпадают");
        assertEquals(subtask.getStatus(), subtaskLoad.getStatus(), "Статусы подзадач не совпадают");
        assertEquals(subtask.getParentId(), subtaskLoad.getParentId(), "ID эпиков у подзадач не совпадают");
        assertEquals(subtask.getDuration(), subtaskLoad.getDuration(), "Duration подзадач не совпадают");
        assertEquals(subtask.getStartTime(), subtaskLoad.getStartTime(), "StartTime подзадач не совпадают");
    }


    @Test
    public void saveFileTest() {
        Path path2 = Paths.get("file_test.csv");
        File file2 = new File(String.valueOf(path2));
        FileBackedTaskManager taskManager2 = new FileBackedTaskManager(file2);
        Exception exception = assertThrows(ManagerSaveException.class, () -> taskManager2.addNewTask(task));
        assertEquals("Файл записи не существует",
                exception.getMessage());
    }

    @Test
    public void loadFileTest() {
        Path path2 = Paths.get("file_test.csv");
        File file2 = new File(String.valueOf(path2));
        Exception exception = assertThrows(ManagerLoadException.class, () -> FileBackedTaskManager.loadFromFile(file2));
        assertEquals("Файла не существует.",
                exception.getMessage());
    }
}