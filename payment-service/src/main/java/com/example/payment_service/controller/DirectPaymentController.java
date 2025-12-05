package com.example.payment_service.controller;

import com.example.payment_service.dto.DirectPaymentRequest;
import com.example.payment_service.dto.DirectPaymentResponse;
import com.example.payment_service.service.DirectPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment/direct")
@RequiredArgsConstructor
public class DirectPaymentController {

    private final DirectPaymentService directPaymentService;

    @PostMapping
    public DirectPaymentResponse recordPayment(@RequestBody DirectPaymentRequest request) {
        return directPaymentService.recordDirectPayment(request);
    }
}
