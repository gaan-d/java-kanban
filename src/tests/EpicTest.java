package tests;

import org.junit.jupiter.api.Test;
import task.Epic;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EpicTest {

    @Test
    public void EpicsWithEqualIdShouldBeEqual() {
        Epic epic1 = new Epic( "Эпик 1", "Описание");
        epic1.setId(10);
        Epic epic2 = new Epic( "Эпик 2", "Описание 2");
        epic2.setId(10);
        assertEquals(epic1, epic2, "Эпики не равны");
    }
}
