package chatjava.client;

public class ClientMain {
    public static void main(String[] args) {
        Client client = new Client("localhost", 3333);
        // Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        //     if (!client.isShutdown()) {
        //         client.handleShutdown();
        //     }
        //     System.exit(1);
        // }));
        client.runClientLogicLoop();
    }
}
