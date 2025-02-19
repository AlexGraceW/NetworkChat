package org.example.Client;

import org.example.Server.ChatServer;

import java.io.*;
import java.net.*;
import java.util.*;


class ChatClient {
    private static final String SETTINGS_FILE = "settings.txt";
    private static final String LOG_FILE = "client.log";
    private static String host;
    private static int port;

    public static void main(String[] args) {
        loadSettings();
        try (Socket socket = new Socket(host, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("Connected to chat server");
            System.out.print("Enter your username: ");
            String username = userInput.readLine();
            out.println(username);

            Thread listener = new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null) {
                        log(serverMessage);
                        System.out.println(serverMessage);
                    }
                } catch (IOException e) {
                    log("Connection closed");
                }
            });
            listener.start();

            String message;
            while ((message = userInput.readLine()) != null) {
                if (message.equalsIgnoreCase("/exit")) {
                    break;
                }
                log(username + ": " + message);
                out.println(message);
            }
        } catch (IOException e) {
            log("Error: " + e.getMessage());
        }
    }

    private static void loadSettings() {
        try (InputStream inputStream = ChatServer.class.getClassLoader().getResourceAsStream(SETTINGS_FILE)) {
            if (inputStream == null) {
                System.out.println("settings.txt not found!");
                port = 12345;
                return;
            } else {
                System.out.println("settings.txt loaded successfully.");
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            port = Integer.parseInt(reader.readLine().trim());
        } catch (IOException e) {
            System.out.println("Error reading settings: " + e.getMessage());
            port = 12345;
        }
    }

    private static void log(String message) {
        try (FileWriter fw = new FileWriter(LOG_FILE, true)) {
            fw.write(new Date() + " " + message + "\n");
        } catch (IOException e) {
            System.out.println("Logging error: " + e.getMessage());
        }
    }
}
