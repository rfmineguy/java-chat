package client;

import common.Message;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class ClientMain {
    private static ObjectOutputStream objectWriter = null;
    private static ObjectInputStream objectReader = null;
    private static Socket clientSocket = null;

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                objectWriter.writeObject(new Message.Disconnect());
            } catch (IOException e) {
                System.err.println("Failed to send client disconnect on shutdown");
            }
        }));

        Scanner scanner = null;
        try {
            clientSocket = new Socket("localhost", 3333);
            System.out.println("Opened client socket");
            objectReader = new ObjectInputStream(clientSocket.getInputStream());
            objectWriter = new ObjectOutputStream(clientSocket.getOutputStream());
            Thread shutdownListener = new Thread(() -> {
                System.out.println("Shutdown listener started");
                while (!clientSocket.isClosed()) {
                    Message message;
                    try {
                        while ((message = (Message) objectReader.readObject()) != null) {
                            if (message instanceof Message.Shutdown) {
                                System.exit(0);
                            }
                        }
                    } catch (IOException | ClassNotFoundException e) {
                        System.err.println("Failed to read object: " + e.getMessage());
                    }
                }
            });
            shutdownListener.start();
            scanner = new Scanner(System.in);
            while (true) {
                System.out.print("Enter command: ");
                String input = scanner.nextLine();
                if (input.equalsIgnoreCase("disconnect")) {
                    objectWriter.writeObject(new Message.Disconnect());
                    break;
                }
            }
        } catch (UnknownHostException e) {
            System.err.println("Unknown host: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("IO Exception: " + e.getMessage());
        }
    }
}
