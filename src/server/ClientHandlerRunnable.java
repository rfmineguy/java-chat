package server;

import common.Message;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class ClientHandlerRunnable implements Runnable {
    Socket socket;
    boolean running;
    List<ClientHandlerRunnable> clientHandlerListRef;

    ObjectOutputStream writer = null;
    ObjectInputStream reader = null;

    public ClientHandlerRunnable(Socket socket, List<ClientHandlerRunnable> clientListRef) {
        this.socket = socket;
        this.running = true;
        this.clientHandlerListRef = clientListRef;

        try {
            writer = new ObjectOutputStream(socket.getOutputStream());
            writer.flush(); // FLUSH HERE to prevent client from blocking!
            reader = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (running) {
            try {
                Message message;
                while ((message = (Message) reader.readObject()) != null) {
                    System.out.println("Received: " + message);
                    if (message instanceof Message.Disconnect) {
                        this.running = false;
                        this.clientHandlerListRef.remove(this);
                        // this.socket.close();
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
            }
        }
        // try {
        //     socket.close();
        // } catch (IOException e) {
        //     System.err.println("Failed to close socket");
        // }
    }

    public void shutdown() {
        this.running = false;
        try {
            socket.close();
        } catch (IOException e) {
            System.err.println("Failed to close client socket");
        }
        System.out.println("Shutdown client handler");
    }
}
