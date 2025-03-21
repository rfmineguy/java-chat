package chatjava.server;

import chatjava.common.Message;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ClientHandlerRunnable implements Runnable {
    Socket socket;
    boolean running;
    List<ClientHandlerRunnable> clientHandlerListRef;
    HashMap<String, List<ClientHandlerRunnable>> roomsListRef;

    String currentlyJoinedRoom;

    ObjectOutputStream writer = null;
    ObjectInputStream reader = null;

    public ClientHandlerRunnable(Socket socket, List<ClientHandlerRunnable> clientListRef, HashMap<String, List<ClientHandlerRunnable>> roomsRef) {
        this.socket = socket;
        this.running = true;
        this.clientHandlerListRef = clientListRef;
        this.roomsListRef = roomsRef;
        this.currentlyJoinedRoom = null;

        try {
            writer = new ObjectOutputStream(socket.getOutputStream());
            writer.flush(); // FLUSH HERE to prevent java.chatjava.client from blocking!
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
                    Server.LOGGER.info("Received message : {}", message);
                    if (message instanceof Message.JoinRoom.Request joinRoom) {
                        // ensure the room exists
                        if (!roomsListRef.containsKey(joinRoom.get_to_room())) roomsListRef.put(joinRoom.get_to_room(), new ArrayList<>());
                        // System.out.println("to: " + joinRoom.get_to_room() + ", from: " + joinRoom.get_from_room());
                        roomsListRef.get(joinRoom.get_to_room()).add(this);
                        roomsListRef.get(joinRoom.get_from_room()).remove(this);
                        currentlyJoinedRoom = joinRoom.get_to_room();
                        writer.writeObject(new Message.JoinRoom.Response(currentlyJoinedRoom));
                    }
                    if (message instanceof Message.ExitRoom.Request exitRoom) {
                        if (!roomsListRef.containsKey(currentlyJoinedRoom)) {
                            Server.LOGGER.error("{} Cannot exit room '{}'", this.socket.getInetAddress(), currentlyJoinedRoom);
                            continue;
                        }
                        roomsListRef.get(currentlyJoinedRoom).remove(this);
                        roomsListRef.get("staging").add(this);

                        writer.writeObject(new Message.ExitRoom.Response(currentlyJoinedRoom, "staging"));
                    }
                    if (message instanceof Message.SendTextMessage.Request text) {
                        Server.LOGGER.info("{} Sent message: '{}'", socket.getInetAddress(), text.getText());

                        writer.writeObject(new Message.SendTextMessage.Response(socket.getInetAddress().toString(), text.getText()));
                        for (ClientHandlerRunnable client : roomsListRef.get(currentlyJoinedRoom)) {
                            if (client == this) continue;
                            client.writer.writeObject(new Message.SendTextMessage.Response(socket.getInetAddress().toString(), text.getText()));
                        }
                    }
                    if (message instanceof Message.Disconnect.Request disconnect) {
                        if (!roomsListRef.containsKey(currentlyJoinedRoom)) {
                            Server.LOGGER.error("Cannot disconnect '{}' does not exist", currentlyJoinedRoom);
                            continue;
                        }
                        if (!roomsListRef.get(currentlyJoinedRoom).contains(this)) {
                            Server.LOGGER.error("Cannot disconnect '{}'. Not connected.", this.socket.getInetAddress());
                            continue;
                        }
                        this.running = false;
                        this.clientHandlerListRef.remove(this);
                        roomsListRef.get(currentlyJoinedRoom).remove(this);

                        writer.writeObject(new Message.Disconnect.Response());
                    }
                    if (message instanceof Message.Rooms.Request roomsRequest) {
                        // TODO: How do we send back the list of rooms in a network efficient manner
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
            Server.LOGGER.error("Failed to close client socket");
        }
        Server.LOGGER.info("Shutdown client handler: {}", this.socket.getInetAddress());
    }
}
