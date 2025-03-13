package java.chatjava.server;

public class ServerMain {

    public static void main(String[] args) {
        java.chatjava.server.Server server = new java.chatjava.server.Server(3333);
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        server.start();
        server.runServerLogicLoop();
        server.stop();
    }
}
