package chatjava.common;

import java.io.Serializable;
import java.util.List;

public class Message implements Serializable {
    public static class Login {
        public static class Request extends Message {
            String username;
            String password;

            public Request(String username, String password) {
                this.username = username;
                this.password = password;
            }

            public String getUsername() {
                return username;
            }

            public String getPassword() {
                return password;
            }
        }
        public static class Response extends Message {
            int status;
            String username;

            public Response(int status, String username) {
                this.status = status;
                this.username = username;
            }

            public String getUsername() {
                return username;
            }

            public int getStatus() {
                return status;
            }
        }
    }
    public static class Disconnect {
        public static class Response extends Message {}
        public static class Request extends Message {}
    }
    public static class Shutdown extends Message {}
    public static class JoinRoom {
        public static class Response extends Message {
            String room;
            public Response(String room) {
                this.room = room;
            }

            public String getRoom() {
                return room;
            }
        }
        public static class Request extends Message {
            String from_room, to_room;
            public Request(String from_room, String to_room) {
                this.from_room = from_room;
                this.to_room = to_room;
            }

            public String get_from_room() {
                return this.from_room;
            }

            public String get_to_room() {
                return to_room;
            }
        }
    }
    public static class CreateRoom {
        public static class Request extends Message {
            String name;
            public Request(String name) {
                this.name = name;
            }

            public String getName() {
                return name;
            }
        }
        public static class Response extends Message {
            public Response() {}
        }
    }
    public static class ExitRoom {
        private ExitRoom() {}

        public static class Request extends Message{
            public Request() {

            }
        }
        public static class Response extends Message {
            String from, to;
            public Response(String from, String to) {
                this.from = from;
                this.to = to;
            }

            public String getFrom() {
                return from;
            }

            public String getTo() {
                return to;
            }
        }
    }
    public static class SendTextMessage {
        public static class Request extends Message {
            String user;
            String text;
            public Request(String user, String text) {
                this.user = user;
                this.text = text;
            }

            public String getUser() {
                return user;
            }
            public String getText() {
                return text;
            }
        }

        public static class Response extends Request {
            public Response(String user, String text) {
                super(user, text);
            }
        }
    }
    public static class UpdateTextMessage {
        public static class Request extends Message {
            public Request() {}
        }
        public static class Response extends Message {
            String user;
            String message;

            public Response(String user, String message) {
                this.user = user;
                this.message = message;
            }

            public String getUser() {
                return user;
            }

            public String getMessage() {
                return message;
            }
        }
    }
    public static class Rooms {
        public static class Request extends Message {}
        public static class Response extends Message {
            final List<String> rooms;
            public Response(List<String> rooms) {
                this.rooms = rooms;
            }

            public List<String> getRooms() {
                return rooms;
            }
        }
    }
}
