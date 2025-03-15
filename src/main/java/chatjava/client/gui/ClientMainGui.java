package chatjava.client.gui;

import chatjava.client.ClientWithHooks;
import chatjava.common.Message;

public class ClientMainGui {
    public static void main(String[] args) {
        ClientWithHooks client = new ClientWithHooks("localhost", 3333);
        client.addMessageHook(Message.Disconnect.Response.class, (Message.Disconnect.Response response) -> {
            System.out.println("Disconnect");
        });
        client.addMessageHook(Message.SendTextMessage.Response.class, (response) -> {
            System.out.println("Send text message");
        });
        client.addMessageHook(Message.JoinRoom.Response.class, (response) -> {
            System.out.println("Join room");
        });
        client.showMessageHooks();
    }
}
