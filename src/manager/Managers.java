package manager;

public class Managers {
    public static TaskManager getDefault() {
        HistoryManager manager = getDefaultHistory();
        return new InMemoryTaskManager(manager);
    }

    public static InMemoryHistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
