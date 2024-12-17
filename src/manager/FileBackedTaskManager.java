package manager;

import exception.ManagerLoadException;
import exception.ManagerSaveException;
import task.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File saveFile;

    public FileBackedTaskManager(File saveFile) {
        super(Managers.getDefaultHistory());
        this.saveFile = saveFile;
    }

    public static FileBackedTaskManager loadFromFile(File saveFile) {
        if (!saveFile.exists()) {
            throw new ManagerLoadException("Файла не существует.");
        }
        FileBackedTaskManager backedTaskManager = new FileBackedTaskManager(saveFile);
        try (BufferedReader br = Files.newBufferedReader(saveFile.toPath())) {
            String line = br.readLine();
            while (br.ready()) {
                line = br.readLine();
                if (line.isEmpty()) {
                    break;
                }
                Task task = backedTaskManager.taskFromString(line);
                backedTaskManager.addTaskFromFile(task);
            }
        } catch (IOException e) {
            throw new ManagerLoadException("Ошибка чтения");
        }
        return backedTaskManager;
    }

    private void addTaskFromFile(Task task) {
        int id = task.getId();
        if (super.taskIdCounter < id) {
            taskIdCounter = id;
        }
        if (task instanceof Epic epic) {
            epics.put(epic.getId(), epic);
        } else if (task instanceof Subtask subtask) {
            int epicId = subtask.getParentId();
            Epic epic = epics.get(epicId);
            if (epic != null) {
                subtasks.put(subtask.getId(), subtask);
                epic.addSubtaskId(subtask.getId());
            } else {
                throw new ManagerLoadException("Ошибка при восстановлении подзадачи");
            }
        } else {
            tasks.put(task.getId(), task);
        }
    }

    private String taskToString(Task task) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.join(",",
                String.valueOf(task.getId()),
                task.getType().name(),
                task.getName(),
                task.getStatus().toString(),
                task.getDescription()
        ));
        if (task.getType() == TaskType.SUBTASK) {
            sb.append(",").append(((Subtask) task).getParentId());
        }
        return sb.toString();
    }

    private void save() {
        if (!Files.exists(saveFile.toPath())) {
            throw new ManagerSaveException("Файл записи не существует");
        }

        try (Writer fileWriter = new FileWriter(saveFile, StandardCharsets.UTF_8)) {
            fileWriter.write("id,type,name,status,description,epic\n");
            for (Task task : getTasks()) {
                fileWriter.write(taskToString(task) + "\n");
            }
            for (Epic epic : getEpics()) {
                fileWriter.write(taskToString(epic) + "\n");
            }
            for (Subtask subtask : getSubtasks()) {
                fileWriter.write(taskToString(subtask) + "\n");
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения");
        }
    }

    private Task taskFromString(String str) {
        Task task = null;
        String[] taskFields = str.split(",");
        Status status = switch (taskFields[3]) {
            case "Новое" -> Status.NEW;
            case "Выполняется" -> Status.IN_PROGRESS;
            case "Выполнено" -> Status.DONE;
            default -> throw new IllegalStateException("Неожиданное значение статуса " + taskFields[3]);
        };
        TaskType tasktype = TaskType.valueOf(taskFields[1]);
        if (tasktype == TaskType.TASK) {
            task = new Task(Integer.parseInt(taskFields[0]), taskFields[2], taskFields[4], status);
        } else if (tasktype == TaskType.SUBTASK) {
            task = new Subtask(Integer.parseInt(taskFields[0]), taskFields[2], taskFields[4], status, Integer.parseInt(taskFields[5]));
        } else if (tasktype == TaskType.EPIC) {
            task = new Epic(Integer.parseInt(taskFields[0]), taskFields[2], taskFields[4], status);
        }
        return task;
    }

    @Override
    public int addNewTask(Task task) {
        int id = super.addNewTask(task);
        save();
        return id;
    }

    @Override
    public int addNewEpic(Epic epic) {
        int id = super.addNewEpic(epic);
        save();
        return id;
    }

    @Override
    public int addNewSubtask(Subtask subtask) {
        int id = super.addNewSubtask(subtask);
        super.updateEpic(getEpic(subtask.getParentId()));
        save();
        return id;
    }

    @Override
    public Task updateTask(Task task) {
        Task updatedTask = super.updateTask(task);
        save();
        return updatedTask;
    }

    @Override
    public Epic updateEpic(Epic epic) {
        Epic updatedEpic = super.updateEpic(epic);
        save();
        return updatedEpic;
    }

    @Override
    public Subtask updateSubtask(Subtask subtask) {
        Subtask updatedSubtask = super.updateSubtask(subtask);
        save();
        return updatedSubtask;
    }

    @Override
    public Task deleteTask(int id) {
        super.deleteTask(id);
        save();
        return null;
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }

    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);
        save();
    }

    @Override
    public void deleteTasks() {
        super.deleteTasks();
        save();
    }

    @Override
    public void deleteSubtasks() {
        super.deleteSubtasks();
        save();
    }

    @Override
    public void deleteEpics() {
        super.deleteEpics();
        save();
    }
}
