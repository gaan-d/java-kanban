import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class TaskManager {
    private Map<Integer, Task> tasks = new HashMap<>();
    private Map<Integer, Subtask> subtasks = new HashMap<>();
    private Map<Integer, Epic> epics = new HashMap<>();
    private int taskIdCounter = 0;

    public int generateUid(Task task) {
        int hashId = 31 * Integer.hashCode(taskIdCounter);
        taskIdCounter++;
        return hashId;
    }

    List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    List<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    List<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    public List<Subtask> getEpicSubtasks(int epicId) {
        Epic epic = epics.get(epicId);
        List<Subtask> epicSubtasks = new ArrayList<>();
        if (epic != null) {
            for (int subtaskId : epic.getSubtaskIds()) {
                Subtask subtask = subtasks.get(subtaskId);
                if (subtask != null) {
                    epicSubtasks.add(subtask);
                }
            }
        } else {
            System.out.println("Эпик с ID " + epicId + " не найден.");
        }
        return epicSubtasks;
    }

    Task getTask(int id) {
        return tasks.get(id);
    }

    Subtask getSubtask(int id) {
        return subtasks.get(id);
    }

    Epic getEpic(int id) {
        return epics.get(id);
    }

    int addNewTask(Task task) {
        int taskId = generateUid(task);
        task.setId(taskId);
        tasks.put(taskId, task);
        return taskId;
    }

    int addNewEpic(Epic epic) {
        int epicId = generateUid(epic);
        epic.setId(epicId);
        epics.put(epicId, epic);
        return epicId;
    }

    int addNewSubtask(Subtask subtask) {
        int epicId = subtask.getParentId();
        Epic epic = epics.get(epicId);
        int subtaskId = generateUid(subtask);
        subtask.setId(subtaskId);
        subtasks.put(subtaskId, subtask);

        epic.addSubtaskId(subtaskId);
        updateEpic(getEpic(epicId));
        return subtaskId;
    }

    void updateTask(Task task) {
        if (task != null && tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task);
        } else {
            System.out.println("Задача с ID " + task.getId() + " не найдена.");
        }
    }

    void updateEpic(Epic epic) {
        epic.updateEpic(this);
        epics.put(epic.getId(), epic);// не совсем ясно зачем, т.к. объект меняется в самой мапе
        // (если я не правильно понял, отпишись пожалуйста)
    }

    void updateSubtask(Subtask subtask) {
        if (subtask != null && subtasks.containsKey(subtask.getId())) {
            subtasks.put(subtask.getId(), subtask);
            updateEpic(getEpic(subtask.getParentId()));
        } else {
            System.out.println("Подзадача с ID " + subtask.getId() + " не найдена.");
        }
    }

    void deleteTask(int id) {
        tasks.remove(id);
    }

    void deleteEpic(int id) {
        List<Subtask> epicSubtasks = getEpicSubtasks(id);
        for (Subtask subtask : epicSubtasks) {
            subtasks.remove(subtask.getId());
        }
        epics.remove(id);
    }

    void deleteSubtask(int id) {
        Subtask subtask = getSubtask(id);
        if (subtask != null) {
            subtasks.remove(id);
            updateEpic(getEpic(subtask.getParentId()));
        }
    }

    void deleteTasks() {
        tasks.clear();
    }

    void deleteSubtasks() {
        subtasks.clear();
        for (Epic epic : epics.values()) {
            updateEpic(epic);
        }
    }

    void deleteEpics() {
        epics.clear();
        subtasks.clear();
    }

    public Map<Integer, Task> getTasksMap() {
        return tasks;
    }

    public Map<Integer, Epic> getEpicsMap() {
        return epics;
    }

    public Map<Integer, Subtask> getSubtasksMap() {
        return subtasks;
    }
}
