package org.example;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

interface SystemVariables {
    String getSourceImagePath(String suffix);
    String getSourceImagePath();
    void setSourceImagePath(String suffix, String path);
    void setSourceImagePath(String path);

    String getDestinationImagePath(String suffix);
    String getDestinationImagePath();
    void setDestinationImagePath(String suffix, String path);
    void setDestinationImagePath(String path);

    String getConfigProperty(String key);
    void setConfigProperty(String key, String value);

}

public class Constants implements SystemVariables {

    private static final Logger logger = LogManager.getLogger(Constants.class);
    private final Properties environmentProperties;
    private final Properties configProperties;

    public static final String ENVIRONMENT_FILE = "environment.properties";
    public static final String CONFIG_FILE = "config.properties";

    public static final int FRAME_PADDING = 50;

    public static final String LAB3_DIST = ".lab03";
    public static final String LAB3_SRC = ".lab03.01";

    public static final int BILATERIAL_D = -1;
    public static final int BILATERIAL_SIGMASPACE = 10;
    public static final int BILATERIAL_SIGMACOLOR= 10;
    public static final int GAUSSIAN_SIGMAX = 0;
    public static final int[] kernelSizes = {3, 5, 7, 9, 13, 15};

    public static final int RANDOM_BOUND = 101;
    public static final int MAX_BOUND = 256;
    public static final int FLOODFILL_STEP = 4;//DIVIDER
    public static final int MAX_PYRUPDAWN_ITERATIONS = 6;
    public static final Scalar RECTANGLE_COLOR = new Scalar(0, 255, 0);
    public static final int RECTANGLE_THICKNESS = 2;
    public static final Size MORPH_KERNEL_SIZE = new Size(5, 5);
    public static final double THRESHOLD_VALUE = 50;
    public static final double THRESHOLD_MAX_VALUE = 255;
    public static final int THRESHOLD_TYPE = Imgproc.THRESH_OTSU;
    public static final double CANNY_THRESHOLD_RATIO = 3;
    public static final int CANNY_APERTURE_SIZE = 3;
    public static final boolean CANNY_L2_GRADIENT = true;

    public static final double RECTINDTF_MIN_WIDTH = 50;
    public static final double RECTINDTF_MAX_WIDTH = 400;
    public static final double RECTINDTF_WIDTH_STEP = 25;
    public static final double RECTINDTF_MIN_HEIGHT = 50;
    public static final double RECTINDTF_MAX_HEIGHT = 500;
    public static final double RECTINDTF_HEIGHT_STEP = 50;
    public static final double RECTINDTF_TOLERANCE = 0.1;


    public static final int BLUR_KERNEL_SIZE = 3;  // Added for blur kernel size
    public static final int OTSU_THRESHOLD_LOWER = 0; // For Otsu's thresholding
    public static final int OTSU_THRESHOLD_UPPER = 255; // For Otsu's thresholding

    public enum OSType{
        WINDOWS,
        MACOS,
        LINUX,
        OTHER
    }

    public Constants() {
        environmentProperties = loadProperties(ENVIRONMENT_FILE);
        configProperties = loadProperties(CONFIG_FILE);
    }
    private Properties loadProperties(String fileName) {
        Properties properties = new Properties();
        try (InputStream input = Constants.class.getClassLoader().getResourceAsStream(fileName)) {
            if (input == null) {
                logger.error("Sorry, unable to find {}", fileName);
                throw new RuntimeException("Config file not found: " + fileName);
            }
            properties.load(input);
        } catch (IOException ex) {
            logger.error("Error loading properties file: {}", fileName, ex);
            throw new RuntimeException("Error loading properties file: " + fileName, ex);
        }
        return properties;
    }

   /* private void saveProperties(Properties properties, String fileName) {
        try (OutputStream output = new FileOutputStream(fileName)) {
            properties.store(output, null);
        } catch (IOException ex) {
            logger.error("Error saving properties to {}: ", fileName, ex);
        }
    }*/

    @Override
    public String getSourceImagePath(String suffix) {
        return environmentProperties.getProperty("source.image" + suffix);
    }

    @Override
    public String getSourceImagePath() {
        return getSourceImagePath("");
    }

    @Override
    public void setSourceImagePath(String suffix, String path) {
        environmentProperties.setProperty("source.image" + suffix, path);
    }

    @Override
    public void setSourceImagePath(String path) {
        setSourceImagePath("", path);
    }

    @Override
    public String getDestinationImagePath(String suffix) {
        return environmentProperties.getProperty("destination.image" + suffix);
    }

    @Override
    public String getDestinationImagePath() {
        return getDestinationImagePath("");
    }

    @Override
    public void setDestinationImagePath(String suffix, String path) {
        environmentProperties.setProperty("destination.image" + suffix, path);
    }

    @Override
    public void setDestinationImagePath(String path) {
        setDestinationImagePath("", path);
    }

    @Override
    public String getConfigProperty(String key) {
        return configProperties.getProperty(key);
    }

    @Override
    public void setConfigProperty(String key, String value) {
        configProperties.setProperty(key, value);
    }
}
