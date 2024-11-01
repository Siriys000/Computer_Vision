package org.example;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.opencv.core.Mat;

import static org.junit.jupiter.api.Assertions.*;
//main class - ImageAPI. It's include all methods from all tasks. In /test you will find 6 tests for 6 labs. In resources/ExampleImages you will find the used image samples, but the paths to them in environment.properties are NOT written, it was designed for my personal system.
public class Lab_06 {
    private static final Logger logger = LogManager.getLogger(Lab_06.class);

    private ImageAPI imageAPI;
    private final Constants constants = new Constants();

    @Test
    void testImageDetectEdges() {
        try {
            imageAPI = new ImageAPI();
        } catch (Exception e) {
            fail("ImageAPI initialization failed: " + e.getMessage());
        }

        String[] imageSuffixes = {".lab06.01", ".lab06.02", ".lab06.03"};

        for (String suffix : imageSuffixes) {
            String inputImagePath = constants.getSourceImagePath(suffix);
            Mat originalImage = imageAPI.loadImage(inputImagePath);
            assertNotNull(originalImage, "Failed to load image " + suffix);
            assertFalse(originalImage.empty(), "Loaded image is empty " + suffix);

            String fileNameWithoutExtension = imageAPI.extractFileName(inputImagePath);
            for (double ratio = 0.5; ratio <= 1.5; ratio += 0.1) {  // Цикл по значениям отношений порогов
                //Для всех трех примеров до 1 показывали б'ольшую визуальную точность и количество граней.
                // Однако при увеличении бывали и всплески детализации.
                Mat detectedEdges = imageAPI.detectEdgesCanny(originalImage, ratio);

                assertNotNull(detectedEdges, "Ошибка детекции границ для " + suffix + " с отношением " + ratio);
                assertFalse(detectedEdges.empty(), "Результат детекции границ пустой для " + suffix + " с отношением " + ratio);

                String saveFileName = fileNameWithoutExtension + "_ratio_" + ratio + ".png";
                imageAPI.saveImage(detectedEdges, ".lab06", saveFileName);
            }
        }
    }
}