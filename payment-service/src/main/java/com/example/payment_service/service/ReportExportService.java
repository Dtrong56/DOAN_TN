package com.example.payment_service.service;

import com.example.payment_service.dto.MonthlyReportDTO;
import com.example.payment_service.dto.RevenueDebtReportDTO;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;



@Service
@RequiredArgsConstructor
public class ReportExportService {

    // Excel export (clean version - no errors)
    public byte[] exportToExcel(RevenueDebtReportDTO report) throws Exception {

        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = wb.createSheet("RevenueDebt");
            int rowIdx = 0;

            // Title
            org.apache.poi.ss.usermodel.Row title = sheet.createRow(rowIdx++);
            org.apache.poi.ss.usermodel.Cell titleCell = title.createCell(0);
            titleCell.setCellValue("Báo cáo Doanh thu & Công nợ");

            CellStyle titleStyle = wb.createCellStyle();
            org.apache.poi.ss.usermodel.Font titleFont = wb.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);
            titleCell.setCellStyle(titleStyle);

            rowIdx++; // blank line

            // Summary rows
            org.apache.poi.ss.usermodel.Row sum1 = sheet.createRow(rowIdx++);
            sum1.createCell(0).setCellValue("Tổng doanh thu");
            sum1.createCell(1).setCellValue(report.getSummary().getTotalRevenue().doubleValue());

            org.apache.poi.ss.usermodel.Row sum2 = sheet.createRow(rowIdx++);
            sum2.createCell(0).setCellValue("Trong đó - Offline");
            sum2.createCell(1).setCellValue(report.getSummary().getTotalOffline().doubleValue());

            org.apache.poi.ss.usermodel.Row sum3 = sheet.createRow(rowIdx++);
            sum3.createCell(0).setCellValue("Trong đó - Online");
            sum3.createCell(1).setCellValue(report.getSummary().getTotalOnline().doubleValue());

            org.apache.poi.ss.usermodel.Row sum4 = sheet.createRow(rowIdx++);
            sum4.createCell(0).setCellValue("Tổng công nợ");
            sum4.createCell(1).setCellValue(report.getSummary().getTotalDebt().doubleValue());

            rowIdx++;

            // Table header
            org.apache.poi.ss.usermodel.Row header = sheet.createRow(rowIdx++);
            header.createCell(0).setCellValue("Tháng");
            header.createCell(1).setCellValue("Năm");
            header.createCell(2).setCellValue("Doanh thu");
            header.createCell(3).setCellValue("Offline");
            header.createCell(4).setCellValue("Online");
            header.createCell(5).setCellValue("Công nợ");

            // Data
            for (MonthlyReportDTO m : report.getMonthly()) {
                org.apache.poi.ss.usermodel.Row r = sheet.createRow(rowIdx++);
                r.createCell(0).setCellValue(m.getMonth());
                r.createCell(1).setCellValue(m.getYear());
                r.createCell(2).setCellValue(toDouble(m.getRevenue()));
                r.createCell(3).setCellValue(toDouble(m.getOffline()));
                r.createCell(4).setCellValue(toDouble(m.getOnline()));
                r.createCell(5).setCellValue(toDouble(m.getDebt()));
            }

            // Autosize
            for (int c = 0; c < 6; c++) {
                sheet.autoSizeColumn(c);
            }

            wb.write(out);
            return out.toByteArray();
        }
    }

    private double toDouble(BigDecimal val) {
        return val == null ? 0d : val.doubleValue();
    }


    // PDF export using OpenPDF
    public byte[] exportToPdf(RevenueDebtReportDTO report) throws Exception {
        Document document = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter.getInstance(document, out);
            document.open();

            // Title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Paragraph title = new Paragraph("Báo cáo Doanh thu & Công nợ", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);

            // Summary table
            PdfPTable sumTable = new PdfPTable(2);
            sumTable.setWidthPercentage(50);
            sumTable.setSpacingBefore(8f);
            sumTable.setSpacingAfter(8f);
            sumTable.addCell(cell("Tổng doanh thu"));
            sumTable.addCell(cell(report.getSummary().getTotalRevenue().toString()));
            sumTable.addCell(cell("Tổng offline"));
            sumTable.addCell(cell(report.getSummary().getTotalOffline().toString()));
            sumTable.addCell(cell("Tổng online"));
            sumTable.addCell(cell(report.getSummary().getTotalOnline().toString()));
            sumTable.addCell(cell("Tổng công nợ"));
            sumTable.addCell(cell(report.getSummary().getTotalDebt().toString()));
            document.add(sumTable);

            document.add(Chunk.NEWLINE);

            // Monthly breakdown table
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.2f, 1.2f, 2f, 2f, 2f, 2f});

            // header
            table.addCell(headerCell("Tháng"));
            table.addCell(headerCell("Năm"));
            table.addCell(headerCell("Doanh thu"));
            table.addCell(headerCell("Offline"));
            table.addCell(headerCell("Online"));
            table.addCell(headerCell("Công nợ"));

            for (MonthlyReportDTO m : report.getMonthly()) {
                table.addCell(cell(Integer.toString(m.getMonth())));
                table.addCell(cell(Integer.toString(m.getYear())));
                table.addCell(cell(m.getRevenue().toString()));
                table.addCell(cell(m.getOffline().toString()));
                table.addCell(cell(m.getOnline().toString()));
                table.addCell(cell(m.getDebt().toString()));
            }

            document.add(table);
            document.close();

            return out.toByteArray();
        }
    }

    private PdfPCell headerCell(String text) {
        Font f = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
        PdfPCell c = new PdfPCell(new Phrase(text, f));
        c.setBackgroundColor(Color.GRAY);
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        c.setPadding(6f);
        return c;
    }

    private PdfPCell cell(String text) {
        PdfPCell c = new PdfPCell(new Phrase(text == null ? "" : text));
        c.setPadding(4f);
        return c;
    }
}
