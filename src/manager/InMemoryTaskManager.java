package manager;

import task.Epic;
import task.Subtask;
import task.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryTaskManager implements TaskManager {
    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
    private final HistoryManager historyManager;
    protected int taskIdCounter = 0;

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    public int generateUid(Task task) {
        int hashId = 31 * Integer.hashCode(taskIdCounter);
        taskIdCounter++;
        return hashId;
    }

    @Override
    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public List<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
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

    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return subtask;
    }

    @Override
    public Subtask getSubtaskWithoutHistory(int id) {
        return subtasks.get(id);
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    public Epic getEpicWithoutHistory(int id) {
        return epics.get(id);
    }

    @Override
    public int addNewTask(Task task) {
        int taskId = generateUid(task);
        task.setId(taskId);
        tasks.put(taskId, task);
        return taskId;
    }

    @Override
    public int addNewEpic(Epic epic) {
        int epicId = generateUid(epic);
        epic.setId(epicId);
        epics.put(epicId, epic);
        return epicId;
    }

    @Override
    public int addNewSubtask(Subtask subtask) {
        subtask.setId(generateUid(subtask));
        Epic epic = epics.get(subtask.getParentId());
        epic.addSubtaskId(subtask.getId());
        subtasks.put(subtask.getId(), subtask);
        updateEpic(epic);
        return subtask.getId();
    }

    @Override
    public Task updateTask(Task task) {
        if (task != null && tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task);
        } else {
            System.out.println("Задача с таким ID не найдена.");
        }
        return task;
    }

    @Override
    public Epic updateEpic(Epic epic) {
        epic.updateEpic(this);
        epics.put(epic.getId(), epic);
        return epic;
    }

    @Override
    public Subtask updateSubtask(Subtask subtask) {
        if (subtask != null && subtasks.containsKey(subtask.getId())) {
            subtasks.put(subtask.getId(), subtask);
            updateEpic(getEpicWithoutHistory(subtask.getParentId()));
        } else {
            System.out.println("Подзадача с таким ID не найдена.");
        }
        return subtask;
    }

    @Override
    public Task deleteTask(int id) {
        return tasks.remove(id);
    }

    @Override
    public void deleteEpic(int id) {
        List<Subtask> epicSubtasks = getEpicSubtasks(id);
        for (Subtask subtask : epicSubtasks) {
            subtasks.remove(subtask.getId());
        }
        epics.remove(id);
    }

    @Override
    public void deleteSubtask(int id) {
        Subtask subtask = getSubtask(id);
        Epic epic = epics.get(subtask.getParentId());
        if (subtask != null) {
            historyManager.remove(id);
            subtasks.remove(id);
            epic.removeSubtaskId(id);
            updateEpic(epic);
        }
    }

    @Override
    public void deleteTasks() {
        tasks.clear();
    }

    @Override
    public void deleteSubtasks() {
        subtasks.clear();
        for (Epic epic : epics.values()) {
            updateEpic(epic);
        }
    }

    @Override
    public void deleteEpics() {
        epics.clear();
        subtasks.clear();
    }

    @Override
    public Map<Integer, Task> getTasksMap() {
        return tasks;
    }

    @Override
    public Map<Integer, Epic> getEpicsMap() {
        return epics;
    }

    @Override
    public Map<Integer, Subtask> getSubtasksMap() {
        return subtasks;
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }
}
