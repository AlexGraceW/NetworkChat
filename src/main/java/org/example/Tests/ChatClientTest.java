package org.example.Tests;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class ChatClientTest {
    private static final String TEST_SETTINGS_FILE = "test_settings.txt";
    private static final String TEST_LOG_FILE = "test_client.log";

    @BeforeEach
    void setUp() throws IOException {
        // Создаем тестовый файл настроек с портом 12345
        try (FileWriter writer = new FileWriter(TEST_SETTINGS_FILE)) {
            writer.write("12345");
        }
        // Удаляем тестовый лог перед тестами
        Files.deleteIfExists(Paths.get(TEST_LOG_FILE));
    }

    @AfterEach
    void tearDown() throws IOException {
        // Удаляем файлы после тестов
        Files.deleteIfExists(Paths.get(TEST_SETTINGS_FILE));
        Files.deleteIfExists(Paths.get(TEST_LOG_FILE));
    }

    @Test
    void testReadPortFromSettings() throws IOException {
        int port = readPort(TEST_SETTINGS_FILE);
        assertEquals(12345, port, "Порт должен быть 12345");
    }

    private int readPort(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            return Integer.parseInt(reader.readLine().trim());
        }
    }

    @Test
    void testClientLogging() throws IOException {
        logMessage("Hello from client!");

        // Проверяем, что лог-файл содержит сообщение
        assertTrue(Files.exists(Paths.get(TEST_LOG_FILE)), "Файл логов должен существовать");
        String content = Files.readString(Paths.get(TEST_LOG_FILE));
        assertTrue(content.contains("Hello from client!"), "Лог должен содержать сообщение");
    }

    private void logMessage(String message) {
        try (FileWriter fw = new FileWriter(TEST_LOG_FILE, true)) {
            fw.write(message + "\n");
        } catch (IOException e) {
            System.out.println("Logging error: " + e.getMessage());
        }
    }


}
