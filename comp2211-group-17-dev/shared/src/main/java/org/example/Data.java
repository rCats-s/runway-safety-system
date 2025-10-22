package org.example;

import java.io.Serializable;

// this is just for now idk what we will end up using.
// due to the set-up we will be able to transmit any object given to the socket.
public class Data implements Serializable {
    String message;
    Serializable value;

    public Data(String message, Serializable value) {
        this.message = message;
        this.value = value;
    }

    public String getMessage() {
        return message;
    }

    public Serializable getValue() {
        return value;
    }
}