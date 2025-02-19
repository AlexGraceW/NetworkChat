package org.example.Tests;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

class ChatIntegrationTest {
    private static final int TEST_PORT = 12345;
    private ExecutorService serverThread;
    private ServerSocket serverSocket;

    @BeforeEach
    void startServer() throws IOException {
        serverSocket = new ServerSocket(TEST_PORT);
        serverThread = Executors.newSingleThreadExecutor();
        serverThread.submit(() -> {
            try {
                Socket clientSocket = serverSocket.accept(); // Ожидаем подключение клиента
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

                String receivedMessage = in.readLine(); // Читаем сообщение от клиента
                out.write("Echo: " + receivedMessage + "\n"); // Отправляем обратно
                out.flush();

                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @AfterEach
    void stopServer() throws IOException {
        serverSocket.close();
        serverThread.shutdown();
    }

    @Test
    void testClientServerCommunication() throws IOException {
        // Подключаемся к серверу
        Socket clientSocket = new Socket("localhost", TEST_PORT);
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        // Отправляем сообщение серверу
        out.write("Hello Server\n");
        out.flush();

        // Читаем ответ от сервера
        String response = in.readLine();
        assertEquals("Echo: Hello Server", response, "Сервер должен вернуть 'Echo: Hello Server'");

        clientSocket.close();
    }
}

