
import org.junit.jupiter.api.Test;
import task.Status;
import task.Subtask;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SubtaskTest {

    @Test
    public void subtasksWithEqualIdShouldBeEqual() {
        Subtask subtask1 = new Subtask("Починить электропроводку", "Купить розетку", Status.NEW, 5);
        subtask1.setId(10);
        Subtask subtask2 = new Subtask("Купить хлеба", "Очень срочно", Status.DONE, 5);
        subtask2.setId(10);
        assertEquals(subtask1, subtask2, "Ошибка");
    }
}
