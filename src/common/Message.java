package common;

import java.io.Serializable;

public class Message implements Serializable {
    public static class Disconnect extends Message {}
    public static class Shutdown extends Message {}
}
