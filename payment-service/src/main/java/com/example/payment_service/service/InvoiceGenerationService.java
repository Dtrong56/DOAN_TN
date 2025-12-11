package com.example.payment_service.service;

import com.example.payment_service.client.OperationContractClient;
import com.example.payment_service.client.ResidentClient;
import com.example.payment_service.dto.OperationContractDTO;
import com.example.payment_service.dto.ResidentDTO;
import com.example.payment_service.dto.ServiceAppendixDTO;
import com.example.payment_service.entity.Invoice;
import com.example.payment_service.entity.InvoiceItem;
import com.example.payment_service.repository.InvoiceItemRepository;
import com.example.payment_service.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoiceGenerationService {

    private final ResidentClient residentClient;
    private final OperationContractClient operationContractClient;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;

    public int generateMonthlyInvoices(String tenantId, int month, int year) {

        // 1) Lấy cư dân
        List<ResidentDTO> residents = residentClient.getResidents(
                tenantId, true, true
        );

        if (residents.isEmpty()) {
            return 0;
        }

        // 2) Lấy hợp đồng vận hành (có thể dùng nếu cần)
        OperationContractDTO contract = operationContractClient.getActiveContract(tenantId);

        int created = 0;

        // 3) Tạo cho từng cư dân
        for (ResidentDTO resident : residents) {

            List<ServiceAppendixDTO> appendices =
                    operationContractClient.getActiveAppendices(
                            tenantId,
                            resident.getResidentId(),
                            month,
                            year
                    );

            if (appendices.isEmpty()) continue;

            // 4) Tính tổng
            BigDecimal total = appendices.stream()
                    .map(ServiceAppendixDTO::getUnitPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 5) Ngày hết hạn (ví dụ: ngày cuối tháng)
            LocalDate dueDate = LocalDate.of(year, month, 1)
                    .with(TemporalAdjusters.lastDayOfMonth());

            // 6) Tạo invoice
            Invoice invoice = new Invoice();
            invoice.setTenantId(tenantId);
            invoice.setResidentId(resident.getResidentId());
            invoice.setApartmentId(resident.getApartment().getApartmentId());
            invoice.setPeriodMonth(month);
            invoice.setPeriodYear(year);
            invoice.setTotalAmount(total);
            invoice.setDueDate(dueDate);
            invoice.setStatus(Invoice.Status.UNPAID);

            invoiceRepository.save(invoice);

            // 7) Tạo invoice items theo đúng entity của bạn
            for (ServiceAppendixDTO sp : appendices) {

                InvoiceItem item = new InvoiceItem();
                item.setInvoice(invoice);

                // build description
                String desc = String.format(
                        "%s – Gói: %s – Giá: %,.0f VND",
                        sp.getServiceName(),
                        sp.getPackageName(),
                        sp.getUnitPrice()
                );

                item.setDescription(desc);
                item.setAmount(sp.getUnitPrice());
                item.setServiceAppendixId(sp.getAppendixId());

                invoiceItemRepository.save(item);
            }

            created++;
        }

        return created;
    }
}
