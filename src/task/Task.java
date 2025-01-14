package task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class Task implements Comparable {
    private String name;
    private String description;
    private int id;
    private Status status;
    private Duration duration = Duration.ZERO;
    private LocalDateTime startTime;

    public Task(String name, String description, Status status) {
        this.name = name;
        this.description = description;
        this.status = status;
    }
    public Task( String name, String description, LocalDateTime startTime, Duration duration) {
        this.name = name;
        this.description = description;
        this.duration = duration;
        this.startTime = startTime;
    }
    public Task( String name, String description,Status status, LocalDateTime startTime, Duration duration) {
        this.name = name;
        this.description = description;
        this.duration = duration;
        this.startTime = startTime;
    }


    public Task(int id, String name, String description, Status status, LocalDateTime startTime, Duration duration) {
        this.name = name;
        this.description = description;
        this.id = id;
        this.status = status;
        this.duration = duration;
        this.startTime = startTime;
    }

    public Task(int id, String name, String description, Status status) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
    }

    public Task(String name, String description) {
        this.name = name;
        this.description = description;
        this.status = Status.NEW;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getEndTime() {
        return (startTime == null) ? null : startTime.plus(duration);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Task task = (Task) object;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public int hashCode(int taskIdCounter) {
        return (Integer.hashCode(id));
    }

    @Override
    public String toString() {
            return "Task{" +
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
    public int compareTo(Object o){
        Task task = (Task) o;
        return startTime.compareTo(task.startTime);
    }

    public TaskType getType() {
        return TaskType.TASK;
    }
}
