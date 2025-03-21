package chatjava.client.gui;

import chatjava.client.ClientWithHooks;
import chatjava.common.Message;

public class ClientMainGui {
    private static ClientGui clientGui;

    static ClientWithHooks client = new ClientWithHooks("localhost", 3333);
    public static void main(String[] args) {
        clientGui = new ClientGui(client); // start up the client gui
        client.addMessageHook(Message.Disconnect.Response.class, (Message.Disconnect.Response response) -> {
            System.out.println("Disconnect");
            client.setSocketTimeout(1);
            client.shouldTerminate = true;
            System.exit(0);
        });
        client.addMessageHook(Message.Login.Response.class, (response) -> {
            if (response.getStatus() == 0) {
                System.out.println("Accepted login: " + response.getUsername());
                client.setUsername(response.getUsername());
                clientGui.acceptLogin();
            }
            else {
                System.err.println("Denied login");
                clientGui.denyLogin();
            }
        });
        client.addMessageHook(Message.SendTextMessage.Response.class, (response) -> {
            System.out.println("Send text message");
        });
        client.addMessageHook(Message.UpdateTextMessage.Response.class, (response) -> {
            clientGui.addChatEntry(response.getUser(), response.getMessage());
        });
        client.addMessageHook(Message.JoinRoom.Response.class, (response) -> {
            System.out.println("Joining room: " + response.getRoom());
            clientGui.updateTitleWithRoom(response.getRoom());
            client.sendNetworkMessage(new Message.UpdateTextMessage.Request());
        });
        client.addMessageHook(Message.Rooms.Response.class, (response) -> {
            System.out.println("Rooms: " + response.getRooms());
            clientGui.updateRooms(response);
        });
        client.addMessageHook(Message.CreateRoom.Response.class, (response) -> {
            client.sendNetworkMessage(new Message.Rooms.Request());
        });
        client.showMessageHooks();
        client.sendNetworkMessage(new Message.Rooms.Request());
    }
}
