package com.example.service_catalog_service.controller;

import com.example.service_catalog_service.dto.CreateServiceRequest;
import com.example.service_catalog_service.dto.ServiceCatalogDetailResponse;
import com.example.service_catalog_service.dto.UpdateServiceRequest;
import com.example.service_catalog_service.entity.ServiceCatalog;
import com.example.service_catalog_service.service.ServiceCatalogAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/services")
@RequiredArgsConstructor
public class ServiceCatalogController {

    private final ServiceCatalogAppService service;

    //endpoint liệt kê tất cả dịch vụ
    @GetMapping
    public List<ServiceCatalog> listServices() {
        return service.listServices();
    }

    //endpoint lấy chi tiết dịch vụ theo ID
    @GetMapping("/{serviceId}")
    public ServiceCatalogDetailResponse getServiceDetail(@PathVariable String serviceId) {
        return service.getServiceDetail(serviceId);
    }

    //endpoint tạo dịch vụ mới
    @PostMapping
    public ServiceCatalog createService(@RequestBody CreateServiceRequest request) {
        return service.createService(request);
    }

    //endpoint cập nhật dịch vụ
    @PutMapping("/serviceUpdate")
    public ServiceCatalog updateService(@RequestBody UpdateServiceRequest request) {
        return service.updateService(request);
    }

    //endpoint hủy kích hoạt dịch vụ
    @PutMapping("/{serviceId}/deactivate")
    public void deactivate(@PathVariable String serviceId) {
        service.deactivateService(serviceId);
    }

}
