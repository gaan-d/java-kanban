import manager.Managers;
import manager.TaskManager;
import task.Epic;
import task.Status;
import task.Subtask;
import task.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        TaskManager manager = Managers.getDefault();

        // Создание
        Task task1 = new Task("Task #1", "Task1 description", Status.NEW,
                LocalDateTime.of(2024, 10, 1, 6, 0), Duration.ofHours(2));
        Task task2 = new Task("Task #2", "Task2 description", Status.IN_PROGRESS,
                LocalDateTime.of(2024, 10, 1, 6, 0), Duration.ofHours(2));
        final int taskId1 = manager.addNewTask(task1);
        final int taskId2 = manager.addNewTask(task2);

        Epic epic1 = new Epic("Epic #1", "Epic1 description");
        Epic epic2 = new Epic("Epic #2", "Epic2 description");
        final int epicId1 = manager.addNewEpic(epic1);
        final int epicId2 = manager.addNewEpic(epic2);

        Subtask subtask1 = new Subtask("Subtask #1-1", "Subtask1 description", Status.NEW,
                LocalDateTime.of(2024, 10, 1, 3, 30), Duration.ofHours(2), epicId1);
        Subtask subtask2 = new Subtask("Subtask #2-1", "Subtask1 description", Status.NEW,
                LocalDateTime.of(2024, 10, 1, 0, 0), Duration.ofHours(1), epicId1);
        Subtask subtask3 = new Subtask("Subtask #3-2", "Subtask1 description", Status.DONE,
                LocalDateTime.of(2024, 10, 1, 9, 0), Duration.ofHours(1), epicId2);
        final Integer subtaskId1 = manager.addNewSubtask(subtask1);
        final Integer subtaskId2 = manager.addNewSubtask(subtask2);
        final Integer subtaskId3 = manager.addNewSubtask(subtask3);

        printAllTasks(manager);

        // Обновление
        final Task task = manager.getTask(taskId2);
        task.setStatus(Status.DONE);
        manager.updateTask(task);
        System.out.println("CHANGE STATUS: Task2 IN_PROGRESS->DONE");
        System.out.println("Задачи:");
        for (Task t : manager.getAllTasks()) {
            System.out.println(t);
        }

        Subtask subtask = manager.getSubtask(subtaskId2);
        subtask.setStatus(Status.DONE);
        manager.updateSubtask(subtask);
        System.out.println("CHANGE STATUS: Subtask2 NEW->DONE");
        subtask = manager.getSubtask(subtaskId3);
        subtask.setStatus(Status.NEW);
        manager.updateSubtask(subtask);
        System.out.println("CHANGE STATUS: Subtask3 DONE->NEW");
        System.out.println("Подзадачи:");
        for (Task t : manager.getAllSubtasks()) {
            System.out.println(t);
        }

        System.out.println("Эпики:");
        for (Task e : manager.getAllEpics()) {
            System.out.println(e);
            for (Subtask t : manager.getEpicSubtasks(e.getId())) {
                System.out.println("--> " + t);
            }
        }
        final Epic epic = manager.getEpic(epicId1);
        epic.setStatus(Status.NEW);
        manager.updateEpic(epic);
        System.out.println("CHANGE STATUS: Epic1 IN_PROGRESS->NEW");
        //printAllTasks(manager);

        System.out.println("Эпики:");
        for (Task e : manager.getAllEpics()) {
            System.out.println(e);
            for (Task t : manager.getEpicSubtasks(e.getId())) {
                System.out.println("--> " + t);
            }
        }

        // Удаление
        System.out.println("DELETE: Task1");
        manager.deleteTask(taskId1);
        System.out.println("DELETE: Epic1");
        manager.deleteEpic(epicId1);
        printAllTasks(manager);

        List<Task> history = manager.getHistory();
        System.out.println("История просмотренных задач: ");
        for (Task taskInHistory : history) {
            System.out.println(taskInHistory);
        }


    }

    private static void printAllTasks(TaskManager manager) {
        System.out.println("Список всех задач:");

        System.out.println("\nОбычные задачи:");
        manager.getTasksMap().values().forEach(System.out::println);

        System.out.println("\nЭпики:");
        for (Task epic : manager.getAllEpics()) {  // Здесь предполагается, что метод getEpics() возвращает список эпиков
            System.out.println(epic);  // Вывод информации об эпике

            List<Subtask> subtasks = manager.getEpicSubtasks(epic.getId());  // Получаем сабтаски для эпика
            if (!subtasks.isEmpty()) {
                for (Subtask subtask : subtasks) {
                    System.out.println("--> " + subtask);  // Выводим информацию о сабтасках
                }
            } else {
                System.out.println("--> У данного эпика нет подзадач.");
            }

            System.out.println("\nПодзадачи:");
            manager.getSubtasksMap().values().forEach(System.out::println);
        }
    }
}