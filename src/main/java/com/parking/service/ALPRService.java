package com.parking.service;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ALPRService {
    private final Tesseract tesseract;
    private final String uploadDir = "uploads/";

    static {
        nu.pattern.OpenCV.loadShared();
        System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
    }

    public ALPRService() {
        this.tesseract = new Tesseract();
        // Set the tessdata path - you need to have tessdata folder with language files
        tesseract.setDatapath("C:/Program Files/Tesseract-OCR/tessdata");
        tesseract.setLanguage("eng"); // Set language to English

        // Create uploads directory if it doesn't exist
        new File(uploadDir).mkdirs();
    }

    public String processImage(MultipartFile file) throws IOException, TesseractException {
        // Save the uploaded file
        String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename();
        Path filePath = Paths.get(uploadDir + fileName);
        Files.write(filePath, file.getBytes());

        // Read the image using OpenCV
        Mat image = Imgcodecs.imread(filePath.toString());
        
        // Preprocess the image
        Mat processedImage = preprocessImage(image);

        // Convert Mat to BufferedImage for Tesseract
        BufferedImage bufferedImage = matToBufferedImage(processedImage);

        // Perform OCR
        String result = tesseract.doOCR(bufferedImage);

        // Clean up the result
        return cleanOCRResult(result);
    }

    private Mat preprocessImage(Mat image) {
        Mat processed = new Mat();
        
        // Convert to grayscale
        Imgproc.cvtColor(image, processed, Imgproc.COLOR_BGR2GRAY);
        
        // Apply Gaussian blur to reduce noise
        Imgproc.GaussianBlur(processed, processed, new Size(5, 5), 0);
        
        // Apply adaptive thresholding
        Imgproc.adaptiveThreshold(
            processed, processed, 255,
            Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
            Imgproc.THRESH_BINARY, 11, 2
        );
        
        return processed;
    }

    private BufferedImage matToBufferedImage(Mat mat) throws IOException {
        MatOfByte mob = new MatOfByte();
        Imgcodecs.imencode(".jpg", mat, mob);
        byte[] byteArray = mob.toArray();
        ByteArrayInputStream bis = new ByteArrayInputStream(byteArray);
        return ImageIO.read(bis);
    }

    private String cleanOCRResult(String result) {
        // Remove special characters and whitespace
        return result.replaceAll("[^A-Z0-9]", "").trim();
    }

    public String getImagePath(String fileName) {
        return uploadDir + fileName;
    }
}