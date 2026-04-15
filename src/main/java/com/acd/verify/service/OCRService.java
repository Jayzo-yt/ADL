package com.acd.verify.service;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.apache.pdfbox.Loader;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Service
public class OCRService {

    private static final Logger logger = LoggerFactory.getLogger(OCRService.class);

    @Value("${tesseract.datapath:/usr/share/tesseract-ocr/4.00/tessdata}")
    private String tesseractDataPath;

    @Value("${tesseract.language:eng}")
    private String tesseractLanguage;

    public String extractText(MultipartFile file) throws IOException, TesseractException {
        String contentType = file.getContentType();
        BufferedImage image;

        if (contentType != null && contentType.equals("application/pdf")) {
            image = convertPdfToImage(file);
        } else {
            image = ImageIO.read(file.getInputStream());
        }

        if (image == null) {
            throw new IOException("Unable to read image from file");
        }

        // Preprocess image
        image = preprocessImage(image);

        // Perform OCR
        ITesseract tesseract = new Tesseract();
        tesseract.setDatapath(tesseractDataPath);
        tesseract.setLanguage(tesseractLanguage);

        String text = tesseract.doOCR(image);
        logger.info("Extracted text length: {}", text.length());

        return text;
    }

    private BufferedImage convertPdfToImage(MultipartFile file) throws IOException {
        File tempFile = File.createTempFile("certificate", ".pdf");
        file.transferTo(tempFile);

        try (PDDocument document = Loader.loadPDF(tempFile)){
            PDFRenderer renderer = new PDFRenderer(document);
            BufferedImage image = renderer.renderImageWithDPI(0, 300);
            tempFile.delete();
            return image;
        }
    }

    private BufferedImage preprocessImage(BufferedImage original) {
        // Convert to grayscale
        BufferedImage grayscale = new BufferedImage(
                original.getWidth(),
                original.getHeight(),
                BufferedImage.TYPE_BYTE_GRAY
        );

        grayscale.getGraphics().drawImage(original, 0, 0, null);

        return grayscale;
    }
}