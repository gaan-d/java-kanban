package tests;

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


public class InMemoryHistoryManagerTest {

    private static TaskManager taskManager;

    @BeforeEach
    public void beforeEach() {
        taskManager = Managers.getDefault();
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
}

