package src;
import java.io.Serializable;
/*
 * class for passing a message as an object, implements serialization via a socket
 */
public class Message implements Serializable {
    private final MessageType type; // type from the MessageType enum

    private final String data;

    public Message(MessageType type) {
        this.type = type;
        this.data = null;
    }
    public Message(MessageType type, String data) {
        this.type = type;
        this.data = data;
    }

    public MessageType getType() { // returning type from the MessageType enum
        return type;
    }

    public String getData() {
        return data;
    }

}
