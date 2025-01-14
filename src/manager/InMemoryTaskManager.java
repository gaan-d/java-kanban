package manager;

import exception.ManagerValidatePriority;
import task.Epic;
import task.Subtask;
import task.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
    private final HistoryManager historyManager;
    protected int taskIdCounter = 0;
    protected final Set<Task> timeOrderedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime,
            Comparator.nullsLast(LocalDateTime::compareTo)));

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
            epicSubtasks = epic.getSubtaskIds()
                    .stream()
                    .map(subtasks::get)
                    .collect(Collectors.toList());
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
        if (task.getStartTime() != null) {
            checkForTimeConflicts(task);
            timeOrderedTasks.add(task);
        }
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
        int subtaskId = subtask.getId();
        if (subtask.getStartTime() != null) {
            checkForTimeConflicts(subtask);
            timeOrderedTasks.add(subtask);
        }
        Epic epic = epics.get(subtask.getParentId());
        epic.addSubtaskId(subtaskId);
        subtasks.put(subtaskId, subtask);
        updateEpic(epic);
        updateEpicTimeFields(epic);
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
        timeOrderedTasks.removeIf(task -> id == task.getId());
        return tasks.remove(id);
    }

    @Override
    public void deleteEpic(int id) {
        Epic epic = epics.get(id);
        List<Subtask> epicSubtasks = getEpicSubtasks(id);
        epicSubtasks.forEach(subtask -> {
            epic.getSubtaskIds().remove(subtask.getId());
            timeOrderedTasks.removeIf(task -> epicSubtasks.stream()
                    .anyMatch(sub -> sub.getId() == task.getId()));
            subtasks.remove(subtask.getId());
        });
        epics.remove(id);
    }

    @Override
    public void deleteSubtask(int id) {
        Subtask subtask = getSubtask(id);
        Epic epic = epics.get(subtask.getParentId());
        timeOrderedTasks.remove(subtasks.get(id));
        historyManager.remove(id);
        subtasks.remove(id);
        epic.removeSubtaskId(id);
        updateEpic(epic);
        updateEpicTimeFields(epic);
    }

    @Override
    public void deleteTasks() {
        tasks.keySet().forEach(taskId -> timeOrderedTasks.removeIf(task -> task.getId() == taskId));
        tasks.clear();
    }

    @Override
    public void deleteSubtasks() {
        epics.values().forEach(epic -> {
            List<Integer> subtaskIds = new ArrayList<>(epic.getSubtaskIds());
            timeOrderedTasks.removeIf(task -> subtaskIds.contains(task.getId()));
            epic.getSubtaskIds().clear();
            updateEpic(epic);
        });
        subtasks.clear();
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

    private void updateEpicTimeFields(Epic epic) {
        List<Subtask> subtasks = getEpicSubtasks(epic.getId());
        LocalDateTime minStartTime = subtasks.stream()
                .map(Subtask::getStartTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);
        LocalDateTime maxEndTime = subtasks.stream()
                .map(Subtask::getEndTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);
        Duration duration = subtasks.stream()
                .map(Subtask::getDuration)
                .filter(Objects::nonNull)
                .reduce(Duration::plus)
                .orElse(Duration.ZERO);
        epic.setStartTime(minStartTime);
        epic.setEndTime(maxEndTime);
        epic.setDuration(duration);
    }

    @Override
    public Set<Task> getPrioritizedTasks() {
        return timeOrderedTasks;
    }

    protected void checkForTimeConflicts(Task task) {
        boolean hasConflict = timeOrderedTasks.stream()
                .filter(sortedTask -> !sortedTask.equals(task))
                .anyMatch(sortedTask -> isOverlapping(task, sortedTask));
        if (hasConflict) {
            System.out.println("Неверное время или продолжительность у задачи " + task);
            throw new ManagerValidatePriority("Невозможно добавить задачу из-за пересечений с уже имеющимися задачами");
        }
    }

    private boolean isOverlapping(Task task1, Task task2) {
        return task1.getStartTime().isBefore(task2.getEndTime()) && task1.getEndTime().isAfter(task2.getStartTime());
    }
}
