package com.example.payment_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;
import java.math.BigDecimal;

@RestController
@RequestMapping("/sandbox")
public class SandboxGatewayController {

    @GetMapping("/pay")
    public RedirectView fakePay(
            @RequestParam String transactionId,
            @RequestParam BigDecimal amount
    ) {
        String redirect = "http://localhost:8080/payment/online/return?"
                + "transactionId=" + transactionId
                + "&status=SUCCESS"
                + "&gatewayCode=SANDBOX123";

        return new RedirectView(redirect);
    }
}

