package com.example.payment_service.service;

import com.example.payment_service.dto.InvoiceDetailDTO;
import com.example.payment_service.dto.InvoiceItemDTO;
import com.example.payment_service.dto.InvoiceSummaryDTO;
import com.example.payment_service.entity.Invoice;
import com.example.payment_service.entity.InvoiceItem;
import com.example.payment_service.repository.InvoiceItemRepository;
import com.example.payment_service.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
public class InvoiceResidentService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;

    public List<InvoiceSummaryDTO> getInvoices(
            String tenantId,
            String residentId,
            Integer month,
            Integer year,
            String status
    ) {
        if (tenantId == null || residentId == null) {
            throw new ResponseStatusException(FORBIDDEN, "Unauthorized");
        }

        LocalDate now = LocalDate.now();
        int m = month == null ? now.getMonthValue() : month;
        int y = year == null ? now.getYear() : year;

        List<Invoice> invoices;

        if (status == null || status.isBlank()) {
            invoices = invoiceRepository
                    .findByTenantIdAndResidentIdAndPeriodYearAndPeriodMonth(
                            tenantId, residentId, y, m
                    );
        } else {
            Invoice.Status st;
            try {
                st = Invoice.Status.valueOf(status.toUpperCase());
            } catch (Exception ex) {
                throw new ResponseStatusException(BAD_REQUEST, "Invalid status value");
            }

            invoices = invoiceRepository
                    .findByTenantIdAndResidentIdAndPeriodYearAndPeriodMonthAndStatus(
                            tenantId, residentId, y, m, st
                    );
        }

        return invoices.stream().map(inv -> {
            InvoiceSummaryDTO dto = new InvoiceSummaryDTO();
            dto.setInvoiceId(inv.getId());
            dto.setPeriodMonth(inv.getPeriodMonth());
            dto.setPeriodYear(inv.getPeriodYear());
            dto.setTotalAmount(inv.getTotalAmount());
            dto.setStatus(inv.getStatus().name());
            dto.setDueDate(inv.getDueDate());
            return dto;
        }).collect(Collectors.toList());
    }

    public InvoiceDetailDTO getInvoiceDetail(String tenantId, String residentId, String invoiceId) {

        if (tenantId == null || residentId == null) {
            throw new ResponseStatusException(FORBIDDEN, "Unauthorized");
        }

        Invoice invoice =
                invoiceRepository.findByIdAndTenantIdAndResidentId(
                        invoiceId, tenantId, residentId
                ).orElseThrow(() ->
                        new ResponseStatusException(FORBIDDEN, "Invoice not found or access denied")
                );

        List<InvoiceItem> items = invoiceItemRepository.findByInvoiceId(invoiceId);

        InvoiceDetailDTO dto = new InvoiceDetailDTO();
        dto.setInvoiceId(invoice.getId());
        dto.setApartmentId(invoice.getApartmentId());
        dto.setPeriodMonth(invoice.getPeriodMonth());
        dto.setPeriodYear(invoice.getPeriodYear());
        dto.setTotalAmount(invoice.getTotalAmount());
        dto.setStatus(invoice.getStatus().name());
        dto.setDueDate(invoice.getDueDate());

        List<InvoiceItemDTO> itemDTOs = items.stream()
                .map(it -> {
                    InvoiceItemDTO i = new InvoiceItemDTO();
                    i.setDescription(it.getDescription());
                    i.setAmount(it.getAmount());
                    i.setServiceAppendixId(it.getServiceAppendixId());
                    return i;
                })
                .collect(Collectors.toList());

        dto.setItems(itemDTOs);
        dto.setItemsTotal(
                itemDTOs.stream()
                        .map(InvoiceItemDTO::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
        );

        return dto;
    }
}
