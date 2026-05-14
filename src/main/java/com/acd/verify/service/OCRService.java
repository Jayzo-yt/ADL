package com.acd.verify.service;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.Word;
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
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import com.acd.verify.model.OCRResult;

@Service
public class OCRService {

    private static final Logger logger = LoggerFactory.getLogger(OCRService.class);

    @Value("${tesseract.datapath:}")
    private String tesseractDataPath;

    @Value("${tesseract.language:eng}")
    private String tesseractLanguage;

    /**
     * Returns OCR text and average confidence. Newer callers should prefer this method.
     */
    public OCRResult extractWithConfidence(MultipartFile file) throws IOException, TesseractException {
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

        // Configure Tesseract
        ITesseract tesseract = new Tesseract();
        tesseract.setDatapath(resolveTessDataPath());
        tesseract.setLanguage(tesseractLanguage);
        try {
            tesseract.setPageSegMode(6); // block of text
        } catch (Exception e) {
            // ignore if API not available
        }

        // Try multiple preprocessed variants and pick the best OCR confidence.
        List<BufferedImage> variants = preprocessVariants(image);
        double bestConfidence = -1.0;
        String bestText = "";

       for (BufferedImage variant : variants) {

    OCRResult partial = runOcrWithConfidence(tesseract, variant);

    if (partial.getAverageConfidence() > bestConfidence ||
            (partial.getAverageConfidence() == bestConfidence &&
             partial.getText().length() > bestText.length())) {

        bestConfidence = partial.getAverageConfidence();
        bestText = partial.getText();
    }

    // Free memory
    variant.flush();
}

// Free original image
image.flush();

        logger.info("Extracted text length: {} | avgConf={} | variants={}", bestText.length(), bestConfidence, variants.size());

        return new OCRResult(bestText, Math.max(bestConfidence, 0.0));
    }

    private OCRResult runOcrWithConfidence(ITesseract tesseract, BufferedImage image) throws TesseractException {
        List<Word> words = tesseract.getWords(image, ITessAPI.TessPageIteratorLevel.RIL_WORD);
        if (words != null && !words.isEmpty()) {
            double sum = 0.0;
            StringBuilder sb = new StringBuilder();
            for (Word w : words) {
                sb.append(w.getText()).append(' ');
                sum += w.getConfidence();
            }
            return new OCRResult(sb.toString().trim(), sum / words.size());
        }

        String text = tesseract.doOCR(image);
        return new OCRResult(text == null ? "" : text.trim(), 0.0);
    }

    private String resolveTessDataPath() throws TesseractException {
        List<String> candidates = new ArrayList<>();

        if (tesseractDataPath != null && !tesseractDataPath.isBlank()) {
            candidates.add(tesseractDataPath);
        }

        String envPath = System.getenv("TESSDATA_PREFIX");
        if (envPath != null && !envPath.isBlank()) {
            candidates.add(envPath);
        }

        candidates.add("D:/ADL/tesseract/tessdata");
        candidates.add("C:/Program Files/Tesseract-OCR/tessdata");
        candidates.add("C:/Program Files (x86)/Tesseract-OCR/tessdata");

        List<String> attemptedPaths = new ArrayList<>();

        for (String candidate : candidates) {
            try {
                Path path = Path.of(candidate);
                attemptedPaths.add(path.toString());
                if (Files.isDirectory(path) && Files.exists(path.resolve(tesseractLanguage + ".traineddata"))) {
                    logger.info("Using Tesseract datapath: {}", path.toAbsolutePath());
                    return path.toString();
                }
            } catch (InvalidPathException e) {
                attemptedPaths.add("INVALID(" + candidate.replace("\t", "\\t") + ")");
            }
        }

        throw new TesseractException(
                "Tesseract language data not found. Configure 'tesseract.datapath' in application.properties " +
                        "or set TESSDATA_PREFIX to your tessdata directory. Checked paths: " + attemptedPaths
        );
    }

   private BufferedImage convertPdfToImage(MultipartFile file) throws IOException {

    File tempFile = File.createTempFile("certificate", ".pdf");
    file.transferTo(tempFile);

    try (PDDocument document = Loader.loadPDF(tempFile)) {

        PDFRenderer renderer = new PDFRenderer(document);

        // MUCH lighter than renderImageWithDPI
        BufferedImage image = renderer.renderImage(0);

        return image;

    } finally {
        tempFile.delete();
    }
}
   
private List<BufferedImage> preprocessVariants(BufferedImage original) {

    List<BufferedImage> variants = new ArrayList<>();

    BufferedImage grayscale = toGrayscale(original);

    variants.add(grayscale);

    return variants;
}

    private BufferedImage toGrayscale(BufferedImage original) {
        BufferedImage grayscale = new BufferedImage(
                original.getWidth(),
                original.getHeight(),
                BufferedImage.TYPE_BYTE_GRAY
        );
        grayscale.getGraphics().drawImage(original, 0, 0, null);
        return grayscale;
    }

   

    
  

   
}