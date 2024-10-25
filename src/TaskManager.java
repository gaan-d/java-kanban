import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class TaskManager {
    private Map<Integer, Task> tasks = new HashMap<>();
    private Map<Integer, Subtask> subtasks = new HashMap<>();
    private Map<Integer, Epic> epics = new HashMap<>();
    private int taskIdCounter = 0;

    public int generateTaskId() {
        return ++taskIdCounter;
    }

    public int generateSubtaskId() {
        return ++taskIdCounter;
    }

    public int generateEpicId() {
        return ++taskIdCounter;
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
        int taskId = generateTaskId();
        task.setId(taskId);
        tasks.put(taskId, task);
        return taskId;
    }

    int addNewEpic(Epic epic) {
        int epicId = generateEpicId();
        epic.setId(epicId);
        epics.put(epicId, epic);
        return epicId;
    }

    Integer addNewSubtask(Subtask subtask) {
        int subtaskId = generateSubtaskId();
        subtask.setId(subtaskId);
        subtasks.put(subtaskId, subtask);
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
        epic.updateEpic();
    }

    void updateSubtask(Subtask subtask) {
        if (subtask != null && subtasks.containsKey(subtask.getId())){
            subtasks.put(subtask.getId(),subtask);
            updateEpic(getEpic(subtask.getParentId()));
        }
        else {
            System.out.println("Подзадача с ID " + subtask.getId() + " не найдена.");
        }
    }

    void deleteTask(int id) {
        tasks.remove(id);
    }

    void deleteEpic(int id) {
        epics.remove(id);
    }

    void deleteSubtask(int id) {
        subtasks.remove(id);
    }

    void deleteTasks() {
        tasks.clear();
    }

    void deleteSubtasks() {
        subtasks.clear();
    }

    void deleteEpics() {
        epics.clear();
    }
}
