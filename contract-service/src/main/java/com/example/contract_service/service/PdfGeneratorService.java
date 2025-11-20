package com.example.contract_service.service;

import com.example.contract_service.entity.ServiceAppendix;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;

import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Service
public class PdfGeneratorService {

    public byte[] generateSignedAppendixPdf(
            ServiceAppendix appendix,
            String residentSignatureBase64,
            String managerSignatureBase64
    ) {

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(document, outputStream);

            document.open();

            // ==========================================
            // 1. TITLE
            // ==========================================
            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Paragraph title = new Paragraph("PHỤ LỤC HỢP ĐỒNG DỊCH VỤ TIỆN ÍCH", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // ==========================================
            // 2. THÔNG TIN PHỤ LỤC
            // ==========================================
            Font normal = new Font(Font.HELVETICA, 12);

            String content =
                    "Mã phụ lục: " + appendix.getId() + "\n" +
                    "Dịch vụ: " + appendix.getServiceId() + "\n" +
                    "Gói dịch vụ: " + appendix.getPackageId() + "\n" +
                    "Cư dân đăng ký: " + appendix.getResidentId() + "\n" +
                    "Căn hộ: " + appendix.getApartmentId() + "\n" +
                    "Ngày hiệu lực: " + appendix.getEffectiveDate() + "\n" +
                    "Ngày hết hạn: " + appendix.getExpirationDate() + "\n";

            Paragraph info = new Paragraph(content, normal);
            info.setSpacingAfter(20);
            document.add(info);

            // ==========================================
            // 3. KHỐI CHỮ KÝ CƯ DÂN
            // ==========================================
            Paragraph residentBlock = new Paragraph("Chữ ký cư dân:", normal);
            residentBlock.setSpacingBefore(10);

            String residentSignatureDecoded = residentSignatureBase64 == null
                    ? "(Chưa ký)"
                    : Base64.getEncoder().encodeToString(
                                Base64.getDecoder().decode(residentSignatureBase64)
                        ).substring(0, 50) + "...";

            residentBlock.add("\n• Ký lúc: " +
                    appendix.getSignedDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"))
            );
            residentBlock.add("\n• Dữ liệu chữ ký (rút gọn): " + residentSignatureDecoded);

            residentBlock.setSpacingAfter(20);
            document.add(residentBlock);

            // ==========================================
            // 4. KHỐI CHỮ KÝ BQL
            // ==========================================
            Paragraph managerBlock = new Paragraph("Chữ ký BQL:", normal);
            managerBlock.setSpacingBefore(10);

            String managerSignatureDecoded = managerSignatureBase64 == null
                    ? "(Chưa ký)"
                    : Base64.getEncoder().encodeToString(
                                Base64.getDecoder().decode(managerSignatureBase64)
                        ).substring(0, 50) + "...";

            managerBlock.add("\n• Ký lúc: " +
                    appendix.getAdminApprovedAt().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"))
            );
            managerBlock.add("\n• Dữ liệu chữ ký (rút gọn): " + managerSignatureDecoded);

            managerBlock.setSpacingAfter(30);
            document.add(managerBlock);

            // ==========================================
            // 5. CLOSE PDF
            // ==========================================
            document.close();

            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Cannot generate appendix PDF: " + e.getMessage(), e);
        }
    }
}
