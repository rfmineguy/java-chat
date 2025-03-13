package java.chatjava.client;

import java.io.*;
import java.net.Socket;

public class ClientMain {
    private static ObjectOutputStream objectWriter = null;
    private static ObjectInputStream objectReader = null;
    private static Socket clientSocket = null;
    private static String activeRoom;
    private static boolean switchingRooms = true;

    public static void main(String[] args) {
        Client client = new Client("localhost", 3333);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!client.isShutdown()) {
                client.handleDisconnect();
            }
            System.exit(1);
        }));
        client.runClientLogicLoop();

        /*
        Scanner scanner = null;
        try {
            clientSocket = new Socket("localhost", 3333);
            System.out.println("Opened java.chatjava.client socket");
            objectReader = new ObjectInputStream(clientSocket.getInputStream());
            objectWriter = new ObjectOutputStream(clientSocket.getOutputStream());
            Thread messageListener = new Thread(() -> {
                System.out.println("Shutdown listener started");
                while (!clientSocket.isClosed()) {
                    Message message;
                    try {
                        while ((message = (Message) objectReader.readObject()) != null) {
                            System.out.println("Received: " + message);
                            if (message instanceof Message.Shutdown) {
                                System.exit(0);
                            }
                            if (message instanceof Message.JoinRoom.Response response) {
                                activeRoom = response.getRoom();
                                switchingRooms = false;
                                System.out.println("Switching rooms off");
                            }
                            if (message instanceof Message.ExitRoom.Response response) {
                                activeRoom = response.getRoom();
                                System.out.println("Setting active room : " + response.getRoom());
                            }
                        }
                    } catch (IOException | ClassNotFoundException e) {
                        System.err.println("Failed to read object: " + e.getMessage());
                    }
                }
            });
            messageListener.start();
            scanner = new Scanner(System.in);
            while (true) {
                if (switchingRooms) continue;
                System.out.print("[" + activeRoom + "] Enter command: ");
                String input = scanner.nextLine();
                String[] inputTokens = input.split(" ");
                if (inputTokens[0].equalsIgnoreCase("help")) {
                    clientHelpMessage();
                }
                else if (inputTokens[0].equalsIgnoreCase("disconnect")) {
                    objectWriter.writeObject(new Message.Disconnect());
                    break;
                }
                else if (inputTokens[0].equalsIgnoreCase("join-room")) {
                    if (inputTokens.length < 2) {
                        System.out.println("join-room requires a parameter");
                        switchingRooms = true;
                        continue;
                    }
                    String room = inputTokens[1];
                    objectWriter.writeObject(new Message.JoinRoom.Request(activeRoom, room));
                }
                else if (inputTokens[0].equalsIgnoreCase("exit-room")) {
                    objectWriter.writeObject(new Message.ExitRoom.Request());
                    switchingRooms = true;
                }
                else if (inputTokens[0].equalsIgnoreCase("send-message")) {
                    String raw = String.join(" ", Arrays.stream(inputTokens).skip(1).collect(Collectors.joining(" ")));
                    objectWriter.writeObject(new Message.Text(raw));
                }
                else {
                    clientHelpMessage();
                }
            }
        } catch (UnknownHostException e) {
            System.err.println("Unknown host: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("IO Exception: " + e.getMessage());
        }
         */
    }

    private static void clientHelpMessage() {
        System.out.println("  help");
        System.out.println("  disconnect");
        System.out.println("  join-room <room>");
        System.out.println("  exit-room");
    }
}
