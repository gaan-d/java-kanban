package tests;

import manager.Managers;
import manager.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.Status;
import task.Subtask;
import task.Task;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class InMemoryTaskManagerTest {
    private static TaskManager taskManager;

    @BeforeEach
    public void beforeEach() {
        taskManager = Managers.getDefault();
    }

    //проверка на добавляет ли задачу и ищет по id
    @Test
    void addNewTest() {
        final Task task = taskManager.getTask(taskManager.addNewTask(new Task("Test", "Test addNewTask description", Status.NEW)));
        final Task savedTask = taskManager.getTask(task.getId());
        assertNotNull(savedTask, "The task was not found");
        assertEquals(task, savedTask, "The tasks do not match");
        final List<Task> tasks = taskManager.getTasks();
        assertNotNull(tasks, "The tasks are not returned");
        assertEquals(1, tasks.size(), "Incorrect number of tasks");
    }

    //проверка на добавляет ли эпик и подзадачу и ищет по id
    @Test
    void addNewEpicAndSubtasks() {
        final Epic testEpic = taskManager.getEpic(taskManager.addNewEpic(new Epic("Список покупок",
                "Завтра идем в магазин")));

        final Subtask subtask = taskManager.getSubtask(taskManager.addNewSubtask(new Subtask("Купить молока",
                "Для кофе", Status.NEW, testEpic.getId())));
        final Subtask subtask2 = taskManager.getSubtask(taskManager.addNewSubtask(new Subtask("Купить чая",
                "Подобрать праздничную одежду", Status.NEW, testEpic.getId())));

        final Epic savedEpic = taskManager.getEpic(testEpic.getId());
        Subtask savedSubtask1 = taskManager.getSubtask(subtask.getId());
        final Subtask savedSubtask2 = taskManager.getSubtask(subtask2.getId());

        assertNotNull(savedEpic, "Epic is not found");
        assertNotNull(savedSubtask2, "The subtask was not found");
        assertEquals(testEpic, savedEpic, "The epics don't match");
        assertEquals(subtask, savedSubtask1, "The subtasks do not match");
        assertEquals(subtask2, savedSubtask2, "The subtasks do not match");

        final List<Epic> epics = taskManager.getEpics();
        assertNotNull(epics, "Epics don't come back");
        assertEquals(1, epics.size(), "The wrong number of epics");

        final List<Subtask> subtasks = taskManager.getSubtasks();
        assertNotNull(subtasks, "Subtasks are not returned");
        assertEquals(1, epics.size(), "Incorrect number of subtasks");

    }

    //Обновить задачу, затем вернуть задачу с тем же id
    @Test
    public void updateTaskShouldReturnTaskWithTheSameId() {
        final Task expected = new Task("имя", "описание", Status.NEW);
        taskManager.addNewTask(expected);
        final Task updatedTask = new Task(expected.getId(), "новое имя", "новое описание", Status.DONE);
        final Task actual = taskManager.updateTask(updatedTask);
        assertEquals(expected, actual, "Вернулась задача с другим id");
    }

    // Обновить эпик, затем вернуть эпик с тем же id
    @Test
    public void updateEpicShouldReturnEpicWithTheSameId() {
        final Epic expected = new Epic("имя", "описание");
        taskManager.addNewEpic(expected);
        final Epic updatedEpic = new Epic("новое имя", "новое описание");
        final Epic actual = taskManager.updateEpic(updatedEpic);
        assertEquals(expected, actual, "Вернулся эпик с другим id");
    }

    //Обновляет подзадачу, затем возвращает подзадачу с тем же id
    @Test
    public void updateSubtaskShouldReturnSubtaskWithTheSameId() {
        final Epic epic = new Epic("имя", "описание");
        taskManager.addNewEpic(epic);
        final Subtask expected = new Subtask("имя", "описание", Status.NEW, epic.getId());
        taskManager.addNewSubtask(expected);
        final Subtask updatedSubtask = new Subtask(expected.getId(), "новое имя", "новое описание", Status.DONE, epic.getId());
        final Subtask actual = taskManager.updateSubtask(updatedSubtask);
        assertEquals(expected, actual, "Вернулась подзадача с другим id");
    }

    //Удалить эпик и вернуть пустой список
    @Test
    public void deleteEpicsShouldReturnEmptyList() {
        taskManager.addNewEpic(new Epic(1, "Задача3", "Подзадача3", Status.IN_PROGRESS));
        taskManager.deleteEpics();
        List<Epic> epics = taskManager.getEpics();
        assertTrue(epics.isEmpty(), "Список Эпиков должен быть пуст");
    }

    //удаление Задачи По id
    @Test
    public void deleteTaskByIdShouldReturnNullIfKeyIsMissing() {
        taskManager.addNewTask(new Task(1, "Задача1", "Подзадача1", Status.NEW));
        taskManager.addNewTask(new Task(2, "Задача2", "Подзадача2", Status.DONE));
        assertNull(taskManager.deleteTask(3));
    }

    //удаление эпик По Id
    @Test
    public void deleteEpicByIdShouldReturnNullIfKeyIsMissing() {
        taskManager.addNewEpic(new Epic(1, "Задача3", "Подзадача3", Status.IN_PROGRESS));
        taskManager.getEpic(1);
        assertNull(taskManager.deleteTask(1));
    }

    @Test
    public void removeSubtaskShouldNotKeepOldId() {
        final Epic testEpic = taskManager.getEpic(taskManager.addNewEpic(new Epic("Список покупок", "Завтра идем в магазин")));
        final Subtask testSubtask = taskManager.getSubtask(taskManager.addNewSubtask(new Subtask("Купить молока", "Для кофе", Status.NEW, testEpic.getId())));

        // Удаляем подзадачу
        taskManager.deleteSubtask(testSubtask.getId());

        // Проверяем, что старый id не остался в истории
        List<Task> history = taskManager.getHistory();
        assertFalse(history.contains(testSubtask), "История не должна содержать удаленную подзадачу");
    }

    @Test
    public void epicShouldNotContainRemovedSubtaskIds() {
        final Epic testEpic = taskManager.getEpic(taskManager.addNewEpic(new Epic("Список покупок", "Завтра идем в магазин")));
        final Subtask testSubtask = taskManager.getSubtask(taskManager.addNewSubtask(new Subtask("Купить молока", "Для кофе", Status.NEW, testEpic.getId())));

        // Удаляем подзадачу
        taskManager.deleteSubtask(testSubtask.getId());

        // Проверяем, что эпик больше не содержит удаленную подзадачу
        Epic updatedEpic = taskManager.getEpic(testEpic.getId());
        assertFalse(updatedEpic.getSubtaskIds().contains(testSubtask.getId()), "Эпик не должен содержать id удаленной подзадачи");
    }

    @Test
    public void updateTaskWithSettersShouldUpdateManagerData() {
        final Task task = new Task("Тестовая задача", "Описание", Status.NEW);
        taskManager.addNewTask(task);

        // Обновляем задачу через сеттеры
        task.setName("Обновленное имя");
        task.setDescription("Обновленное описание");
        task.setStatus(Status.DONE);
        taskManager.updateTask(task);

        // Проверяем, что изменения корректно отразились в менеджере
        Task updatedTask = taskManager.getTask(task.getId());
        assertEquals("Обновленное имя", updatedTask.getName(), "Имя задачи не обновилось");
        assertEquals("Обновленное описание", updatedTask.getDescription(), "Описание задачи не обновилось");
        assertEquals(Status.DONE, updatedTask.getStatus(), "Статус задачи не обновился");
    }
}
