import manager.InMemoryTaskManager;
import manager.Managers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import task.Epic;
import task.Status;
import task.Task;


public class InMemoryTaskManagerTest extends AbstractTaskManagerTest {

    @BeforeEach
    @Override
    public void setUp() {
        taskManager = new InMemoryTaskManager(Managers.getDefaultHistory());
        task = new Task("Просто задача - 1", "Описание простой задачи - 1");
        epic = new Epic(1, "Эпическая задача - 1", "Описание эпической задачи - 1", Status.NEW);
    }

    @AfterEach
    @Override
    public void finish() {

    }
}
