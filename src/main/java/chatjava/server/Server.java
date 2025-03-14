package chatjava.server;

import chatjava.common.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {
    protected static final Logger LOGGER = LogManager.getLogger("Server");

    private final int port;
    private final List<ClientHandlerRunnable> clients = new ArrayList<>();
    private final HashMap<String, List<ClientHandlerRunnable>> rooms = new HashMap<>();
    private ServerSocket serverSocket;
    private Thread connectionThread;
    private boolean isStarted;

    public Server(int port) {
        this.port = port;
        this.rooms.put("staging", new ArrayList<>());
        this.isStarted = false;
        LOGGER.info("Test log statement");
    }

    public void start() {
        connectionThread = new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                serverSocket.setReuseAddress(true);
                LOGGER.info("Server listening on port 3333");
                LOGGER.info("Started server");
                System.out.println("Server started: type help");

                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    synchronized (clients) {
                        ClientHandlerRunnable clientHandler = new ClientHandlerRunnable(clientSocket, clients, rooms);
                        clients.add(clientHandler);
                        rooms.get("staging").add(clientHandler);
                        new Thread(clientHandler).start();
                        clientHandler.writer.writeObject(new Message.JoinRoom.Response("staging"));
                        clientHandler.currentlyJoinedRoom = "staging";
                        LOGGER.info("New connection: {}", clientSocket.getInetAddress());
                    }
                }
            } catch (IOException e) {
                System.err.println("Failed to open socket");
            }
        });
        connectionThread.start();
        this.isStarted = true;
    }

    public void stop() {
        // shutdown code
        synchronized (clients) {
            for (ClientHandlerRunnable handler : clients) {
                try {
                    handler.writer.writeObject(new Message.Shutdown());
                } catch (IOException e) {
                    LOGGER.error("Failed to send disconnect message to client socket");
                }
                handler.shutdown();
            }
            clients.clear();
        }
        try {
            serverSocket.close();
        } catch (IOException e) {
            LOGGER.error("Failed to close server socket");
        }
    }

    public void runServerLogicLoop() {
        if (!isStarted) throw new IllegalStateException("Server must be started via Server#start() before running the logic loop");
        try (Scanner scanner = new Scanner(System.in)) {
            scanner.nextLine();
            while (true) {
                System.out.print("> ");
                String command = scanner.nextLine().trim();
                if (command.equalsIgnoreCase("help")) {
                    serverHelpMessage();
                }
                else if (command.equalsIgnoreCase("stop")) {
                    stop();
                    System.out.println("Stopping java.chatjava.server...");
                    break;
                }
                else if (command.equalsIgnoreCase("list_clients")) {
                    System.out.println(clients.size() + " connected.");
                    for (ClientHandlerRunnable handler : clients) {
                        System.out.println("Client: " + handler.socket.getInetAddress());
                    }
                }
                else if (command.equalsIgnoreCase("list_rooms")) {
                    System.out.println(rooms.size() + " rooms");
                    for (Map.Entry<String, List<ClientHandlerRunnable>> room : rooms.entrySet()) {
                        System.out.println("Room: " + room.getKey());
                        for (ClientHandlerRunnable clientHandler : room.getValue()) {
                            System.out.println("\t" + clientHandler.socket.getInetAddress());
                        }
                    }
                }
                else {
                    serverHelpMessage();
                }
            }
        }

        try {
            connectionThread.join();
            LOGGER.info("Joined connection thread");
        } catch (InterruptedException e) {
            LOGGER.error("Failed to join connection thread");
        }
    }

    private void serverHelpMessage() {
        System.out.println("  help");
        System.out.println("  stop");
        System.out.println("  list_clients");
        System.out.println("  list_rooms");
    }
}
