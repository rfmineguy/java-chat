package java.chatjava.common;

import java.io.Serializable;

public class Message implements Serializable {
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
    public static class Text extends Message {
        String text;
        public Text(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }
    }
}
