package bg.sofia.uni.fmi.mjt.splitwise.client.io;

import java.util.Scanner;

public class ConsoleUI implements UserInterface {
    @Override
    public String read() {
        Scanner scanner = new Scanner(System.in);

        return scanner.nextLine();
    }

    @Override
    public void write(String text) {
        System.out.println(text);
    }

    @Override
    public void writeError(String error) {
        System.err.println(error);
    }
}
