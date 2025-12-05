package com.example.payment_service.service;

import com.example.payment_service.client.MonitoringClient;
import com.example.payment_service.dto.SystemLogDTO;
import com.example.payment_service.dto.DirectPaymentRequest;
import com.example.payment_service.dto.DirectPaymentResponse;
import com.example.payment_service.entity.Invoice;
import com.example.payment_service.entity.PaymentRecord;
import com.example.payment_service.repository.InvoiceRepository;
import com.example.payment_service.repository.PaymentRecordRepository;
import com.example.payment_service.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DirectPaymentService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRecordRepository paymentRecordRepository;

    private final TenantContext tenantContext;
    private final MonitoringClient monitoringClient;

    @Transactional
    public DirectPaymentResponse recordDirectPayment(DirectPaymentRequest request) {

        String tenantId = tenantContext.getTenantId();
        String bqlUserId = tenantContext.getUserId(); // user BQL đang thực hiện

        // 1. Lấy invoice
        Invoice invoice = invoiceRepository.findById(request.getInvoiceId())
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        if (!invoice.getTenantId().equals(tenantId)) {
            throw new RuntimeException("Invoice does not belong to current tenant");
        }

        if (invoice.getStatus() == Invoice.Status.PAID) {
            throw new RuntimeException("Invoice already paid");
        }

        // 2. Tạo PaymentRecord
        PaymentRecord record = new PaymentRecord();
        record.setTenantId(tenantId);
        record.setInvoice(invoice);
        record.setAmount(request.getAmount());
        record.setMethod(request.getMethod());
        record.setPaymentDate(LocalDateTime.now());
        record.setProcessedBy(bqlUserId);
        record.setNote(request.getNote());

        paymentRecordRepository.save(record);

        // 3. Cập nhật trạng thái hóa đơn → PAID
        invoice.setStatus(Invoice.Status.PAID);
        invoiceRepository.save(invoice);

        // 4. Gửi log sang Monitoring Service
        monitoringClient.createLog(
                SystemLogDTO.builder()
                        .timestamp(LocalDateTime.now())
                        .tenantId(tenantId)
                        .userId(bqlUserId)
                        .role("BQL")
                        .action("DIRECT_PAYMENT")
                        .objectType("Invoice")
                        .objectId(invoice.getId())
                        .message("BQL ghi nhận thanh toán trực tiếp cho hóa đơn.")
                        .serviceName("payment-service")
                        .endpoint("/payment/direct")
                        .build()
        );

        // 5. Trả về response
        return DirectPaymentResponse.builder()
                .paymentRecordId(record.getId())
                .invoiceId(invoice.getId())
                .amount(record.getAmount())
                .method(record.getMethod().name())
                .paidAt(record.getPaymentDate())
                .processedBy(record.getProcessedBy())
                .build();
    }
}
