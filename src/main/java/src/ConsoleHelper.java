package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;
/*
 * Additional class for console output
 */
public class ConsoleHelper {
    private static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    //prints to console
    public static void writeMessage(String message) {
        System.out.println(message);
    }

    //reads a string
    public static String readString() {
        while (true) {
            try {
                String result = reader.readLine();
                return result;
            } catch (IOException e) {
                System.out.println("Произошла ошибка при попытке ввода текста. Попробуйте еще раз.");
            }
        }
    }

    //reads a number
    public static int readInt() {
        while (true) {
            try {
                return Integer.parseInt(Objects.requireNonNull(readString()));
            } catch (NumberFormatException e) {
                System.out.println("Произошла ошибка при попытке ввода числа. Попробуйте еще раз.");
            }
        }
    }
}
