package com.example.contract_service.client;

import com.example.contract_service.dto.PackageInfoDTO;
import com.example.contract_service.dto.ServiceInfoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@FeignClient(name = "service-catalog-service", path = "/internal")
public interface CatalogClient {

    @GetMapping("/service/{serviceId}")
    ServiceInfoDTO getServiceInfo(@PathVariable String serviceId);

    @GetMapping("/service/{serviceId}/package/{packageId}")
    PackageInfoDTO getPackageOfService(
        @PathVariable String serviceId,
        @PathVariable String packageId
    );
}
//


