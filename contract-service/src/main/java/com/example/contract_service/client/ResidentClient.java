package com.example.contract_service.client;

import com.example.contract_service.dto.ResidentInfoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@FeignClient(name = "resident-service", path = "/api/internal")
public interface ResidentClient {

    @GetMapping("/resident-accounts/by-user-contract/{userId}")
    ResidentInfoDTO getResidentByUserId(@PathVariable("userId") String userId);
}
//