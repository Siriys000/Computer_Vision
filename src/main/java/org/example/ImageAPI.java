package org.example;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opencv.core.*;
import org.example.Constants.OSType;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.Point;
import org.opencv.photo.Photo;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.List;

public class ImageAPI {

    private static final Logger logger = LogManager.getLogger(ImageAPI.class);
    private final Constants constants;
    //private final SystemVariables sysvar;

    public ImageAPI() throws Exception {
        constants = new Constants();
        //this.sysvar = new Constants();
        logger.info("Checking OS.....");
        initializeOpenCV();
        logger.info("OpenCV version: {}", Core.getVersionString());
    }
   /* public ImageAPI(SystemVariables sysvar) throws Exception {
        this.consts = sysvar;
        logger.info("Checking OS.....");
        initializeOpenCV();
        logger.info("OpenCV version: {}", Core.getVersionString());
    }*/ // Поддержка других классов, имплементирующих интерфейс SystemVariables, убрана

    private void initializeOpenCV() throws Exception {
        OSType osType = getOperatingSystemType();

        switch (osType) {
            case LINUX:
                System.load(constants.getConfigProperty("PATH_TO_NATIVE_LIB_LINUX"));
                break;
            case WINDOWS:
                System.load(constants.getConfigProperty("PATH_TO_NATIVE_LIB_WIN"));
                break;
            case MACOS:
                throw new Exception("Mac OS does not support!");
            case OTHER:
                throw new Exception("Current OS does not support!");
            default:
                throw new Exception("Your OS does not support!!!");
        }

        logger.info("OpenCV initialized for OS {}", osType);
    }
    private OSType getOperatingSystemType() {
        String os = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
        if (os.contains("mac") || os.contains("darwin")) {
            return OSType.MACOS;
        } else if (os.contains("win")) {
            return OSType.WINDOWS;
        } else if (os.contains("nux")) {
            return OSType.LINUX;
        } else {
            return OSType.OTHER;
        }
    }

//////Lab2///////////////////////////////////////////////////////////
    public Mat loadImage(String imagePath) {
        logger.debug("Loading image from: {}", imagePath);
        Mat image = Imgcodecs.imread(imagePath);
        if (image.empty()) {
            logger.error("Could not load image: {}", imagePath);
            return null;
        }
        return image;
    }

    public void zeroChannel(Mat image, int channelNumber) throws Exception {
        if (image == null || image.empty()) {
            throw new Exception("Image is empty or null");
        }

        if (channelNumber < 0 || channelNumber >= image.channels()) {
            throw new Exception("Invalid channel number. Must be between 0 and " + (image.channels() - 1));
        }

        int totalBytes = (int) (image.total() * image.elemSize());
        byte[] buffer = new byte[totalBytes];
        image.get(0, 0, buffer);

        int numChn = image.channels();

        // Использование Stream API для обнуления канала
        IntStream.range(0, totalBytes)
                .parallel() // Добавляем распараллеливание для больших изображений
                .filter(i -> i % numChn == channelNumber)
                .forEach(i -> buffer[i] = 0);

        image.put(0, 0, buffer);
    }

    // Метод для отображения изображения (адаптированный)
    private static JFrame frame = null;
    private static JLabel lbl = null;
    public void showImage(Mat m, String title) {
        if (m == null || m.empty()) {
            logger.error("Cannot show image. Mat is empty or null");
            return;
        }

        int type = m.channels() > 1 ? BufferedImage.TYPE_3BYTE_BGR : BufferedImage.TYPE_BYTE_GRAY;
        BufferedImage image = new BufferedImage(m.cols(), m.rows(), type);
        byte[] b = new byte[m.channels() * m.cols() * m.rows()];
        m.get(0, 0, b);
        System.arraycopy(b, 0, ((DataBufferByte) image.getRaster().getDataBuffer()).getData(), 0, b.length);
        ImageIcon icon = new ImageIcon(image);


        if (frame == null) { // Создаем новое окно, только если его еще нет
            frame = new JFrame();
            frame.setLayout(new FlowLayout());
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Только одно окно закроет приложение
            lbl = new JLabel(); // Создаем JLabel
            frame.add(lbl);

        }
        frame.setTitle(title); // Устанавливаем заголовок окна
        lbl.setIcon(icon); // Устанавливаем иконку в существующий JLabel
        int padding=constants.FRAME_PADDING;
        frame.setSize(image.getWidth(null) + padding, image.getHeight(null) + padding);
        frame.setVisible(true);

    }


    public void saveImage(Mat image, String propertySuffix, String imageName) {
        String basePath = constants.getDestinationImagePath(propertySuffix); // Получаем базовый путь из constants
        if (basePath == null || basePath.isEmpty()) {
            logger.error("Base path for property {} is not defined.", propertySuffix);
            return;
        }

        String outputPath = basePath + imageName + ".png"; // Формируем полный путь

        try {
            Files.createDirectories(Paths.get(outputPath).getParent());// Создаем директории, если они не существуют

            boolean success = Imgcodecs.imwrite(outputPath, image);

            if (!success) {
                logger.error("Could not save image to: {}", outputPath);
            } else {
                logger.info("Image saved to: {}", outputPath);
            }

        } catch (Exception e) {
            logger.error("Error saving image: {} - {}", outputPath, e.getMessage());
        }
    }
//////Lab3///////////////////////////////////////////////////////////

// Методы для операторов Собеля и Лапласа

// Объяснение параметров:
// * `dx`: Порядок производной по x.
// * `dy`: Порядок производной по y.
// * `ksize`: Размер ядра Собеля (например, 1, 3, 5, 7). Больший размер - большее сглаживание.
// * `scale`: Масштабный коэффициент для вычисленного градиента.
// * `delta`: Значение, добавляемое к результату.
// * `borderType`: определяет, как обрабатываются границы изображения при применении операций,
// которые требуют доступ к пикселям за пределами изображения.
// Например, при Core.BORDER_DEFAULT (или Core.BORDER_REFLECT_101) граница создается путем отражения относительно центра пикселей края.

    public Mat applySobel(Mat image, int dx, int dy, int ksize, double scale, double delta, int borderType) {
        Mat grayImage = new Mat();
        Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY); // Преобразование в оттенки серого

        Mat dstSobel = new Mat();
        Imgproc.Sobel(grayImage, dstSobel, CvType.CV_32F, dx, dy, ksize, scale, delta, borderType);

        Mat absSobel = new Mat();
        Core.convertScaleAbs(dstSobel, absSobel); // Берем модуль градиента

        return absSobel;
    }

    public Mat applyLaplacian(Mat image, int ksize, double scale, double delta, int borderType) {
        Mat grayImage = new Mat();
        Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY); // Преобразование в оттенки серого

        Mat dstLaplace = new Mat();
        Imgproc.Laplacian(grayImage, dstLaplace, CvType.CV_32F, ksize, scale, delta, borderType);

        Mat absLaplace = new Mat();
        Core.convertScaleAbs(dstLaplace, absLaplace);

        return absLaplace;
    }


    // Методы для трансформаций

    //'flipCode': 0 - вертикально, 1 - горизонтально, -1 - оба направления
    public Mat flipImage(Mat image, int flipCode) {
        Mat dst = new Mat();
        Core.flip(image, dst, flipCode);
        return dst;
    }

    // nx - кол-во повторений по x, ny - кол-во повторений по y
    public Mat repeatImage(Mat image, int ny, int nx) {
        Mat dst = new Mat();
        Core.repeat(image, ny, nx, dst);
        return dst;
    }

    //'direction': 0 - горизонтально, 1 - вертикально
    public Mat concatImages(List<Mat> images, int direction) {

        Mat dst = new Mat();
        if (direction == 0) {
            Core.hconcat(images, dst);
        } else {
            Core.vconcat(images, dst);
        }
        return dst;
    }

    public Mat resizeImage(Mat image, int width, int height) {
        Mat dst = new Mat();
        Imgproc.resize(image, dst, new Size(width, height));
        return dst;
    }


    public Mat rotateImage(Mat image, double angle, boolean crop) {
        Point center = new Point(image.width() / 2.0, image.height() / 2.0);
        Mat rotationMat = Imgproc.getRotationMatrix2D(center, angle, 1.0);

        if (crop) {
            // Вращение с обрезкой - размер выходного изображения совпадает с входным
            Mat dst = new Mat();
            Imgproc.warpAffine(image, dst, rotationMat, new Size(image.width(), image.height()), Imgproc.INTER_LINEAR, Core.BORDER_CONSTANT, new Scalar(0,0,0)); // BORDER_CONSTANT для черного фона
            return dst;
        } else {
            // Вращение с сохранением всего контента - размер выходного изображения увеличивается

            // Вычисляем границы повернутого изображения
            double cos = Math.abs(Math.cos(Math.toRadians(angle)));
            double sin = Math.abs(Math.sin(Math.toRadians(angle)));
            int newWidth = (int) (image.width() * cos + image.height() * sin);
            int newHeight = (int) (image.width() * sin + image.height() * cos);

            // Корректируем матрицу трансформации для центрирования
            rotationMat.put(0, 2, rotationMat.get(0, 2)[0] + (newWidth - image.width()) / 2.0);
            rotationMat.put(1, 2, rotationMat.get(1, 2)[0] + (newHeight - image.height()) / 2.0);

            Mat dst = new Mat(new Size(newWidth, newHeight), image.type()); // Создаем Mat с правильным размером и типом
            dst.setTo(new Scalar(0, 0, 0)); // Заполняем черным цветом до вращения
            Imgproc.warpAffine(image, dst, rotationMat, dst.size(), Imgproc.INTER_LINEAR, Core.BORDER_TRANSPARENT);


            return dst;
        }
    }

    // tx - сдвиг по x, ty - сдвиг по y
    public Mat shiftImage(Mat image, int tx, int ty) {
        Mat translationMat = Mat.eye(2, 3, CvType.CV_64F); // Создаем матрицу сдвига
        translationMat.put(0, 2, tx);
        translationMat.put(1, 2, ty);

        Mat dst = new Mat();
        Imgproc.warpAffine(image, dst, translationMat, new Size(image.width(), image.height()),
                Imgproc.INTER_LINEAR, Core.BORDER_CONSTANT, Scalar.all(0)); // Черный фон для сдвинутых областей
        return dst;
    }

    // Метод для трансформации перспективы
    public Mat warpPerspective(Mat image, Point[] srcPoints, Point[] dstPoints) {
        Mat perspectiveMat = Imgproc.getPerspectiveTransform(new MatOfPoint2f(srcPoints), new MatOfPoint2f(dstPoints));
        Mat dst = new Mat();
        Imgproc.warpPerspective(image, dst, perspectiveMat, new Size(image.width(), image.height()));
        return dst;
    }

//////Lab4///////////////////////////////////////////////////////////
    public void filterImage(String imagePath, int kernelSize, boolean showIntermediate, boolean saveIntermediate) {
        if (kernelSize <= 0 || kernelSize % 2 == 0) {
            logger.error("Kernel size must be a positive odd number.");
            return;
        }
    
        Mat image = loadImage(imagePath);
        if (image == null) {
            logger.error("Failed to load image");
            return; 
        }

        // В целом, чем больше размер ядра, тем сильнее размытие и шумоподавление.
        // Разные типы фильтров по-разному реагируют на изменение размера ядра.
        // Например, медианный фильтр лучше сохраняет резкие границы.
        Size kernel = new Size(kernelSize, kernelSize);
    
        Mat blurredImage = new Mat();
        Mat gaussianBlurredImage = new Mat();
        Mat medianBlurredImage = new Mat();
        Mat bilateralFilteredImage = new Mat();
    
        // Averaging Blur
        Imgproc.blur(image, blurredImage, kernel);
        showAndSave(blurredImage, "blurred", imagePath, kernelSize, showIntermediate, saveIntermediate);
    
        // Gaussian Blur
        Imgproc.GaussianBlur(image, gaussianBlurredImage, kernel, constants.GAUSSIAN_SIGMAX); // sigmaX = 0 for automatic calculation
        showAndSave(gaussianBlurredImage, "gaussian_blurred", imagePath, kernelSize, showIntermediate, saveIntermediate);
    
        // Median Blur
        Imgproc.medianBlur(image, medianBlurredImage, kernelSize);
        showAndSave(medianBlurredImage, "median_blurred", imagePath, kernelSize, showIntermediate, saveIntermediate);
    
        // Bilateral Filter
        Imgproc.bilateralFilter(image, bilateralFilteredImage, constants.BILATERIAL_D, constants.BILATERIAL_SIGMACOLOR, constants.BILATERIAL_SIGMASPACE); // Example values
        showAndSave(bilateralFilteredImage, "bilateral_filtered", imagePath, kernelSize, showIntermediate, saveIntermediate);
    }
    private void showAndSave(Mat image, String filterName, String originalImagePath, int kernelSize, boolean show, boolean save){
        if(show) {
            showImage(image, filterName);
        }
        if(save) {
            String fileNameWithoutExtension = extractFileName(originalImagePath);
            saveImage(image, ".lab04", fileNameWithoutExtension + "_" + filterName + "_kernel" + kernelSize);        }
    }
    public String extractFileName(String fullPath) {
        File file = new File(fullPath);
        String fileName = file.getName(); // Получаем имя файла с расширением
        int extensionIndex = fileName.lastIndexOf('.');
        if (extensionIndex > 0) {
            return fileName.substring(0, extensionIndex);
        } else {
            return fileName;
        }
    }
//////Lab5///////////////////////////////////////////////////////////
    /**
     * Выполняет заливку области изображения заданным цветом, начиная с указанной точки.
     *
     * @param image    Изображение, на котором будет выполняться заливка.  Не должно быть null или пустым.
     * @param seedPoint Начальная точка заливки. Координаты пикселя, с которого начинается процесс. Не должно быть null.
     * @param newVal    Цвет заливки (Scalar с RGB значениями). Если null, генерируется случайный цвет.
     * @param loDiff    Нижняя граница диапазона цветов для заливки (Scalar с RGB значениями).  Если null, генерируется случайный диапазон.
     *                Пиксели с цветом, отличающимся от цвета начальной точки в пределах этого диапазона, будут залиты.
     * @param upDiff    Верхняя граница диапазона цветов для заливки (Scalar с RGB значениями).  Если null, генерируется случайный диапазон.
     *                Пиксели с цветом, отличающимся от цвета начальной точки в пределах этого диапазона, будут залиты.
     * @return Новое изображение Mat с выполненной заливкой. Возвращает null, если входное изображение image было null или пустым,
     *         или если seedPoint был null.  Оригинальное изображение image не изменяется.
     */
    public Mat floodFillImage(Mat image, Point seedPoint, Scalar newVal, Scalar loDiff, Scalar upDiff) {
        if (image == null || image.empty()) {
            logger.error("Cannot perform flood fill. Image is empty or null.");
            return null;
        }

        if (seedPoint == null) {
            logger.error("Seed point cannot be null.");
            return null;
        }

        Random rand = new Random();

        if (newVal == null) {
            newVal = new Scalar(rand.nextInt(constants.MAX_BOUND), rand.nextInt(constants.MAX_BOUND), rand.nextInt(constants.MAX_BOUND));
            logger.info("Generated random newVal: {}", newVal);
        }

        if (loDiff == null) {
            int randomDiff = rand.nextInt(constants.RANDOM_BOUND); // Диапазон от 0 до 100
            loDiff = new Scalar(randomDiff, randomDiff, randomDiff);
            logger.info("Generated random loDiff: {}", loDiff);
        }

        if (upDiff == null) {
            int randomDiff = rand.nextInt(constants.RANDOM_BOUND); // Диапазон от 0 до 100
            upDiff = new Scalar(randomDiff, randomDiff, randomDiff);
            logger.info("Generated random upDiff: {}", upDiff);
        }

        Mat mask = new Mat();
        Rect rect = new Rect();

        Mat filledImage = image.clone();

        Imgproc.floodFill(filledImage, mask, seedPoint, newVal, rect, loDiff, upDiff, Imgproc.FLOODFILL_FIXED_RANGE);

        return filledImage;
    }

    public Mat pyrDown(Mat image, int iterations) {
        if (image == null || image.empty()) {
            throw new IllegalArgumentException("Input image is null or empty.");
        }
        if (iterations < 1) {
            throw new IllegalArgumentException("Number of iterations must be at least 1.");
        }

        Mat result = image.clone();
        for (int i = 0; i < iterations; i++) {
            Mat temp = new Mat();
            Imgproc.pyrDown(result, temp);
            result = temp;
        }
        return result;
    }

    public Mat pyrUp(Mat image, int iterations) {
        if (image == null || image.empty()) {
            throw new IllegalArgumentException("Input image is null or empty.");
        }
        if (iterations < 1) {
            throw new IllegalArgumentException("Number of iterations must be at least 1.");
        }

        Mat result = image.clone();
        for (int i = 0; i < iterations; i++) {
            Mat temp = new Mat();
            Imgproc.pyrUp(result, temp);
            result = temp;
        }
        return result;
    }

    public Mat getDifferenceAfterPyrDownUp(Mat image, int downIterations, int upIterations) {
        if (image == null || image.empty()) {
            throw new IllegalArgumentException("Input image is null or empty.");
        }
        if (downIterations < 1 || upIterations < 1) {
            throw new IllegalArgumentException("Number of iterations must be at least 1.");
        }
        Mat downSampled = pyrDown(image, downIterations);
        Mat upSampled = pyrUp(downSampled, upIterations);

        if (upSampled.rows() != image.rows() || upSampled.cols() != image.cols()) {
            Imgproc.resize(upSampled, upSampled, image.size());
        }

        Mat diff = new Mat();
        // Core.subtract() вычитает пиксели upSampled изображения (после понижения и повышения разрешения) из пикселей исходного изображения image.
        // Результат – изображение pyrDownUpImage, которое показывает разницу между исходным и обработанным изображением.
        // С увеличением количества итераций pyrDown/pyrUp, качество изображения ухудшается из-за потерь информации при понижении разрешения.
        // diff будет содержать более яркие пиксели в тех областях, где разница между исходным и обработанным изображением больше.
        // А чем больше итераций, тем больше должна быть разница.
        Core.subtract(image, upSampled, diff);
        return diff;
    }

    public int identifyRectangularObjects(Mat srcImage, double targetWidth, double targetHeight, double tolerance) {
        if (srcImage == null || srcImage.empty()) {
            throw new IllegalArgumentException("Input image is null or empty.");
        }
        Mat imageWithRectangles = srcImage.clone();

        // 1. Преобразование в оттенки серого
        Mat grayImage = new Mat();
        Imgproc.cvtColor(srcImage, grayImage, Imgproc.COLOR_BGR2GRAY);

        // 2. Шумоподавление
        Mat denoisingImage = new Mat();
        Photo.fastNlMeansDenoising(grayImage, denoisingImage);

        // 3. Выравнивание гистограммы
        Mat histogramEqualizationImage = new Mat();
        Imgproc.equalizeHist(denoisingImage, histogramEqualizationImage);

        // 4. Морфологическое открытие
        Mat morphologicalOpeningImage = new Mat();
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, constants.MORPH_KERNEL_SIZE);
        Imgproc.morphologyEx(histogramEqualizationImage, morphologicalOpeningImage, Imgproc.MORPH_OPEN, kernel);

        // 5. Вычитание
        Mat subtractImage = new Mat();
        Core.subtract(histogramEqualizationImage, morphologicalOpeningImage, subtractImage);

        // 6. Пороговая обработка
        Mat thresholdImage = new Mat();
        double threshold = Imgproc.threshold(subtractImage, thresholdImage, constants.THRESHOLD_VALUE, constants.THRESHOLD_MAX_VALUE, constants.THRESHOLD_TYPE);

        // 7. Оператор Canny (необязательно, если не нужны контуры)
        thresholdImage.convertTo(thresholdImage, CvType.CV_8U);
        Mat edgeImage = new Mat();
        Imgproc.Canny(thresholdImage, edgeImage, threshold, threshold * constants.CANNY_THRESHOLD_RATIO, constants.CANNY_APERTURE_SIZE, constants.CANNY_L2_GRADIENT);

        // 8. Дилатация
        Mat dilatedImage = new Mat();
        Imgproc.dilate(thresholdImage, dilatedImage, kernel);

        // 9. Поиск контуров
        ArrayList<MatOfPoint> contours = new ArrayList<>();
        //Imgproc.findContours(dilatedImage, contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        Imgproc.findContours(dilatedImage, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        // RETR_EXTERNAL вместо RETR_TREE, чтобы получить только внешние контуры

        int matchingObjectsCount = 0;
        for (MatOfPoint contour : contours) {
            Rect rect = Imgproc.boundingRect(contour);

            // Проверка соотношения сторон с учетом допустимой погрешности
            double ratio = (double) rect.height / rect.width;
            double targetRatio = targetHeight / targetWidth;
            if (Math.abs(ratio - targetRatio) > tolerance) {
                continue; // Пропускаем контуры с неподходящим соотношением сторон
            }

            if (Math.abs(rect.width - targetWidth) < targetWidth * tolerance &&
                    Math.abs(rect.height - targetHeight) < targetHeight * tolerance) {

                matchingObjectsCount++;

                Imgproc.rectangle(imageWithRectangles, rect, constants.RECTANGLE_COLOR, constants.RECTANGLE_THICKNESS); // Рисуем на копии

            }
        }
        if (matchingObjectsCount > 0) {
            String outputName = "outputImage_objects_" + targetWidth + "x" + targetHeight + "_tolerance_" + tolerance + ".jpg";
            saveImage(imageWithRectangles,".lab05.03", outputName); // Сохраняем копию с прямоугольниками
        }

        return matchingObjectsCount;
    }
//////Lab6///////////////////////////////////////////////////////////
    public Mat detectEdgesCanny(Mat image, double thresholdRatio) {
        if (image == null || image.empty()) {
            logger.error("Cannot detect edges. Image is empty or null.");
            return null;
        }

        Mat grayImage = new Mat();
        Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);

        Mat blurredImage = new Mat();
        Imgproc.blur(grayImage, blurredImage, new Size(constants.BLUR_KERNEL_SIZE, constants.BLUR_KERNEL_SIZE)); // Use constant for blur size

        Mat thresholdImage = new Mat();
        double threshold = Imgproc.threshold(grayImage, thresholdImage, constants.OTSU_THRESHOLD_LOWER, constants.OTSU_THRESHOLD_UPPER, Imgproc.THRESH_OTSU);

        Mat detectedEdges = new Mat();
        Imgproc.Canny(blurredImage, detectedEdges, threshold, threshold * thresholdRatio, constants.CANNY_APERTURE_SIZE, constants.CANNY_L2_GRADIENT);

        return detectedEdges;
    }


}
