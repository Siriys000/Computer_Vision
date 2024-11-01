package org.example;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Lab_03 {
    private static final Logger logger = LogManager.getLogger(Lab_03.class);

    private ImageAPI imageAPI;
    private final Constants constants = new Constants();

    @Test
    void testImageTransformations() {
        try {
            imageAPI = new ImageAPI();
        } catch (Exception e) {
            fail("ImageAPI initialization failed: " + e.getMessage());
        }

        String inputImagePath = constants.getSourceImagePath(constants.LAB3_SRC);
        Mat originalImage = imageAPI.loadImage(inputImagePath);
        assertNotNull(originalImage, "Failed to load image");
        assertFalse(originalImage.empty(), "Loaded image is empty");

            // Тест flipImage
            Mat flippedImage = imageAPI.flipImage(originalImage, 1); // Горизонтальное отражение
            assertFalse(flippedImage.empty(), "Flipped image is empty");
            // saveImage("flipped.jpg", flippedImage); //  Раскомментируйте для сохранения и проверки
            imageAPI.saveImage(flippedImage, constants.LAB3_DIST, "flipped_" + constants.LAB3_SRC);

        // Тест repeatImage
            Mat repeatedImage = imageAPI.repeatImage(originalImage, 2, 3); // 2 раза по вертикали, 3 по горизонтали
            assertFalse(repeatedImage.empty(), "Repeated image is empty");
            assertEquals(originalImage.rows() * 2, repeatedImage.rows());
            assertEquals(originalImage.cols() * 3, repeatedImage.cols());
            imageAPI.saveImage(repeatedImage, constants.LAB3_DIST, "repeated_" + constants.LAB3_SRC);

        // Тест concatImages
            List<Mat> imagesToConcat = new ArrayList<>();
            imagesToConcat.add(originalImage);
            imagesToConcat.add(flippedImage);
            Mat concatenatedImage = imageAPI.concatImages(imagesToConcat, 0); // Горизонтальная конкатенация
            assertFalse(concatenatedImage.empty(), "Concatenated image is empty");
            assertEquals(originalImage.rows(), concatenatedImage.rows());
            assertEquals(originalImage.cols() * 2, concatenatedImage.cols());
            imageAPI.saveImage(concatenatedImage, constants.LAB3_DIST, "concatenated_" + constants.LAB3_SRC);


        // Тест resizeImage
            Mat resizedImage = imageAPI.resizeImage(originalImage, 200, 150); // Изменение размера до 200x150
            assertFalse(resizedImage.empty(), "Resized image is empty");
            assertEquals(150, resizedImage.rows());
            assertEquals(200, resizedImage.cols());
            imageAPI.saveImage(resizedImage, constants.LAB3_DIST, "resized_" + constants.LAB3_SRC);

            // Тест rotateImage
            Mat rotatedImageCrop = imageAPI.rotateImage(originalImage, 45, true);  // С обрезкой
            assertFalse(rotatedImageCrop.empty(), "Rotated image (cropped) is empty");
            imageAPI.saveImage(rotatedImageCrop, constants.LAB3_DIST, "rotatedImageCrop_" + constants.LAB3_SRC);

            Mat rotatedImageNoCrop = imageAPI.rotateImage(originalImage, 45, false); // Без обрезки
            assertFalse(rotatedImageNoCrop.empty(), "Rotated image (no crop) is empty");
            imageAPI.saveImage(rotatedImageNoCrop, constants.LAB3_DIST, "rotatedImageNoCrop_" + constants.LAB3_SRC);


            // Тест shiftImage
            Mat shiftedImage = imageAPI.shiftImage(originalImage, 50, 30); // Сдвиг на 50 пикселей по x и 30 по y
            assertFalse(shiftedImage.empty(), "Shifted image is empty");
            imageAPI.saveImage(shiftedImage, constants.LAB3_DIST, "shifted_" + constants.LAB3_SRC);



            // Тест warpPerspective
            Point[] srcPoints = new Point[]{
                    new Point(0, 0), new Point(originalImage.width() - 1, 0),
                    new Point(originalImage.width() - 1, originalImage.height() - 1), new Point(0, originalImage.height() - 1)
            };

            Point[] dstPoints = new Point[]{
                    new Point(0, 0), new Point(originalImage.width() - 1, 50), //  Пример трансформации
                    new Point(originalImage.width() - 1, originalImage.height() - 1), new Point(0, originalImage.height() - 1)

            };

            Mat warpedImage = imageAPI.warpPerspective(originalImage, srcPoints, dstPoints);
            assertFalse(warpedImage.empty(), "Warped image is empty");
            imageAPI.saveImage(warpedImage, constants.LAB3_DIST, "warped_" + constants.LAB3_SRC);


        }


        // Тест applySobel
        @Test
        void testApplySobel () {

            try {
                imageAPI = new ImageAPI();
            } catch (Exception e) {
                fail("ImageAPI initialization failed: " + e.getMessage());
            }

            String[] imageSuffixes = {".lab03.01", ".lab03.02", ".lab03.03"};

            for (String suffix : imageSuffixes) {
                String inputImagePath = constants.getSourceImagePath(suffix);
                Mat originalImage = imageAPI.loadImage(inputImagePath);
                assertNotNull(originalImage, "Failed to load image " + suffix);
                assertFalse(originalImage.empty(), "Loaded image is empty " + suffix);


                Mat sobelImage = imageAPI.applySobel(originalImage, 1, 0, 3, 1, 0, Core.BORDER_DEFAULT);
                assertFalse(sobelImage.empty(), "Sobel image is empty");
                imageAPI.saveImage(sobelImage, constants.LAB3_DIST, "sobel_" + suffix);
            }
        }



        // Тест applyLaplacian
        @Test
        void testApplyLaplacian () {

            try {
                imageAPI = new ImageAPI();
            } catch (Exception e) {
                fail("ImageAPI initialization failed: " + e.getMessage());
            }

            String[] imageSuffixes = {".lab03.01", ".lab03.02", ".lab03.03"};

            for (String suffix : imageSuffixes) {
                String inputImagePath = constants.getSourceImagePath(suffix);
                Mat originalImage = imageAPI.loadImage(inputImagePath);
                assertNotNull(originalImage, "Failed to load image " + suffix);
                assertFalse(originalImage.empty(), "Loaded image is empty " + suffix);

                Mat laplacianImage = imageAPI.applyLaplacian(originalImage, 1, 1, 0, Core.BORDER_DEFAULT);
                assertFalse(laplacianImage.empty(), "Laplacian image is empty");
                imageAPI.saveImage(laplacianImage, constants.LAB3_DIST, "laplacian_" + suffix);
            }
        }

}
