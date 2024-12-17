package task;

public class Subtask extends Task {

    private final int parentId;

    public Subtask(String name, String description, Status status, int parentId) {
        super(name, description, status);
        this.parentId = parentId;
    }

    public Subtask(int id, String name, String description, Status status, int parentId) {
        super(id, name, description, status);
        this.parentId = parentId;
    }
    public Subtask(String name, String description, int epicId) {
        super(name, description);
        this.parentId = epicId;
    }

    public int getParentId() {
        return parentId;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", id=" + getId() +
                ", status=" + getStatus() +
                '}';
    }

    @Override
    public TaskType getType() {
        return TaskType.SUBTASK;
    }
}
