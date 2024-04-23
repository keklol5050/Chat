package src;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    // Sends a message to all users
    public static void sendBroadcastMessage(Message message) {
        for (String name : connectionMap.keySet()) {
            try {
                connectionMap.get(name).send(message);
            } catch (IOException e) {
                ConsoleHelper.writeMessage("Error sending message to " + name);
            }
        }
    }

    // main method
    public static void main(String[] args) {
        int port = ConsoleHelper.readInt();
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            ConsoleHelper.writeMessage("creating server socket is successfully");
            while (true) {
                Socket socket = serverSocket.accept();
                new Handler(socket).start();
            }
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }

    // Protocol for server-client communication
    private static class Handler extends Thread {
        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            String name = null;
            try {
                ConsoleHelper.writeMessage("successfully connected: " + socket.getRemoteSocketAddress());
                Connection connection = new Connection(socket);
                name = serverHandshake(connection);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, name));
                notifyUsers(connection, name);
                serverMainLoop(connection, name);

            } catch (Exception e) {
                ConsoleHelper.writeMessage("error in remote connection");
            } finally {
                if (name!=null) {
                    connectionMap.remove(name);
                    sendBroadcastMessage(new Message(MessageType.USER_REMOVED, name));
                }
            }
            ConsoleHelper.writeMessage("the remote connection was closed");
        }

        // Establishing a connection between the server and the user, returns the username
        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            String name = null;
            while (true) {
                connection.send(new Message(MessageType.NAME_REQUEST));
                Message message = connection.receive();

                if (message.getType() != MessageType.USER_NAME) continue;
                name = message.getData();
                if (name.equals("")) continue;
                boolean isAvailable = false;
                for (String nameCon : connectionMap.keySet()) {
                    if (nameCon.equals(name)) isAvailable = true;
                }
                if (isAvailable) continue;

                connectionMap.put(name, connection);
                connection.send(new Message(MessageType.NAME_ACCEPTED));
                break;
            }
            return name;
        }

        // sends a USER_ADDED message to all users except userName
        private void notifyUsers(Connection connection, String userName) throws IOException {
            for (String key : connectionMap.keySet()) {
                if (key.equals(userName)) continue;
                Message message = new Message(MessageType.USER_ADDED, key);
                connection.send(message);
            }
        }

        // Processing the messages
        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                if (message.getType() == MessageType.TEXT) {
                    String messageData = String.format("%s: %s", userName, message.getData());
                    sendBroadcastMessage(new Message(MessageType.TEXT, messageData));
                } else {
                    ConsoleHelper.writeMessage("error!");
                }
            }
        }

    }
}
