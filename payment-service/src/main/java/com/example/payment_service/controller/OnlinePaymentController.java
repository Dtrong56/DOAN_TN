package com.example.payment_service.controller;

import com.example.payment_service.dto.InitOnlinePaymentRequest;
import com.example.payment_service.dto.InitOnlinePaymentResponse;
import com.example.payment_service.dto.PaymentCallbackResult;
import com.example.payment_service.service.OnlinePaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payment/online")
@RequiredArgsConstructor
public class OnlinePaymentController {

    private final OnlinePaymentService service;

    @PostMapping("/init")
    public InitOnlinePaymentResponse init(@RequestBody InitOnlinePaymentRequest request) {
        return service.initPayment(request);
    }

    @GetMapping("/return")
    public PaymentCallbackResult callback(
            @RequestParam String transactionId,
            @RequestParam String status,
            @RequestParam(required = false) String gatewayCode
    ) {
        return service.handleCallback(transactionId, status, gatewayCode);
    }
}

