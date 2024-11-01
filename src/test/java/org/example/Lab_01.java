package org.example;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class Lab_01 {

    private static final Logger logger = LogManager.getLogger(Lab_01.class);

    @Test
    void testImageAPIInitialization() {
        try {
            new ImageAPI();
            logger.info("ImageAPI initialized successfully."); // Логируем успешную инициализацию
        } catch (Exception e) {
            logger.error("Error initializing ImageAPI:", e);
            fail("ImageAPI initialization failed: " + e.getMessage()); // Завершаем тест с ошибкой
        }
    }
}
