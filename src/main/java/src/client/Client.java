package src.client;

import src.Connection;
import src.ConsoleHelper;
import src.Message;
import src.MessageType;

import java.io.IOException;
import java.net.Socket;

public class Client {
    protected Connection connection;
    private volatile boolean clientConnected;

    protected String getServerAddress() {
        ConsoleHelper.writeMessage("Enter server address:");
        return ConsoleHelper.readString();
    }

    protected int getServerPort() {
        ConsoleHelper.writeMessage("Enter server port:");
        return ConsoleHelper.readInt();
    }

    protected String getUserName() {
        ConsoleHelper.writeMessage("Enter your name:");
        return ConsoleHelper.readString();
    }

    protected SocketThread getSocketThread() {
        return new SocketThread();
    }

    protected void sendTextMessage(String text) {
        try {
            connection.send(new Message(MessageType.TEXT, text));
        } catch (IOException e) {
            ConsoleHelper.writeMessage("Failed to send message");
            clientConnected = false;
        }
    }

    protected boolean shouldSendTextFromConsole() {
        return true;
    }

    public void run() {
        SocketThread socketThread = getSocketThread();
        // Помечаем поток как daemon
        socketThread.setDaemon(true);
        socketThread.start();

        try {
            synchronized (this) {
                wait();
            }
        } catch (InterruptedException e) {
            ConsoleHelper.writeMessage("An error occurred while the client was running.");
            return;
        }

        if (clientConnected)
            ConsoleHelper.writeMessage("The connection has been established. To exit, type 'exit'.");
        else
            ConsoleHelper.writeMessage("An error occurred while the client was running.");

        // Пока не будет введена команда exit, считываем сообщения с консоли и отправляем их на сервер
        while (clientConnected) {
            String text = ConsoleHelper.readString();
            if (text.equalsIgnoreCase("exit"))
                break;

            if (shouldSendTextFromConsole())
                sendTextMessage(text);
        }
    }

    public class SocketThread extends Thread {
        protected void clientHandshake() throws IOException, ClassNotFoundException {
            while (true) {
                Message message = Client.this.connection.receive();
                if (message.getType() == MessageType.NAME_REQUEST) {
                    connection.send(new Message(MessageType.USER_NAME, getUserName()));
                } else if (message.getType() == MessageType.NAME_ACCEPTED) {
                    notifyConnectionStatusChanged(true);
                    return;
                } else throw new IOException("Unexpected Message Type");
            }
        }

        // main cycle of the message processing
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            while (true) {
                Message message = Client.this.connection.receive();
                if (message.getType() == MessageType.TEXT) {
                    processIncomingMessage(message.getData());
                } else if (message.getType() == MessageType.USER_ADDED) {
                    informAboutAddingNewUser(message.getData());
                } else if (message.getType() == MessageType.USER_REMOVED) {
                    informAboutDeletingNewUser(message.getData());
                } else throw new IOException("Unexpected Message Type");
            }
        }

        protected void processIncomingMessage(String message) {
            // Выводим текст сообщения в консоль
            ConsoleHelper.writeMessage(message);
        }

        protected void informAboutAddingNewUser(String userName) {
            ConsoleHelper.writeMessage("User '" + userName + "' joined the chat.");
        }

        protected void informAboutDeletingNewUser(String userName) {
            ConsoleHelper.writeMessage("User '" + userName + "' left the chat.");
        }

        protected void notifyConnectionStatusChanged(boolean clientConnected) {
            Client.this.clientConnected = clientConnected;
            synchronized (Client.this) {
                Client.this.notify();
            }
        }

        @Override
        public void run() {
            try {
                String address = getServerAddress();
                int port = getServerPort();
                Client.this.connection = new Connection(new Socket(address, port));
                clientHandshake();
                clientMainLoop();
            } catch (IOException | ClassNotFoundException e) {
                notifyConnectionStatusChanged(false);
            }

        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }
}
