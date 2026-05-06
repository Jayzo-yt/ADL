package com.acd.verify.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;

@Service
public class QRCodeService {

    private static final Logger logger = LoggerFactory.getLogger(QRCodeService.class);

    /**
     * Generate a QR code as a Base64-encoded PNG string.
     * The QR encodes the public verification URL.
     */
    public String generateQRCodeBase64(String certId, String baseUrl) {
        try {
            String verifyUrl = baseUrl + "/verify?id=" + certId;
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = Map.of(
                    EncodeHintType.MARGIN, 1,
                    EncodeHintType.CHARACTER_SET, "UTF-8"
            );

            BitMatrix bitMatrix = qrCodeWriter.encode(verifyUrl, BarcodeFormat.QR_CODE, 300, 300, hints);
            BufferedImage image = MatrixToImageWriter.toBufferedImage(bitMatrix);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", baos);
            byte[] imageBytes = baos.toByteArray();

            String base64 = Base64.getEncoder().encodeToString(imageBytes);
            logger.info("Generated QR code for certificate: {}", certId);
            return base64;
        } catch (WriterException | IOException e) {
            logger.error("Failed to generate QR code for certificate: {}", certId, e);
            return null;
        }
    }

    /**
     * Generate a QR code for a certificate ID using localhost as the default base URL.
     */
    public String generateQRCodeBase64(String certId) {
        return generateQRCodeBase64(certId, "http://localhost:8080");
    }
}
