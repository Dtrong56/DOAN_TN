package com.example.contract_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import com.example.contract_service.dto.DigitalSignatureInternalDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "auth-service")
public interface AuthFeignClient {

   @GetMapping("/auth/internal/digital-signature/{userId}")
    DigitalSignatureInternalDTO getDigitalSignature(@PathVariable("userId") String userId);

}
