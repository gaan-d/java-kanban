package tests;

import org.junit.jupiter.api.Test;
import task.Status;
import task.Task;


import static org.junit.jupiter.api.Assertions.assertEquals;

public class TaskTest {

    @Test
    public void tasksWithEqualIdShouldBeEqual() {
        Task task1 = new Task("Name", "Description", Status.NEW);
        task1.setId(10);
        Task task2 = new Task("Name", "Description", Status.DONE);
        task2.setId(10);

        assertEquals(task1, task2, "Tasks with equal ids should be equal");
    }
}
