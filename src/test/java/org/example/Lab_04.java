package org.example;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class Lab_04 {
    private static final Logger logger = LogManager.getLogger(Lab_04.class);

    private ImageAPI imageAPI;
    private final Constants constants = new Constants();

    @Test
    void testImageFilters() {
        try {
            imageAPI = new ImageAPI();
        } catch (Exception e) {
            fail("ImageAPI initialization failed: " + e.getMessage());
        }

        String[] imageSuffixes = {".lab04.01", ".lab04.02", ".lab04.03"};
        int[] kernelSizes = Arrays.copyOfRange(constants.kernelSizes, 0, 4);

        for (String suffix : imageSuffixes) {
            String inputImagePath = constants.getSourceImagePath(suffix);
            Mat originalImage = imageAPI.loadImage(inputImagePath);
            assertNotNull(originalImage, "Failed to load image " + suffix);
            assertFalse(originalImage.empty(), "Loaded image is empty " + suffix);
            for (int kernelSize : kernelSizes) {
                logger.info("Filtering image {} with kernel size {}", suffix, kernelSize);

                imageAPI.filterImage(inputImagePath, kernelSize, true, true);
            }
        }
    }

    @Test
    public void testMorphology() {
        try {
            imageAPI = new ImageAPI();
        } catch (Exception e) {
            fail("ImageAPI initialization failed: " + e.getMessage());
            return;
        }

        String suffix = ".lab04.04";
        String inputImagePath = constants.getSourceImagePath(suffix);
        Mat src = imageAPI.loadImage(inputImagePath);
        assertNotNull(src, "Failed to load image " + suffix);
        assertFalse(src.empty(), "Loaded image is empty " + suffix);

        String fileNameWithoutExtension = imageAPI.extractFileName(inputImagePath);


        int[] kernelSizes = constants.kernelSizes;
        int[] elementShapes = {Imgproc.MORPH_RECT, Imgproc.MORPH_ELLIPSE, Imgproc.MORPH_CROSS}; // MORPH_GRADIENT; MORPH_BLACKHAT; не являются указателями формы
        String[] shapePrefixes = {"rec_", "ell_", "cross_"};


        for (int kernelSize : kernelSizes) {
            for (int i = 0; i < elementShapes.length; i++) { // Итерируемся по формам элементов
                int shape = elementShapes[i];
                String shapePrefix = shapePrefixes[i];

                Mat element = Imgproc.getStructuringElement(shape, new Size(kernelSize, kernelSize));


                Mat dstErode = src.clone();
                Imgproc.erode(src, dstErode, element);
                imageAPI.saveImage(dstErode, ".lab04", fileNameWithoutExtension + "_" + shapePrefix + "erode_" + kernelSize + "x" + kernelSize + ".png");

                Mat dstDilate = src.clone();
                Imgproc.dilate(src, dstDilate, element);
                imageAPI.saveImage(dstDilate, ".lab04", fileNameWithoutExtension + "_" + shapePrefix + "dilate_" + kernelSize + "x" + kernelSize + ".png");

                //imageAPI.showImage(dstDilate,  shapePrefix + "dilate_" + kernelSize + "x" + kernelSize);

            }
        }
    }
}

