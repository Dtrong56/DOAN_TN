package com.example.payment_service.service;

import com.example.payment_service.dto.InitOnlinePaymentRequest;
import com.example.payment_service.dto.InitOnlinePaymentResponse;
import com.example.payment_service.dto.PaymentCallbackResult;
import com.example.payment_service.dto.SystemLogDTO;
import com.example.payment_service.entity.Invoice;
import com.example.payment_service.entity.PaymentMethod;
import com.example.payment_service.entity.PaymentTransaction;
import com.example.payment_service.repository.InvoiceRepository;
import com.example.payment_service.repository.PaymentMethodRepository;
import com.example.payment_service.repository.PaymentTransactionRepository;
import com.example.payment_service.security.TenantContext;
import com.example.payment_service.client.MonitoringClient;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OnlinePaymentService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentTransactionRepository transactionRepository;

    private final TenantContext tenantContext;
    private final MonitoringClient monitoringClient;

    // ================================
    // INIT PAYMENT
    // ================================
    @Transactional
    public InitOnlinePaymentResponse initPayment(InitOnlinePaymentRequest request) {

        String tenantId = tenantContext.getTenantId();
        String residentId = tenantContext.getResidentId();

        Invoice invoice = invoiceRepository.findById(request.getInvoiceId())
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        if (!invoice.getTenantId().equals(tenantId))
            throw new RuntimeException("Invoice does not belong to tenant");

        if (!invoice.getResidentId().equals(residentId))
            throw new RuntimeException("Invoice does not belong to this resident");

        if (invoice.getStatus() == Invoice.Status.PAID)
            throw new RuntimeException("Invoice already paid");

        PaymentMethod method = paymentMethodRepository.findById(request.getPaymentMethodId())
                .orElseThrow(() -> new RuntimeException("Payment method not found"));

        // Create transaction PENDING
        PaymentTransaction tx = new PaymentTransaction();
        tx.setInvoice(invoice);
        tx.setPaymentMethod(method);
        tx.setAmount(invoice.getTotalAmount());
        tx.setStatus(PaymentTransaction.Status.PENDING);
        tx = transactionRepository.save(tx);

        // Sandbox redirect URL
        String redirectUrl = 
                "http://localhost:8080/sandbox/pay?"
                + "transactionId=" + tx.getId()
                + "&amount=" + invoice.getTotalAmount();

        // Log
        monitoringClient.createLog(
                SystemLogDTO.builder()
                        .tenantId(tenantId)
                        .userId(residentId)
                        .role("RESIDENT")
                        .timestamp(LocalDateTime.now())
                        .action("ONLINE_PAYMENT_INIT")
                        .objectType("Invoice")
                        .objectId(invoice.getId())
                        .message("Resident initiated online payment")
                        .serviceName("payment-service")
                        .endpoint("/payment/online/init")
                        .build()
        );

        return InitOnlinePaymentResponse.builder()
                .transactionId(tx.getId())
                .redirectUrl(redirectUrl)
                .build();
    }


    // ================================
    // CALLBACK / RETURN
    // ================================
    @Transactional
    public PaymentCallbackResult handleCallback(
            String transactionId,
            String status,
            String gatewayCode
    ) {

        PaymentTransaction tx = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        Invoice invoice = tx.getInvoice();
        String tenantId = invoice.getTenantId();

        tx.setGatewayTransactionCode(gatewayCode);

        if (status.equalsIgnoreCase("SUCCESS")) {
            tx.setStatus(PaymentTransaction.Status.SUCCESS);
            invoice.setStatus(Invoice.Status.PAID);
        } else {
            tx.setStatus(PaymentTransaction.Status.FAILED);
        }

        transactionRepository.save(tx);
        invoiceRepository.save(invoice);

        // Log
        monitoringClient.createLog(
                SystemLogDTO.builder()
                        .tenantId(tenantId)
                        .userId(invoice.getResidentId())
                        .role("RESIDENT")
                        .timestamp(LocalDateTime.now())
                        .action("ONLINE_PAYMENT_RESULT")
                        .objectType("Invoice")
                        .objectId(invoice.getId())
                        .message("Online payment callback: " + status)
                        .serviceName("payment-service")
                        .endpoint("/payment/online/return")
                        .build()
        );

        return PaymentCallbackResult.builder()
                .transactionId(transactionId)
                .status(status)
                .invoiceStatus(invoice.getStatus().name())
                .build();
    }
}
