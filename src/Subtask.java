public class Subtask extends Task {

    private final int parentId;

    public Subtask(String name, String description, Status status, int parentId) {
        super(name, description, status);
        this.parentId = parentId;
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
}
