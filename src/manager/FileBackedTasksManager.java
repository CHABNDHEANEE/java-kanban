package manager;

import manager.exception.ManagerSaveException;
import task.Epic;
import task.Subtask;
import task.Task;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Objects;

public class FileBackedTasksManager extends InMemoryTaskManager implements TaskManager {

    public static void main(String[] args) {
    }

    static void testSaving() {
        TaskManager taskManager = Managers.getDefaultWithSaves();


        Epic epic1 = new Epic("Переезд", "Заняться переездом.");
        taskManager.createEpic(epic1);

        Subtask subtask1 = new Subtask("Упаковать вещи", "Разложить вещи по коробкам.", epic1.getUid());
        Subtask subtask2 = new Subtask("Перевезти вещи", "Увезти все вещи.", epic1.getUid());
        Subtask subtask3 = new Subtask("Распаковать вещи", "Распаковать их.", epic1.getUid());
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);
        taskManager.createSubtask(subtask3);

        Epic epic2 = new Epic("Пополнить запасы", "Сходить в магазин за продуктами.");
        taskManager.createEpic(epic2);


        System.out.println(taskManager.getEpicById(0));
        System.out.println(taskManager.getEpicById(4));
        System.out.println(taskManager.getSubtaskById(2));
        System.out.println(taskManager.getEpicById(0));

        System.out.println(taskManager.getHistory());
        System.out.println(taskManager.getHistory());

        taskManager.save();
    }

    public static void testLoading() {
        TaskManager taskManager = loadFromFile(new File("TaskManager.csv"));
        System.out.println(taskManager.getHistory());
        System.out.println(taskManager.getSubtasks());
    }

    FileBackedTasksManager() {
        super();
    }

    @Override
    public Integer createTask(Task task) {
        super.createTask(task);
        save();
        return task.getUid();
    }

    @Override
    public Integer createSubtask(Subtask subtask) {
        super.createSubtask(subtask);
        save();
        return subtask.getUid();
    }

    @Override
    public Integer createEpic(Epic epic) {
        super.createEpic(epic);
        save();
        return epic.getUid();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public void deleteTaskById(Integer id) {
        super.deleteTaskById(id);
        save();
    }

    @Override
    public void deleteSubtaskById(Integer id) {
        super.deleteSubtaskById(id);
        save();
    }

    @Override
    public void deleteEpicById(Integer id) {
        super.deleteEpicById(id);
        save();
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void setStatus(int uidOfEpic) {
        super.setStatus(uidOfEpic);
        save();
    }


    public void save() {
        try (Writer fileWriter = new FileWriter("TaskManager.csv")) {
            fileWriter.write("id,type,name,status,description,epic \n");
            fileWriter.write(CSVTaskFormat.getAllTasks(this));
            fileWriter.write("\n\n");
            fileWriter.write(CSVTaskFormat.historyToString(getHistoryManager()));
        } catch (IOException e) {
            throw new ManagerSaveException("Произошла ошибка!");
        }

    }


    static FileBackedTasksManager loadFromFile(File file) {
        FileBackedTasksManager taskManager = new FileBackedTasksManager();
        String strFile;
        try {
            strFile = Files.readString(Path.of(file.toURI()));
        } catch (IOException e) {
            System.out.println("Ошибка!");
            strFile = "";
        }

        String[] lines = strFile.split("\n");

        for (int i = 1; i < lines.length; i++) {
            if (lines[i].isBlank()) break;
            taskManager.loadTask(CSVTaskFormat.taskFromString(lines[i]));
        }

        try {
            for (Integer id : CSVTaskFormat.historyFromString(lines[lines.length - 1])) {
                if (taskManager.tasks.containsKey(id)) {
                    taskManager.historyManager.addTask(taskManager.tasks.get(id));
                } else if (taskManager.subtasks.containsKey(id)) {
                    taskManager.historyManager.addTask(taskManager.subtasks.get(id));
                } else if (taskManager.epics.containsKey(id)) {
                    taskManager.historyManager.addTask(taskManager.epics.get(id));
                }
            }
        } catch (Exception e) {} //Если нет истории, то не добавляем ее в новый файл.

        return taskManager;
    }

    public void loadTask(Task task) {
        TaskType type = task.getTaskType();
        switch (type) {
            case TASK:
                tasks.put(task.getUid(), task);
                break;
            case EPIC:
                epics.put(task.getUid(), (Epic) task);
                break;
            case SUBTASK:
                subtasks.put(task.getUid(), (Subtask) task);
                break;
        }

    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TaskManager item = (TaskManager) obj;
        boolean equals = false;

        if (getEpics().equals(item.getEpics())) {
            if (Objects.equals(getSubtasks(), item.getSubtasks())) {
                if (Objects.equals(getTasks(), item.getTasks())) {
                        return true;
                }
            }
        }
        return false;
//        return Objects.equals(getEpics(), item.getEpics())
//                && Objects.equals(getSubtasks(), item.getSubtasks())
//                && Objects.equals(getTasks(), item.getTasks());
    }
}