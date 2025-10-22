package org.example.server;

import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerController {
    private final int port;
    private final List<ConnectionManager> clients = new ArrayList<>();
    private final HashMap<String, List<ConnectionManager>> rooms = new HashMap<>();
    private final ExecutorService threadPool = Executors.newCachedThreadPool();
    private final ServerView view;

    public ServerController(int port, ServerView view) {
        this.port = port;
        this.view = view;
    }

    public void startSocketServer() {

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            view.log("Server is listening on port " + port);

            while (true) {
                Socket socket = serverSocket.accept();

                threadPool.execute(new ConnectionManager(socket, this));
            }
        } catch (IOException e) {
            view.logErr("Server could not be started");
            throw new RuntimeException("Server error: " + e.getMessage(), e);
        }
    }

    public void broadcast(Serializable msg) {
        synchronized (clients) {
            clients.forEach(c -> c.send(msg));
        }
    }

    public void broadcastExcl(Serializable msg, ConnectionManager... excl) {
        synchronized (clients) {
            Set<ConnectionManager> excluded = new HashSet<>(Arrays.asList(excl));
            clients.stream()
                    .filter(c -> !excluded.contains(c))
                    .forEach(c -> c.send(msg));
        }
    }

    public void joinRoom(String room, ConnectionManager conn) {
        if (rooms.containsKey(room)) {
            rooms.get(room).add(conn);
        } else {
            ArrayList<ConnectionManager> items = new ArrayList<>();
            items.add(conn);
            rooms.put(room, items);
        }
    }

    public void leaveRoom(String room, ConnectionManager conn) {
        rooms.get(room).remove(conn);
    }

    public void broadcastToRoom(String room, Serializable msg) {
        rooms.getOrDefault(room, new ArrayList<>()).forEach(c -> c.send(msg));
    }

    public void broadcastToRoomExcl(String room, Serializable msg, ConnectionManager... excl) {
        Set<ConnectionManager> excluded = new HashSet<>(Arrays.asList(excl));
        rooms.getOrDefault(room, new ArrayList<>()).stream()
                .filter(c -> !excluded.contains(c))
                .forEach(c -> c.send(msg));
    }

    public void onConnected(ConnectionManager handler) {
        synchronized (clients) {
            clients.add(handler);
        }
        view.log("New client connected");
    }

    public void onDisconnected(ConnectionManager handler) {
        synchronized (clients) {
            clients.remove(handler);
        }
        for (String room : handler.getJoinedRooms()) {
            rooms.get(room).remove(handler);
        }
        view.log("Client disconnected");
    }

    public ServerView getView() {
        return view;
    }
}