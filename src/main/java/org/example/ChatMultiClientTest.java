package org.example;

import org.junit.jupiter.api.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

class ChatMultiClientTest {
    private static final int TEST_PORT = 54321; // Изменил порт
    private ExecutorService serverThread;
    private ServerSocket serverSocket;
    private final List<Socket> clientSockets = new ArrayList<>();
    private final List<BufferedWriter> clientWriters = new ArrayList<>();
    private final List<BufferedReader> clientReaders = new ArrayList<>();

    @BeforeEach
    void startServer() throws IOException {
        serverSocket = new ServerSocket(TEST_PORT);
        serverThread = Executors.newSingleThreadExecutor();
        serverThread.submit(() -> {
            try {
                List<Socket> clients = new CopyOnWriteArrayList<>();
                while (!serverSocket.isClosed()) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("[SERVER] Новый клиент подключился");
                    clients.add(clientSocket);
                    new Thread(() -> handleClient(clientSocket, clients)).start();
                }
            } catch (IOException ignored) {
            }
        });
    }

    private void handleClient(Socket clientSocket, List<Socket> clients) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

            while (true) {
                String message = in.readLine();
                if (message == null) break; // Клиент отключился
                System.out.println("[SERVER] Получено сообщение: " + message);

                // Рассылаем сообщение всем клиентам
                for (Socket socket : clients) {
                    BufferedWriter clientOut = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    clientOut.write(message + "\n");
                    clientOut.flush();
                }
            }
            clientSocket.close();
        } catch (IOException ignored) {
        }
    }

    @AfterEach
    void stopServer() throws IOException {
        for (Socket socket : clientSockets) {
            if (!socket.isClosed()) socket.close();
        }
        if (!serverSocket.isClosed()) serverSocket.close();
        serverThread.shutdown();
    }

    @Test
    void testMultipleClientsCommunication() throws IOException, InterruptedException, ExecutionException, TimeoutException {
        int clientCount = 3;
        ExecutorService clientPool = Executors.newFixedThreadPool(clientCount);
        List<Future<String>> responses = new ArrayList<>();

        for (int i = 0; i < clientCount; i++) {
            final int clientId = i;
            responses.add(clientPool.submit(() -> runClientTest(clientId)));
        }

        for (int i = 0; i < clientCount; i++) {
            try {
                String response = responses.get(i).get(10, TimeUnit.SECONDS); // Увеличил тайм-аут
                assertTrue(response.contains("Client 0: Hello from Client 0"),
                        "Клиент " + i + " должен получить сообщение от Client 0");
            } catch (TimeoutException e) {
                fail("Тест завис, клиент " + i + " не получил сообщение");
            }
        }

        clientPool.shutdown();
    }

    private String runClientTest(int clientId) throws IOException {
        Socket socket = new Socket("localhost", TEST_PORT);
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        clientSockets.add(socket);
        clientWriters.add(out);
        clientReaders.add(in);

        if (clientId == 0) {
            out.write("Client 0: Hello from Client 0\n");
            out.flush();  // ОБЯЗАТЕЛЬНО ВЫЗЫВАЕМ FLUSH!
            System.out.println("[CLIENT " + clientId + "] Отправлено сообщение на сервер");
        }

        String response = in.readLine();  // НЕ БЛОКИРУЕТСЯ, ЕСЛИ СЕРВЕР ОТПРАВЛЯЕТ \n
        System.out.println("[CLIENT " + clientId + "] Получено сообщение: " + response);
        socket.close();
        return response != null ? response : "";
    }
}
