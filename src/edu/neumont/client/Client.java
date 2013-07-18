package edu.neumont.client;

import edu.neumont.server.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * User: Sean Yergensen
 */
public class Client {

    public static final String COMMAND_PROMPT = "/>";

    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", Server.PORT);
            try(PrintWriter writer = new PrintWriter(socket.getOutputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                Scanner scanner = new Scanner(System.in);
                System.out.print(COMMAND_PROMPT);
                while(scanner.hasNext()) {
                    writer.println(scanner.nextLine());
                    writer.flush();
                    String line;
                    while(!"$".equals((line = reader.readLine()))) {
                        System.out.println(line);
                    }
                    System.out.print(COMMAND_PROMPT);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
