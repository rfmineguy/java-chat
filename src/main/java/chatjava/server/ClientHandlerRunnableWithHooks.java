package chatjava.server;

import chatjava.common.Message;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.function.Consumer;

public class ClientHandlerRunnableWithHooks implements Runnable {
    private HashMap<Class<? extends Message>, Consumer<? super Message>> messageHooks;
    private volatile boolean running;

    String currentlyJoinedRoom;
    String username;

    Socket socket;
    ObjectOutputStream writer = null;
    ObjectInputStream reader = null;

    public ClientHandlerRunnableWithHooks(Socket socket) {
        this.socket = socket;
        this.running = true;
        this.messageHooks = new HashMap<>();
        this.username = "unpopulated";

        try {
            writer = new ObjectOutputStream(socket.getOutputStream());
            writer.flush();
            reader = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.err.println("Failed to initialize writer/reader");
        }
    }

    @Override
    public void run() {
        while (running) {
            try {
                Message message;
                while ((message = (Message) reader.readObject()) != null) {
                    if (messageHooks.containsKey(message.getClass())) {
                        messageHooks.get(message.getClass()).accept(message);
                    }
                    else {
                        System.err.println("No hook for message: " + message.getClass());
                    }
                }
            } catch (IOException e) {
                System.err.println("Failed to read socket");
                running = false;
            } catch (ClassNotFoundException e) {
                System.err.println("Failed to read object from socket");
                running = false;
            }
        }

    }

    public <T extends Message> void addMessageHook(Class<T> messageClass, Consumer<? super T> consumer) {
        if (messageHooks.containsKey(messageClass)) {
            System.err.println("There is already a message hook for: " + messageClass);
            return;
        }
        messageHooks.put(messageClass, (Consumer<? super Message>) consumer);
    }

    public void showMessageHooks() {
        messageHooks.forEach((aClass, consumer) -> {
            System.out.println(aClass);
        });
    }

    public void shutdown() {
        this.running = false;
        try {
            socket.close();
        } catch (IOException e) {
            System.err.println("Failed to close socket");
        }
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
