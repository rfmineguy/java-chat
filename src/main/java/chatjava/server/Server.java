package chatjava.server;

import chatjava.common.Message;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {
    protected static final Logger LOGGER = LogManager.getLogger("Server");
    public static Gson gson;
    public static Gson prettyGson;

    private final int port;
    ServerState serverState;
    private final List<ClientHandlerRunnableWithHooks> clients = new ArrayList<>();
    private final HashMap<String, List<ClientHandlerRunnableWithHooks>> rooms = new HashMap<>();
    private ServerSocket serverSocket;
    private Thread saveThread, connectionThread;
    private boolean isStarted;

    public Server(int port) {
        this.port = port;
        this.rooms.put("staging", new ArrayList<>());
        this.isStarted = false;
        Server.prettyGson = new GsonBuilder().registerTypeAdapter(ServerState.class, new ServerState.Serializer()).setPrettyPrinting().create();
        Server.gson = new GsonBuilder().registerTypeAdapter(ServerState.class, new ServerState.Serializer()).create();
        this.serverState = ServerState.loadServerState();
        if (this.serverState == null) {
            this.serverState = new ServerState();
            this.serverState.registerNewUser("test", "test");
            this.serverState.registerNewUser("rf", "booger");
        }

        System.out.println(Server.gson.toJson(this.serverState));
    }

    public void start() {
        saveThread = new Thread(() -> {
            do {
                try {
                    Thread.sleep(2 * 1000);
                    ServerState.saveServerState(serverState);
                } catch (InterruptedException e) {
                    System.err.println("Failed to sleep save thread");
                    break;
                }
            } while (isStarted);
        });
        saveThread.start();
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
                        ClientHandlerRunnableWithHooks clientHandler = new ClientHandlerRunnableWithHooks(clientSocket);
                        clientHandler.addMessageHook(Message.JoinRoom.Request.class, (request) -> {
                            System.out.println("Join room request");
                            if (!rooms.containsKey(request.get_to_room())) rooms.put(request.get_to_room(), new ArrayList<>());
                            rooms.get(request.get_to_room()).add(clientHandler);
                            rooms.get(request.get_from_room()).remove(clientHandler);
                            clientHandler.currentlyJoinedRoom = request.get_to_room();
                            try {
                                clientHandler.writer.writeObject(new Message.JoinRoom.Response(clientHandler.currentlyJoinedRoom));
                            } catch (IOException e) {
                                System.err.println("JoinRoom: Failed to write response");
                            }
                        });
                        clientHandler.addMessageHook(Message.ExitRoom.Request.class, (request) -> {
                            System.out.println("Exit room request");
                        });
                        clientHandler.addMessageHook(Message.SendTextMessage.Request.class, (request) -> {
                            System.out.println("Receive text message request");
                            System.out.println("Room: " + clientHandler.currentlyJoinedRoom);
                            serverState.submitNewChat(clientHandler.currentlyJoinedRoom, request.getUser(), request.getText());

                            for (ClientHandlerRunnableWithHooks _clientHandler : serverState.getConnections()) {
                                try {
                                    _clientHandler.writer.writeObject(new Message.UpdateTextMessage.Response(request.getUser(), request.getText()));
                                } catch (IOException e) {
                                    System.err.println("Failed to send text message update to: " + _clientHandler.username);
                                }
                            }
                        });
                        clientHandler.addMessageHook(Message.Disconnect.Request.class, (request) -> {
                            System.out.println("Disconnect request");
                            if (!rooms.containsKey(clientHandler.currentlyJoinedRoom)) {
                                Server.LOGGER.error("Cannot disconnect '{}' does not exist", clientHandler.currentlyJoinedRoom);
                                return;
                            }
                            if (!rooms.get(clientHandler.currentlyJoinedRoom).contains(clientHandler)) {
                                Server.LOGGER.error("Cannot disconnect '{}'. Not connected.", clientHandler.socket.getInetAddress());
                                return;
                            }
                            clients.remove(clientHandler);
                            rooms.get(clientHandler.currentlyJoinedRoom).remove(clientHandler);
                            try {
                                clientHandler.writer.writeObject(new Message.Disconnect.Response());
                            } catch (IOException e) {
                                System.err.println("Disconnect: Failed to write response");
                            }
                        });
                        clientHandler.addMessageHook(Message.Rooms.Request.class, (request) -> {
                            System.out.println("Rooms request");
                            try {
                                Message.Rooms.Response response = new Message.Rooms.Response(rooms.keySet().stream().toList());
                                clientHandler.writer.writeObject(response);
                            } catch (IOException e) {
                                System.err.println("Rooms: Failed to write response");
                            }
                        });
                        clientHandler.addMessageHook(Message.CreateRoom.Request.class, (request) -> {
                            try {
                                rooms.put(request.getName(), new ArrayList<>());
                                for (ClientHandlerRunnableWithHooks handler : clients) {
                                    handler.writer.writeObject(new Message.CreateRoom.Response());
                                }
                            } catch (IOException e) {
                                System.err.println("CreateRoom: Failed to write response");
                            }
                        });
                        clientHandler.addMessageHook(Message.Login.Request.class, (request) -> {
                            clientHandler.setUsername(request.getUsername());
                            try {
                                if (serverState.tryLoginUser(request.getUsername(), request.getPassword())) {
                                    clientHandler.writer.writeObject(new Message.Login.Response(0, request.getUsername()));
                                    System.out.println("Login success");
                                }
                                else {
                                    clientHandler.writer.writeObject(new Message.Login.Response(1, "<invalid login>"));
                                    System.out.println("Login failed");
                                }
                            } catch (IOException e) {
                                System.err.println("Login: Failed to write response");
                            }
                        });
                        clientHandler.showMessageHooks();
                        new Thread(clientHandler).start();
                        clients.add(clientHandler);
                        rooms.get("staging").add(clientHandler);
                        serverState.addClientRunnable(clientHandler);
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
            for (ClientHandlerRunnableWithHooks handler : clients) {
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
            isStarted = false;
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
                    for (ClientHandlerRunnableWithHooks handler : clients) {
                        System.out.println("Client: " + handler.socket.getInetAddress());
                    }
                }
                else if (command.equalsIgnoreCase("list_rooms")) {
                    System.out.println(rooms.size() + " rooms");
                    for (Map.Entry<String, List<ClientHandlerRunnableWithHooks>> room : rooms.entrySet()) {
                        System.out.println("Room: " + room.getKey());
                        for (ClientHandlerRunnableWithHooks clientHandler : room.getValue()) {
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
            ServerState.saveServerState(serverState);
            System.out.println("Waiting for save...");
            saveThread.join();
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
