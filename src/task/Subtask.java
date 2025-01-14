package task;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {

    private final int parentId;

    public Subtask(String name, String description, Status status, int parentId) {
        super(name, description, status);
        this.parentId = parentId;
    }

    public Subtask(String name, String description, LocalDateTime startTime, Duration duration, int parentId) {
        super(name, description, startTime, duration);
        this.parentId = parentId;
    }
    public Subtask(String name, String description, Status status, LocalDateTime startTime, Duration duration, int parentId) {
        super(name, description, status, startTime, duration);
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

    public Subtask(int id, String name, String description, Status status, LocalDateTime startTime, Duration duration, int parentId) {
        super(id, description, name, status, startTime, duration);
        this.parentId = parentId;
    }

    public int getParentId() {
        return parentId;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "name= '" + getName() + '\'' +
                ", description= '" + getDescription() + '\'' +
                ", id= " + getId() +
                ", status= " + getStatus() +
                ", startTime= " + (getStartTime() != null ? getStartTime() : "not set") +
                ", duration= " + (getDuration() != null ? getDuration().toHours() + " hours " +
                getDuration().toMinutesPart() + " minutes" : "not set") +
                ", endTime= " + getEndTime() +
                '}';
    }

    @Override
    public TaskType getType() {
        return TaskType.SUBTASK;
    }
}
