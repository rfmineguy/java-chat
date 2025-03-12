package server;

import common.Message;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ServerMain {
    static final List<ClientHandlerRunnable> clients = new ArrayList<>();
    static ServerSocket serverSocket;

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(ServerMain::serverShutdown));

        // setup thread for continuous connection listening
        Thread connectionThread = new Thread(() -> {
            try {
                serverSocket = new ServerSocket(3333);
                serverSocket.setReuseAddress(true);
                System.out.println("Server listening on port 3333");

                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    synchronized (clients) {
                        ClientHandlerRunnable clientHandler = new ClientHandlerRunnable(clientSocket, clients);
                        clients.add(clientHandler);
                        new Thread(clientHandler).start();
                        System.out.println("New connection: " + clientSocket.getInetAddress());
                    }
                }
            } catch (IOException e) {
                System.err.println("Failed to open socket");
            }
        });
        connectionThread.start();

        // server logic
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("Enter command: ");
                String command = scanner.nextLine().trim();
                if (command.equalsIgnoreCase("help")) {
                    serverHelpMessage();
                }
                else if (command.equalsIgnoreCase("stop")) {
                    serverShutdown();
                    System.out.println("Stopping server...");
                    break;
                }
                else if (command.equalsIgnoreCase("list_clients")) {
                    System.out.println(clients.size() + " connected.");
                    for (ClientHandlerRunnable handler : clients) {
                        System.out.println("Client: " + handler.socket.getInetAddress());
                    }
                }
                else {
                    serverHelpMessage();
                }
            }
        }
        try {
            connectionThread.join();
            System.out.println("Joined thread");
        } catch (InterruptedException e) {
            System.err.println("Failed to join thread");
        }
    }

    private static void serverShutdown() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            System.err.println("Failed to close server socket");
        }
        // shutdown code
        synchronized (clients) {
            for (ClientHandlerRunnable handler : clients) {
                try {
                    handler.writer.writeObject(new Message.Shutdown());
                } catch (IOException e) {
                    System.err.println("Failed to write to client");
                }
                handler.shutdown();
            }
        }
        clients.clear();
    }

    private static void serverHelpMessage() {
        System.out.println("  help");
        System.out.println("  stop");
        System.out.println("  list_clients");
    }
}
