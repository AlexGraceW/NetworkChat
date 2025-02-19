package org.example.Tests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChatServerTest {
    private static final String TEST_LOG_FILE = "test_server.log";

    @BeforeEach
    void setUp() throws IOException {
        // Очистка файла перед каждым тестом
        Files.deleteIfExists(Paths.get(TEST_LOG_FILE));
    }

    @AfterEach
    void tearDown() throws IOException {
        // Удаляем файл после тестов
        Files.deleteIfExists(Paths.get(TEST_LOG_FILE));
    }

    @Test
    void testLog() throws IOException {
        // Логируем тестовое сообщение
        log("Test message");

        // Проверяем, что сообщение записалось
        List<String> lines = Files.readAllLines(Paths.get(TEST_LOG_FILE));
        assertFalse(lines.isEmpty(), "Файл логов должен содержать данные");
        assertTrue(lines.get(0).contains("Test message"), "Лог должен содержать записанное сообщение");
    }

    // Тестовая реализация метода log, аналогичная той, что в ChatServer
    private static void log(String message) {
        try (FileWriter fw = new FileWriter(TEST_LOG_FILE, true)) {
            fw.write(message + "\n");
        } catch (IOException e) {
            System.out.println("Logging error: " + e.getMessage());
        }
    }
}
