package manager;

import task.Epic;
import task.Subtask;
import task.Task;

import java.util.List;
import java.util.Map;

public interface TaskManager {
    List<Task> getAllTasks();

    List<Subtask> getAllSubtasks();

    List<Epic> getAllEpics();

    List<Subtask> getEpicSubtasks(int epicId);

    Task getTask(int id);

    Subtask getSubtask(int id);

    Epic getEpic(int id);

    int addNewTask(Task task);

    int addNewEpic(Epic epic);

    int addNewSubtask(Subtask subtask);

    Task updateTask(Task task);

    Epic updateEpic(Epic epic);

    Subtask updateSubtask(Subtask subtask);

    Task deleteTask(int id);

    void deleteEpic(int id);

    void deleteSubtask(int id);

    void deleteTasks();

    void deleteSubtasks();

    void deleteEpics();

    Map<Integer, Task> getTasksMap();

    Map<Integer, Epic> getEpicsMap();

    Map<Integer, Subtask> getSubtasksMap();

    List<Task> getHistory();

    Subtask getSubtaskWithoutHistory(int subtaskId);

    List<Task> getPrioritizedTasks();
}
