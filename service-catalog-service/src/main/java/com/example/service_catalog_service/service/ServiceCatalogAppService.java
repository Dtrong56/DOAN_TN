package com.example.service_catalog_service.service;

import com.example.service_catalog_service.dto.CreateServiceRequest;
import com.example.service_catalog_service.dto.PackagePriceHistoryResponse;
import com.example.service_catalog_service.dto.ServiceCatalogDetailResponse;
import com.example.service_catalog_service.dto.ServicePackageDetailResponse;
import com.example.service_catalog_service.dto.ServicePackageRequest;
import com.example.service_catalog_service.dto.UpdateServiceRequest;
import com.example.service_catalog_service.entity.PackagePriceHistory;
import com.example.service_catalog_service.entity.ServiceCatalog;
import com.example.service_catalog_service.entity.ServicePackage;
import com.example.service_catalog_service.repository.ServiceCatalogRepository;
import com.example.service_catalog_service.repository.ServicePackageRepository;
import com.example.service_catalog_service.repository.PackagePriceHistoryRepository;
import com.example.service_catalog_service.security.TenantContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ServiceCatalogAppService {

    private final ServiceCatalogRepository serviceCatalogRepository;
    private final ServicePackageRepository servicePackageRepository;
    private final PackagePriceHistoryRepository historyRepo;
    private final TenantContext tenantContext;

    /*
     * Tạo dịch vụ mới
     */
    @Transactional
    public ServiceCatalog createService(CreateServiceRequest request) {

        String tenantId = tenantContext.getTenantId();

        ServiceCatalog service = new ServiceCatalog();
        service.setTenantId(tenantId);
        service.setName(request.name());
        service.setDescription(request.description());
        service.setUnit(request.unit());
        service.setActive(true);
        serviceCatalogRepository.save(service);

        if (request.packages() != null && !request.packages().isEmpty()) {
            for (ServicePackageRequest pkg : request.packages()) {
                ServicePackage sp = new ServicePackage();
                sp.setServiceCatalog(service);
                sp.setName(pkg.name());
                sp.setDescription(pkg.description());
                sp.setDurationMonths(pkg.durationMonths());
                sp.setPrice(pkg.price());
                sp.setActive(true);
                servicePackageRepository.save(sp);
            }
        }

        return service;
    }


    /*
     * Cập nhật dịch vụ
     */
    @Transactional
    public ServiceCatalog updateService(String serviceId, UpdateServiceRequest request) {

        String tenantId = tenantContext.getTenantId();
        String userId = tenantContext.getUserId();

        ServiceCatalog service = serviceCatalogRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));

        if (!service.getTenantId().equals(tenantId)) {
            throw new RuntimeException("Access denied: Wrong tenant");
        }

        // Cập nhật thông tin dịch vụ
        service.setName(request.name());
        service.setDescription(request.description());
        service.setUnit(request.unit());
        serviceCatalogRepository.save(service);

        // Cập nhật danh sách gói
        if (request.packages() != null) {
            for (var pkgReq : request.packages()) {

                // 1. Nếu id == null → gói mới
                if (pkgReq.id() == null) {
                    ServicePackage newPkg = new ServicePackage();
                    newPkg.setServiceCatalog(service);
                    newPkg.setName(pkgReq.name());
                    newPkg.setDescription(pkgReq.description());
                    newPkg.setDurationMonths(pkgReq.durationMonths());
                    newPkg.setPrice(pkgReq.price());
                    newPkg.setActive(pkgReq.active() != null ? pkgReq.active() : true);
                    servicePackageRepository.save(newPkg);
                    continue;
                }

                // 2. Nếu là gói cũ → tải ra để update
                ServicePackage pkg = servicePackageRepository.findById(pkgReq.id())
                        .orElseThrow(() -> new RuntimeException("Package not found"));

                // Nếu đổi giá → ghi lịch sử
                if (pkgReq.price() != null && pkg.getPrice().compareTo(pkgReq.price()) != 0) {
                    PackagePriceHistory history = new PackagePriceHistory();
                    history.setServicePackage(pkg);
                    history.setOldPrice(pkg.getPrice());
                    history.setNewPrice(pkgReq.price());
                    history.setChangedByUserId(userId);
                    historyRepo.save(history);
                    pkg.setPrice(pkgReq.price());
                }

                pkg.setName(pkgReq.name());
                pkg.setDescription(pkgReq.description());
                pkg.setDurationMonths(pkgReq.durationMonths());
                if (pkgReq.active() != null) pkg.setActive(pkgReq.active());

                servicePackageRepository.save(pkg);
            }
        }

        return service;
    }

    /*
     * Vô hiệu hóa dịch vụ
     */
    @Transactional
    public void deactivateService(String serviceId) {

        String tenantId = tenantContext.getTenantId();

        ServiceCatalog service = serviceCatalogRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));

        if (!service.getTenantId().equals(tenantId)) {
            throw new RuntimeException("Access denied: Wrong tenant");
        }

        service.setActive(false);
        serviceCatalogRepository.save(service);
    }

    /*
     * Lấy chi tiết dịch vụ theo ID
     */
    public ServiceCatalogDetailResponse getServiceDetail(String serviceId) {

        String tenantId = tenantContext.getTenantId();

        ServiceCatalog service = serviceCatalogRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));

        if (!service.getTenantId().equals(tenantId)) {
            throw new RuntimeException("Access denied");
        }

        var packages = servicePackageRepository.findByServiceCatalogId(serviceId)
                .stream()
                .map(pkg -> new ServicePackageDetailResponse(
                        pkg.getId(),
                        pkg.getName(),
                        pkg.getDescription(),
                        pkg.getDurationMonths(),
                        pkg.getPrice(),
                        pkg.isActive(),
                        historyRepo.findByServicePackageIdOrderByEffectiveFromDesc(pkg.getId())
                                .stream()
                                .map(h -> new PackagePriceHistoryResponse(
                                        h.getOldPrice(),
                                        h.getNewPrice(),
                                        h.getEffectiveFrom(),
                                        h.getChangedAt(),
                                        h.getChangedByUserId()
                                ))
                                .toList()
                )).toList();

        return new ServiceCatalogDetailResponse(
                service.getId(),
                service.getName(),
                service.getDescription(),
                service.getUnit(),
                service.isActive(),
                packages
        );
    }


    /*
     * Liệt kê tất cả dịch vụ
     */
    public List<ServiceCatalog> listServices() {

        String tenantId = tenantContext.getTenantId();

        return serviceCatalogRepository.findByTenantIdAndActiveTrue(tenantId);
    }
}
