package org.example.client;

import org.example.Data;

public interface ServerListener {

  void onMessageReceived(Data message);

  void onError(Throwable error);
}
