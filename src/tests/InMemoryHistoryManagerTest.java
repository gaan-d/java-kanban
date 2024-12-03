package tests;

import manager.HistoryManager;
import manager.Managers;
import manager.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.Status;
import task.Epic;
import task.Subtask;
import task.Task;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;


public class InMemoryHistoryManagerTest {

    private static TaskManager taskManager;
    private HistoryManager historyManager;

    @BeforeEach
    public void beforeEach() {
        taskManager = Managers.getDefault();
        historyManager = Managers.getDefaultHistory();
    }


    //Должен вернуть Старый эпик после обновления
    @Test
    public void getHistoryShouldReturnOldEpicAfterUpdate() {
        Epic epic1 = new Epic("Тестовый эпик", "Тестовое описание   ");
        taskManager.addNewEpic(epic1);
        taskManager.getEpic(epic1.getId());
        taskManager.updateEpic(new Epic(epic1.getId(), "Новое имя", "Новое описание", Status.IN_PROGRESS));
        List<Task> epics = taskManager.getHistory();
        Epic oldEpic = (Epic) epics.getFirst();
        assertEquals(epic1.getName(), oldEpic.getName(), "В истории нет старой версии");
        assertEquals(epic1.getDescription(), oldEpic.getDescription(), "В истории нет старой версии");
    }

    //вернуть эту подзадачу после обновления
    @Test
    public void getHistoryShouldReturnOldSubtaskAfterUpdate() {
        Epic testEpic = new Epic("Эпик 1", "Описание эпика");
        taskManager.addNewEpic(testEpic);
        Subtask testSubtask = new Subtask("Пункт 1", "Начало", Status.NEW, testEpic.getId());
        taskManager.addNewSubtask(testSubtask);
        List<Task> subtasks3 = taskManager.getHistory();
        taskManager.getSubtask(testSubtask.getId());
        taskManager.updateSubtask(new Subtask(testSubtask.getId(), "Новое имя", "новое описание", Status.IN_PROGRESS, testEpic.getId()));
        List<Task> subtasks = taskManager.getHistory();
        Subtask oldSubtask = (Subtask) subtasks.getFirst();
        assertEquals(testSubtask.getName(), oldSubtask.getName(), "В истории нет старой версии");
        assertEquals(testSubtask.getDescription(), oldSubtask.getDescription(), "В истории нет старой версии");
    }

    @Test
    public void addTask() {
        Task task = new Task("Тестовая задача", "Тестовое описание", Status.NEW);
        historyManager.add(task); // Добавляем задачу в историю

        List<Task> history = historyManager.getHistory(); // Получаем историю
        assertEquals(1, history.size(), "История должна содержать одну задачу");
        assertEquals(task, history.getFirst(), "Задача в истории должна совпадать с добавленной");
    }

    @Test
    public void addingDuplicateTaskShouldReplacePreviousEntry() {
        Task task = new Task("Тестовая задача", "Описание", Status.NEW);
        historyManager.add(task); // Добавляем задачу в историю
        historyManager.add(task); // Добавляем ту же задачу снова

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "История не должна содержать дубликатов");
    }

    @Test
    public void removeTaskShouldDeleteItFromHistory() {
        Task task1 = new Task(1, "Задача 1", "Описание 1", Status.NEW);
        Task task2 = new Task(2, "Задача 2", "Описание 2", Status.NEW);
        Task task3 = new Task(3, "Задача 3", "Описание 3", Status.NEW);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(task2.getId());

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "История должна содержать две задачи после удаления");
        assertFalse(history.contains(task2), "История не должна содержать удалённую задачу");
    }

    //удаление первой задачи
    @Test
    public void removeFirstTaskShouldUpdateHead() {
        Task task1 = new Task(1, "Задача 1", "Описание 1", Status.NEW);
        Task task2 = new Task(2, "Задача 2", "Описание 2", Status.NEW);

        historyManager.add(task1);
        historyManager.add(task2);

        historyManager.remove(task1.getId()); // Удаляем первую задачу

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "История должна содержать одну задачу");
        assertEquals(task2, history.getFirst(), "Первая задача должна быть заменена на вторую");
    }

    //удаление последней задачи
    @Test
    public void removeLastTaskShouldUpdateTail() {
        Task task1 = new Task(1, "Задача 1", "Описание 1", Status.NEW);
        Task task2 = new Task(2, "Задача 2", "Описание 2", Status.NEW);
        Task task3 = new Task(3, "Задача 2", "Описание 2", Status.NEW);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(task3.getId()); // Удаляем последнюю задачу

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "История должна содержать две задачу");
        assertEquals(task2, history.getLast(), "Последняя задача должна быть заменена второй");
    }
}

