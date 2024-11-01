package org.example;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

public class Lab_02 {

    private static final Logger logger = LogManager.getLogger(Lab_02.class);

    private ImageAPI imageAPI;
    private final Constants constants = new Constants();

    @Test
    void testZeroChannelAndShowImage() {
        try {
            imageAPI = new ImageAPI();
        } catch (Exception e) {
            fail("ImageAPI initialization failed: " + e.getMessage());
        }

        String inputImagePath = constants.getSourceImagePath();
        Mat originalImage = imageAPI.loadImage(inputImagePath);

        assertNotNull(originalImage, "Failed to load image");
        assertFalse(originalImage.empty(), "Loaded image is empty");

        int numChannels = originalImage.channels();

        try {
            for (int channelToZero = 0; channelToZero < numChannels; channelToZero++) {
                Mat imageCopy = originalImage.clone(); // Создаем копию для каждого канала
                imageAPI.zeroChannel(imageCopy, channelToZero);
                imageAPI.showImage(imageCopy, "Zeroed Channel " + channelToZero);
                imageAPI.saveImage(imageCopy, ".lab02", "ZeroedChannel" + channelToZero);
            }

        } catch (Exception e) {
            fail("zeroChannel operation failed: " + e.getMessage());
        }
    }
}

