import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private final ArrayList<Subtask> subtasks;
    private List<Integer> subtaskIds = new ArrayList<>();

    public Epic(String name, String description) {
        super(name, description, Status.NEW);
        this.subtasks = new ArrayList<>();
    }

    public ArrayList<Subtask> getSubtasks() {
        return subtasks;
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
    private void updateStatus() {
        if (subtasks.isEmpty()) {
            setStatus(Status.NEW);
        } else {
            boolean done = true;
            boolean inProgress = false;
            for (Subtask subtask : subtasks) {
                if (subtask.getStatus() != Status.DONE) {
                    done = false;
                }
                if (subtask.getStatus() == Status.IN_PROGRESS) {
                    inProgress = true;
                }
            }
            if (done) {
                setStatus(Status.DONE);
            } else if (inProgress) {
                setStatus(Status.IN_PROGRESS);
            } else {
                setStatus(Status.NEW);
            }
        }
    }

    public void updateEpic() {
        updateStatus();
    }

    public void removeSubtask(Subtask subtask) {
        subtasks.remove(subtask);
        updateStatus();
    }

    public void clearSubtasks() {
        subtasks.clear();
        updateStatus();
    }

    @Override
    public String toString() {
        return "Epic{" +
                "title='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", id=" + getId() +
                ", status=" + getStatus() +
                ", subtasks=" + getSubtasks() +
                '}';
    }
}
