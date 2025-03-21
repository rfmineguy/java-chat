package chatjava.server;

import com.google.gson.*;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerState {

    public static class UserData {
        public String username;
        public String password;
        public UserData(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }
    public static class RoomData {
        String name;
        List<String> chats;

        public RoomData(String name) {
            this.name = name;
            this.chats = new ArrayList<>();
        }
    }
    private static final String SERVER_STATE_SAVE_FILE = "server_state.json";
    private HashMap<String, RoomData> rooms;
    private HashMap<String, UserData> users;
    private List<ClientHandlerRunnableWithHooks> connections;

    public ServerState() {
        this.rooms = new HashMap<>();
        this.users = new HashMap<>();
        this.connections = new ArrayList<>();
    }

    public void submitNewChat(String room, String user, String message) {
        if (!rooms.containsKey(room)) rooms.put(room, new RoomData(room));
        rooms.get(room).chats.add(user + ":" + message);
    }


    public void addClientRunnable(ClientHandlerRunnableWithHooks clientHandler) {
        this.connections.add(clientHandler);
    }

    public List<ClientHandlerRunnableWithHooks> getConnections() {
        return connections;
    }

    public static void saveServerState(ServerState serverState) {
        String json = Server.prettyGson.toJson(serverState);

        try(FileWriter fw = new FileWriter(ServerState.SERVER_STATE_SAVE_FILE)) {
            fw.write(json);
        } catch (IOException e) {
            System.err.println("Failed to open file");
        }
        System.out.println("Saved server state...");
    }

    public static ServerState loadServerState() {
        StringBuilder sb = new StringBuilder();
        try(BufferedReader fr = new BufferedReader(new FileReader(ServerState.SERVER_STATE_SAVE_FILE))) {
            String line;
            while ((line = fr.readLine()) != null) {
                sb.append(line);
            }
            System.out.println("Loaded server state... " + sb.toString());
            return Server.gson.fromJson(sb.toString(), ServerState.class);
        } catch (IOException e) {
            System.err.println("Failed to open file: " + e.getLocalizedMessage());
            return null;
        }
    }

    public void registerNewUser(String username, String password) {
        users.put(username, new UserData(username, password));
    }
    public boolean tryLoginUser(String username, String password) {
        return users.containsKey(username) && users.get(username).password.equals(password);
    }

    public final HashMap<String, RoomData> getRooms() {
        return rooms;
    }


    public static class Serializer implements JsonSerializer<ServerState>, JsonDeserializer<ServerState> {
        @Override
        public JsonElement serialize(ServerState src, Type typeOfSrc, JsonSerializationContext context) {
            // System.out.println("Serializing server state");
            JsonObject serverStateWrapper = new JsonObject();

            // chat rooms
            JsonObject element = new JsonObject();
            for (Map.Entry<String, RoomData> entry : src.rooms.entrySet()) {
                RoomData roomData = entry.getValue();
                JsonArray roomJsonArray = new JsonArray();
                for (String chatLine : roomData.chats) {
                    roomJsonArray.add(chatLine);
                }
                element.add(roomData.name, roomJsonArray);
            }
            serverStateWrapper.add("rooms", element);

            // users
            JsonArray users = new JsonArray();
            for (Map.Entry<String, UserData> entry : src.users.entrySet()) {
                JsonObject user = new JsonObject();
                user.addProperty("username", entry.getValue().username);
                user.addProperty("password", entry.getValue().password);
                users.add(user);
            }
            serverStateWrapper.add("users", users);
            return serverStateWrapper;
        }

        @Override
        public ServerState deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            System.out.println("Deserialize");
            System.out.println(json);
            return new ServerState();
        }
    }
}
