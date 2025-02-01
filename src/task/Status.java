package task;

public enum Status {
    NEW("Новое"),
    IN_PROGRESS("Выполняется"),
    DONE("Выполнено");

    private final String name;

    Status(String name) {
        this.name = name;
    }


    public String toString() {
        return name;
    }
}
