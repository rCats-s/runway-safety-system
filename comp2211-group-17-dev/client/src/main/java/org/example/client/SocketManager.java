package org.example.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import org.example.Data;

public class SocketManager {
  private static SocketManager instance;

  private final String hostname;
  private final int port;
  private Socket socket;
  private ObjectOutputStream output;
  private ObjectInputStream input;
  private ArrayList<ServerListener> listeners;
  private boolean isRunning = true;

  public SocketManager() {
    this.listeners = new ArrayList<>();
    this.hostname = "localhost";
    this.port = 12345;
  }

  public static SocketManager getInstance() {
    if (instance == null) instance = new SocketManager();
    return instance;
  }

  public void connect() throws IOException {
    this.socket = new Socket(hostname, port);
    this.output = new ObjectOutputStream(socket.getOutputStream());
    this.input = new ObjectInputStream(socket.getInputStream());

    new Thread(this::listen).start();
  }

  public void send(Data data) throws IOException {
    output.writeObject(data);
    output.flush();
    output.reset(); // Reset to clear serialization cache
  }

  private void listen() {
    try {
      while (isRunning) {
        //this is throwing -1 so EOF
        Data message = (Data) input.readObject();
        for (var listener : listeners) {
          listener.onMessageReceived(message);
        }
      }
    } catch (IOException | ClassNotFoundException e) {
      for (var listener : listeners) {
        listener.onError(e);
      }
    } finally {
      disconnect();
    }
  }

  public void disconnect() {
    isRunning = false;
    try {
      if (socket != null) {
        socket.close();
      }
      if (output != null) {
        output.close();
      }
      if (input != null) {
        input.close();
      }
    } catch (IOException e) {
      for (var listener : listeners) {
        listener.onError(e);
      }
    }
  }

  public void addListener(ServerListener serverListener) {
    listeners.add(serverListener);
  }

  public void removeListener(ServerListener serverListener) {
    listeners.remove(serverListener);
  }
}