package manager;

import manager.HistoryManager;
import manager.InMemoryHistoryManager;
import manager.InMemoryTaskManager;

public class Managers {
    public static TaskManager getDefault(){
        HistoryManager manager = getDefaultHistory();
        return new InMemoryTaskManager(manager);
    }
    public static InMemoryHistoryManager getDefaultHistory(){return new InMemoryHistoryManager();}
}
