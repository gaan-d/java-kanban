package manager;

import exception.ManagerValidatePriorityException;
import exception.NotFoundException;
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
    protected int taskIdCounter = 1;
    protected final Set<Task> timeOrderedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime,
            Comparator.nullsLast(LocalDateTime::compareTo)));

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    public int generateUid(Task task) {
        return taskIdCounter++;
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<Subtask> getEpicSubtasks(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            throw new NotFoundException("Эпик с ID " + epicId + " не найден.");
        }
        return epic.getSubtaskIds()
                .stream()
                .filter(Objects::nonNull)
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        if (task == null) {
            throw new NotFoundException("Задача с ID " + id + " не найдена.");
        }
        return task;
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        if (subtask == null) {
            throw new NotFoundException("Подзадача с ID " + id + " не найдена.");
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
        if (epic == null) {
            throw new NotFoundException("Эпическая задача с ID " + id + " не найдена.");
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
            throw new NotFoundException("Задача с ID" + task.getId() + "не найдена.");
        }
        return task;
    }

    @Override
    public Epic updateEpic(Epic epic) {
        if (!epics.containsKey(epic.getId())) {
            throw new NotFoundException("Эпик с ID " + epic.getId() + " не найден.");
        }
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
            throw new NotFoundException("Подзадача с ID" + subtask.getId() + "не найдена.");
        }
        return subtask;
    }

    @Override
    public Task deleteTask(int id) {
        timeOrderedTasks.remove(getTask(id));
        tasks.remove(id);
        return null;
    }

    @Override
    public void deleteEpic(int id) {
        if (epics.containsKey(id)) {
            Epic epic = epics.get(id);
            List<Subtask> epicSubtasks = getEpicSubtasks(id);
            epicSubtasks.forEach(subtask -> {
                epic.getSubtaskIds().remove(subtask.getId());
                timeOrderedTasks.removeIf(task -> epicSubtasks.stream()
                        .anyMatch(sub -> sub.getId() == task.getId()));
                subtasks.remove(subtask.getId());
            });
            epics.remove(id);
        } else {
            throw new NotFoundException("Эпик с ID " + id + " не найден.");
        }
    }

    @Override
    public void deleteSubtask(int id) {
        if (subtasks.containsKey(id)) {
            try {
                Subtask subtask = getSubtask(id);
                Epic epic = epics.get(subtask.getParentId());
                timeOrderedTasks.remove(subtasks.get(id));
                historyManager.remove(id);
                subtasks.remove(id);
                epic.removeSubtaskId(id);
                updateEpic(epic);
                updateEpicTimeFields(epic);
            } catch (Exception e) {
                throw new NotFoundException("Эпик для данной подзадачи не найден.");
            }
        } else {
            throw new NotFoundException("Подзадача с ID " + id + " не найдена.");
        }
    }

    @Override
    public void deleteTasks() {
        timeOrderedTasks.removeAll(tasks.values());
        tasks.clear();
    }

    @Override
    public void deleteSubtasks() {
        epics.values().forEach(epic -> {
            epic.getSubtaskIds().clear();
            updateEpic(epic);
        });
        timeOrderedTasks.removeAll(subtasks.values());
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
    public List<Task> getPrioritizedTasks() {
        if (timeOrderedTasks.isEmpty()) {
            throw new NotFoundException("Список сортировки пуст!");
        }
        return new ArrayList<>(timeOrderedTasks);
    }

    protected void checkForTimeConflicts(Task task) {
        boolean hasConflict = timeOrderedTasks.stream()
                .filter(sortedTask -> !sortedTask.equals(task))
                .anyMatch(sortedTask -> isOverlapping(task, sortedTask));
        if (hasConflict) {
            System.out.println("Неверное время или продолжительность у задачи " + task);
            throw new ManagerValidatePriorityException("Невозможно добавить задачу из-за пересечений с уже имеющимися задачами");
        }
    }

    private boolean isOverlapping(Task task1, Task task2) {
        return task1.getStartTime().isBefore(task2.getEndTime()) && task1.getEndTime().isAfter(task2.getStartTime());
    }
}
