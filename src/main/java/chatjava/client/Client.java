package chatjava.client;

import chatjava.common.Message;

import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Client {
    // Client Data
    private String ip;
    private int port;

    private String activeRoom;
    private boolean switchingRooms;
    private boolean running;

    // Network data
    private Thread serverMessageListenerThread;
    private Socket socket;
    private ObjectOutputStream objectWriter = null;
    private ObjectInputStream objectReader = null;
    private volatile boolean responseProcessed;

    public Client(String ip, int port) {
        this.activeRoom = "";
        this.ip = ip;
        this.port = port;
        this.switchingRooms = true;
        this.running = true;
        this.responseProcessed = false;
        try {
            this.socket = new Socket(this.ip, this.port);
            this.objectReader = new ObjectInputStream(this.socket.getInputStream());
            this.objectWriter = new ObjectOutputStream(this.socket.getOutputStream());
        } catch (IOException e) {
            System.err.println("Failed to connect to " + this.ip + ":" + this.port);
            System.exit(1);
        }
        this.serverMessageListenerThread = new Thread(() -> {
           while (running) {
               Message message;
               try {
                    if ((message = (Message) objectReader.readObject()) != null) {
                        this.responseProcessed = false;
                        // if (message instanceof Message.Shutdown) handleShutdown();
                        if (message instanceof Message.JoinRoom.Response response) handleJoinResponse(response);
                        if (message instanceof Message.ExitRoom.Response response) handleExitResponse(response);
                        if (message instanceof Message.Disconnect.Response response) {
                            System.out.println("Disconnecting");
                            socket.setSoTimeout(1);
                            running = false;
                            break;
                        }
                        if (message instanceof Message.SendTextMessage.Response response) handleSendTextMessageResponse(response);
                        this.responseProcessed = true;
                    }
               } catch (ClassNotFoundException e) {
                   System.out.println("Failed to read object : " + e.getLocalizedMessage());
               } catch (IllegalMonitorStateException e) {
                   System.out.println("Failed to unlock : " + e.getLocalizedMessage());
               } catch (InterruptedIOException e) {
                   running = false;
                   System.out.println("Interrupted in thread");
                   break;
               } catch (IOException e) {
                   running = false;
                   System.err.println("Server stopped. Client exiting...");
                   System.exit(0);
               }
           }
        });
        this.serverMessageListenerThread.start();
    }

    public void runClientLogicLoop() {
        Scanner scanner = new Scanner(System.in);
        while (running) {
            try {
                while (!responseProcessed) {
                    Thread.onSpinWait();
                }
                System.out.print("[" + activeRoom + "] > ");
                System.out.flush();
                String input = scanner.nextLine();
                String[] tokens = input.split(" ");
                if (tokens[0].equalsIgnoreCase("help")) {
                    clientHelpMessage();
                }
                else if (tokens[0].equalsIgnoreCase("disconnect")) {
                    objectWriter.writeObject(new Message.Disconnect.Request());
                    responseProcessed = false;
                    running = false;
                }
                else if (tokens[0].equalsIgnoreCase("join-room")) {
                    if (tokens.length < 2) {
                        System.out.println("join-room requires a parameter");
                        continue;
                    }
                    String room = tokens[1];
                    objectWriter.writeObject(new Message.JoinRoom.Request(activeRoom, room));
                    responseProcessed = false;
                }
                else if (tokens[0].equalsIgnoreCase("exit-room")) {
                    objectWriter.writeObject(new Message.ExitRoom.Request());
                    responseProcessed = false;
                }
                else if (tokens[0].equalsIgnoreCase("send-message")) {
                    String raw = String.join(" ", Arrays.stream(tokens).skip(1).collect(Collectors.joining(" ")));
                    System.out.println(raw);
                    objectWriter.writeObject(new Message.SendTextMessage.Request(socket.getInetAddress().toString(), raw));
                    responseProcessed = false;
                    System.out.println("Sent send-message request");
                }
                else {
                    clientHelpMessage();
                }
            } catch (IOException e) {
                System.err.println("Failed to write object to network");
            }
        }
        scanner.close();
    }

    private void clientHelpMessage() {
        System.out.println("  help");
        System.out.println("  disconnect");
        System.out.println("  join-room <room>");
        System.out.println("  exit-room");
        System.out.println("  send-message <message>");
    }

    private void handleJoinResponse(Message.JoinRoom.Response response) {
        this.activeRoom = response.getRoom();
        //System.out.println("Joining: " + response.getRoom());
    }

    private void handleExitResponse(Message.ExitRoom.Response response) {
        this.activeRoom = response.getTo();

        // System.out.println("Exiting: " + response.getFrom());
    }

    private void handleSendTextMessageResponse(Message.SendTextMessage.Response response) {
        System.out.println("Text message response");
    }
}
