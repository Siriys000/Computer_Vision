package org.example;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;

import static org.junit.jupiter.api.Assertions.*;

public class Lab_05 {
    private static final Logger logger = LogManager.getLogger(Lab_05.class);

    private ImageAPI imageAPI;
    private final Constants constants = new Constants();

    @Test
    public void testFloodFill() {
        try {
            imageAPI = new ImageAPI();
        } catch (Exception e) {
            fail("ImageAPI initialization failed: " + e.getMessage());
        }

        String[] imageSuffixes = {".lab05.01", ".lab05.02", ".lab05.03"};

        for (String suffix : imageSuffixes) {
            String inputImagePath = constants.getSourceImagePath(suffix);
            Mat originalImage = imageAPI.loadImage(inputImagePath);
            assertNotNull(originalImage, "Failed to load image " + suffix);
            assertFalse(originalImage.empty(), "Loaded image is empty " + suffix);

            String fileNameWithoutExtension = imageAPI.extractFileName(inputImagePath);

            for (int x = 0; x < originalImage.cols(); x += originalImage.cols() / constants.FLOODFILL_STEP) { // Шаг в пикселях по горизонтали
                for (int y = 0; y < originalImage.rows(); y += originalImage.rows() / constants.FLOODFILL_STEP) { // Шаг в пикселях по вертикале
                    Point seedPoint = new Point(x, y);

                    // Проверка, что точка находится внутри изображения
                    if (seedPoint.x >= 0 && seedPoint.x < originalImage.cols() &&
                            seedPoint.y >= 0 && seedPoint.y < originalImage.rows()) {
                        Mat filledImage = imageAPI.floodFillImage(originalImage.clone(), seedPoint, null, null, null); // Используем clone, чтобы не менять originalImage
                        assertNotNull(filledImage, "Flood fill returned null for " + suffix + " at " + seedPoint);


                        imageAPI.saveImage(filledImage, ".lab05.01", fileNameWithoutExtension + "_filled_" + seedPoint.x + "_" + seedPoint.y);

                        //imageAPI.showImage(filledImage, "Filled Image - " + fileNameWithoutExtension + " - " + seedPoint.x + ", " + seedPoint.y);

                        // Качественная оценка.Пример: вычисление количества измененных пикселей
                       // Mat diff = new Mat();
                       // Core.absdiff(originalImage, filledImage, diff);
                        //int changedPixels = Core.countNonZero(diff);
                        //logger.info("Number of changed pixels for {} at ({}, {}): {}", suffix, x, y, changedPixels);

                    }
                }
            }
        }
    }

    @Test
    public void testPyrUpDawn() {
        try {
            imageAPI = new ImageAPI();
        } catch (Exception e) {
            fail("ImageAPI initialization failed: " + e.getMessage());
        }

        String[] imageSuffixes = {".lab05.01", ".lab05.02", ".lab05.03"};

        for (String suffix : imageSuffixes) {
            String inputImagePath = constants.getSourceImagePath(suffix);
            Mat originalImage = imageAPI.loadImage(inputImagePath);
            assertNotNull(originalImage, "Failed to load image " + suffix);
            assertFalse(originalImage.empty(), "Loaded image is empty " + suffix);

            String fileNameWithoutExtension = imageAPI.extractFileName(inputImagePath);

            for (int iterations = 1; iterations <= constants.MAX_PYRUPDAWN_ITERATIONS; iterations++){ // Проверяем разное количество итераций
                Mat pyrDownUpImage = imageAPI.getDifferenceAfterPyrDownUp(originalImage, iterations, iterations);

                String saveFileName = fileNameWithoutExtension + "_diff_" + iterations + "x_pyrDownUp" + suffix;

                imageAPI.saveImage(pyrDownUpImage, ".lab05.02", saveFileName);

                //imageAPI.showImage(pyrDownUpImage, "Difference Image " + iterations + "x");
            }
        }
    }

    @Test
    public void testIdentifyRectObj() {
        try {
            imageAPI = new ImageAPI();
        } catch (Exception e) {
            fail("ImageAPI initialization failed: " + e.getMessage());
        }

        String[] imageSuffixes = {".lab05.01", ".lab05.02", ".lab05.03"};

        for (String suffix : imageSuffixes) {
            String inputImagePath = constants.getSourceImagePath(suffix);
            Mat originalImage = imageAPI.loadImage(inputImagePath);
            assertNotNull(originalImage, "Failed to load image " + suffix);
            assertFalse(originalImage.empty(), "Loaded image is empty " + suffix);

            for (double targetWidth = constants.RECTINDTF_MIN_WIDTH; targetWidth <= constants.RECTINDTF_MAX_WIDTH; targetWidth += constants.RECTINDTF_WIDTH_STEP) {
                for (double targetHeight = constants.RECTINDTF_MIN_HEIGHT; targetHeight <= constants.RECTINDTF_MAX_HEIGHT; targetHeight += constants.RECTINDTF_HEIGHT_STEP) {

                    int count = imageAPI.identifyRectangularObjects(originalImage.clone(), targetWidth, targetHeight, constants.RECTINDTF_TOLERANCE);
                    if (count > 0) {
                        System.out.println("Image: " + suffix + ", Objects " + targetWidth + "x" + targetHeight + ": " + count + " found.");
                    } else {
                        System.out.println("Image: " + suffix + ", Objects " + targetWidth + "x" + targetHeight + ": not found.");
                    }
                }
            }

        }
    }
}