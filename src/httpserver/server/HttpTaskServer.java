package httpserver.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import httpserver.adapter.DurationAdapter;
import httpserver.adapter.LocalDateTimeAdapter;
import manager.Managers;
import manager.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;

public class HttpTaskServer {
    private static HttpServer server;
    private static final int PORT = 8080;

    public HttpTaskServer(TaskManager manager) throws IOException {
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/tasks", new TaskHandler(manager));
        server.createContext("/subtasks", new SubtaskHandler(manager));
        server.createContext("/epics", new EpicHandler(manager));
        server.createContext("/history", new HistoryHandler(manager));
        server.createContext("/prioritized", new PrioritizedHandler(manager));
    }

    public void start() {
        server.start();
        System.out.println("Сервер запущен на порте " + PORT);
    }

    public void stop() {
        server.stop(2);
    }

    public static Gson getGson() {
        return new GsonBuilder()
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
    }

    public static void main(String[] args) {
        try {
            TaskManager taskManager = Managers.getDefault();
            HttpTaskServer taskServer = new HttpTaskServer(taskManager);
            taskServer.start();
        } catch (IOException e) {
            System.out.println("Ошибка при запуске сервера " + e.getMessage());
        }
    }
}
