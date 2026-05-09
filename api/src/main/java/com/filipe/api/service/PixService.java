package com.filipe.api.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.Base64;

@Service
public class PixService {

    public String generatePayload(String chave, String beneficiario, String cidade, BigDecimal valor) {
        StringBuilder sb = new StringBuilder();
        
        // 00 - Payload Format Indicator
        sb.append("000201");
        
        // 26 - Merchant Account Information
        String merchantInfo = "0014br.gov.bcb.pix" + String.format("01%02d%s", chave.length(), chave);
        sb.append(String.format("26%02d%s", merchantInfo.length(), merchantInfo));
        
        // 52 - Merchant Category Code
        sb.append("52040000");
        
        // 53 - Transaction Currency (986 = BRL)
        sb.append("5303986");
        
        // 54 - Transaction Amount
        String amountStr = valor.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
        sb.append(String.format("54%02d%s", amountStr.length(), amountStr));
        
        // 58 - Country Code
        sb.append("5802BR");
        
        // 59 - Merchant Name
        String name = beneficiario.length() > 25 ? beneficiario.substring(0, 25) : beneficiario;
        sb.append(String.format("59%02d%s", name.length(), name));
        
        // 60 - Merchant City
        String city = cidade.length() > 15 ? cidade.substring(0, 15) : cidade;
        sb.append(String.format("60%02d%s", city.length(), city));
        
        // 62 - Additional Data Field Template
        sb.append("62070503***");
        
        // 63 - CRC16
        sb.append("6304");
        sb.append(calculateCRC16(sb.toString()));
        
        return sb.toString();
    }

    public String generateQrCodeBase64(String payload) throws Exception {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(payload, BarcodeFormat.QR_CODE, 300, 300);
        
        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        byte[] pngData = pngOutputStream.toByteArray();
        
        return Base64.getEncoder().encodeToString(pngData);
    }

    private String calculateCRC16(String str) {
        int crc = 0xFFFF;
        int polynomial = 0x1021;

        for (byte b : str.getBytes()) {
            for (int i = 0; i < 8; i++) {
                boolean bit = ((b >> (7 - i) & 1) == 1);
                boolean c15 = ((crc >> 15 & 1) == 1);
                crc <<= 1;
                if (c15 ^ bit) {
                    crc ^= polynomial;
                }
            }
        }
        crc &= 0xFFFF;
        return String.format("%04X", crc);
    }
}
