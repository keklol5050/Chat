package src.client;

import src.ConsoleHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class BotClient extends Client{

    @Override
    protected BotSocketThread getSocketThread() {
        return new BotSocketThread();
    }

    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;
    }

    @Override
    protected String getUserName() {
        return "date_bot_"+ (int) (Math.random() * 100);
    }

    public static void main(String[] args) {
        BotClient bot = new BotClient();
        bot.run();
    }

    public class BotSocketThread extends Client.SocketThread {
        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            sendTextMessage("Hello chat. I'm a bot. I understand the commands: date, day, month, year, time, hour, minutes, seconds.");
            super.clientMainLoop();
        }

        @Override
        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
            if (!message.contains(":")) return;
            String name = message.substring(0, message.indexOf(":")).trim();
            String text = message.substring(message.indexOf(":") + 1).trim();
            String mask = null;
            switch(text) {
                case "date": mask = "d.MM.YYYY"; break;
                case "day": mask = "d"; break;
                case "month": mask = "MMMM"; break;
                case "year": mask = "YYYY"; break;
                case "time": mask = "H:mm:ss"; break;
                case "hour": mask = "H"; break;
                case "minutes": mask = "m"; break;
                case "seconds": mask = "s"; break;
            }
            if (mask != null) {
                sendTextMessage(String.format("Information for %s: %s", name, new SimpleDateFormat(mask).format(Calendar.getInstance().getTime())));
            }
        }
    }
}
