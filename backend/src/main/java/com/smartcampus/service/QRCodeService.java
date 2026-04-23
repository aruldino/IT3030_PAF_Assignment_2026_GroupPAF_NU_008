package com.smartcampus.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import javax.imageio.ImageIO;

@Service
public class QRCodeService {

    /**
     * Generates a base64-encoded PNG QR code for the given booking ID.
     * Called on booking approval.
     */
    public String generateQRCode(String bookingId) {
        try {
            String qrContent = "SMARTCAMPUS:BOOKING:" + bookingId;
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(qrContent, BarcodeFormat.QR_CODE, 200, 200);
            BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (WriterException | IOException e) {
            throw new RuntimeException("Failed to generate QR code for booking: " + bookingId, e);
        }
    }

    /**
     * Verifies a QR token and returns the bookingId embedded in it.
     * Token format: "SMARTCAMPUS:BOOKING:{bookingId}"
     */
    public String verifyQRCode(String token) {
        if (token == null || !token.startsWith("SMARTCAMPUS:BOOKING:")) {
            throw new IllegalArgumentException("Invalid QR token");
        }
        return token.substring("SMARTCAMPUS:BOOKING:".length());
    }
}