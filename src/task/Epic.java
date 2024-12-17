package task;

import manager.TaskManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Epic extends Task {
    private List<Integer> subtaskIds = new ArrayList<>(); // Хранение только ID подзадач

    public Epic(String name, String description) {
        super(name, description, Status.NEW);
    }

    public Epic(int id, String name, String description, Status status) {
        super(id, name, description, status);
    }

    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void addSubtaskId(int subtaskId) {
        subtaskIds.add(subtaskId);
    }

    public void removeSubtaskId(int subtaskId) {
        subtaskIds.remove((Integer) subtaskId);
    }

    // Метод обновления статуса эпика на основе статусов подзадач
    private void updateStatus(TaskManager manager) {
        if (subtaskIds.isEmpty()) {
            setStatus(Status.NEW);
        } else {
            boolean allDone = true;
            boolean hasInProgress = false;

            for (int subtaskId : subtaskIds) {
                Subtask subtask = manager.getSubtaskWithoutHistory(subtaskId);
                if (subtask != null) {
                    if (subtask.getStatus() != Status.DONE) {
                        allDone = false;
                    }
                    if (subtask.getStatus() == Status.IN_PROGRESS) {
                        hasInProgress = true;
                    }
                }
            }

            if (allDone) {
                setStatus(Status.DONE);
            } else if (hasInProgress) {
                setStatus(Status.IN_PROGRESS);
            } else if (!allDone && !hasInProgress) {
                setStatus(Status.IN_PROGRESS);
            } else {
                setStatus(Status.NEW);
            }
        }
    }

    public void updateEpic(TaskManager manager) {
        updateStatus(manager);
    }

    @Override
    public String toString() {
        return "Epic{" +
                "title='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", id=" + getId() +
                ", status=" + getStatus() +
                ", subtaskIds=" + getSubtaskIds() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return this.getId() == task.getId(); // сравнение по ID
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public TaskType getType() {
        return TaskType.EPIC;
    }
}
