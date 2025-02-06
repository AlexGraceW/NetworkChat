package org.example;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final String SETTINGS_FILE = "settings.txt";
    private static final String LOG_FILE = "server.log";
    private static int port;
    private static Set<ClientHandler> clients = new HashSet<>();

    public static void main(String[] args) {
        loadSettings();
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            log("Server started on port " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
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

    private static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String username;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                out.println("Enter your username:");
                username = in.readLine();
                log(username + " joined the chat.");
                broadcast(username + " joined the chat.");

                String message;
                while ((message = in.readLine()) != null) {
                    if (message.equalsIgnoreCase("/exit")) {
                        break;
                    }
                    log(username + ": " + message);
                    broadcast(username + ": " + message);
                }
            } catch (IOException e) {
                log("Error: " + e.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    log("Error closing socket: " + e.getMessage());
                }
                log(username + " left the chat.");
                broadcast(username + " left the chat.");
                clients.remove(this);
            }
        }

        private void broadcast(String message) {
            for (ClientHandler client : clients) {
                client.out.println(message);
            }
        }
    }
}
