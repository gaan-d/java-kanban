import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private List<Integer> subtaskIds = new ArrayList<>(); // Хранение только ID подзадач

    public Epic(String name, String description) {
        super(name, description, Status.NEW);
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
                Subtask subtask = manager.getSubtask(subtaskId);
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
}
