package com.example.resident_service.service;

import com.example.resident_service.client.AuthFeignClient;
import com.example.resident_service.dto.*;
import com.example.resident_service.entity.*;
import com.example.resident_service.repository.*;
import com.example.resident_service.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApartmentImportService {

    private final BuildingRepository buildingRepository;
    private final ApartmentRepository apartmentRepository;
    private final ResidentProfileRepository residentProfileRepository;
    private final ResidentAccountRepository residentAccountRepository;
    private final ApartmentOwnershipRepository ownershipRepository;
    private final AuthFeignClient authFeignClient;
    private final TenantContext tenantContext;


    // Preview: read excel and validate basic rules (no DB write)
    public ImportPreviewResponse preview(MultipartFile file) throws Exception {
        List<ApartmentImportRow> rows = parseExcel(file.getInputStream());
        List<Map<String, String>> errors = new ArrayList<>();
        int idx = 1;
        for (ApartmentImportRow r : rows) {
            if (isBlank(r.getBuildingName())) {
                errors.add(Map.of("row", String.valueOf(idx), "message", "building_name is required"));
            }
            if (isBlank(r.getApartmentCode())) {
                errors.add(Map.of("row", String.valueOf(idx), "message", "apartment_code is required"));
            }
            if (isBlank(r.getResidentCccd())) {
                errors.add(Map.of("row", String.valueOf(idx), "message", "resident_cccd is required"));
            }
            if (r.getFloor() == null) {
                errors.add(Map.of("row", String.valueOf(idx), "message", "floor is required"));
            }
            if (r.getAreaM2() == null) {
                errors.add(Map.of("row", String.valueOf(idx), "message", "area_m2 is required"));
            }
            idx++;
        }
        int total = rows.size();
        int invalid = errors.size();
        return ImportPreviewResponse.builder()
                .tenantId(tenantContext.getTenantId())
                .totalRows(total)
                .validRows(total - invalid)
                .invalidRows(invalid)
                .previewRows(rows)
                .errors(errors)
                .build();
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private List<ApartmentImportRow> parseExcel(InputStream in) throws Exception {
        List<ApartmentImportRow> list = new ArrayList<>();
        try (Workbook wb = new XSSFWorkbook(in)) {
            Sheet sheet = wb.getSheetAt(0);
            boolean first = true;
            for (Row row : sheet) {
                if (first) { first = false; continue; } // skip header
                if (row == null) continue;
                ApartmentImportRow r = new ApartmentImportRow();
                r.setBuildingName(getString(row, 0));
                r.setBuildingAddress(getString(row, 1));
                r.setApartmentCode(getString(row, 2));
                r.setFloor(getInt(row, 3));
                r.setAreaM2(getDouble(row, 4));
                r.setResidentFullName(getString(row, 5));
                r.setResidentCccd(getString(row, 6));
                r.setResidentPhone(getString(row, 7));
                r.setResidentEmail(getString(row, 8));
                r.setResidentDateOfBirth(getString(row, 9));
                if (r.getBuildingName() == null && r.getApartmentCode() == null && r.getResidentCccd() == null) {
                    continue; // skip empty row
                }
                list.add(r);
            }
        }
        return list;
    }

    private String getString(Row row, int idx) {
        Cell c = row.getCell(idx);
        if (c == null) return null;
        c.setCellType(CellType.STRING);
        return c.getStringCellValue().trim();
    }

    private Integer getInt(Row row, int idx) {
        try {
            Cell c = row.getCell(idx);
            if (c == null) return null;
            if (c.getCellType() == CellType.NUMERIC) {
                return (int) c.getNumericCellValue();
            } else {
                String s = c.getStringCellValue().trim();
                if (s.isEmpty()) return null;
                return Integer.parseInt(s);
            }
        } catch (Exception e) { return null; }
    }

    private Double getDouble(Row row, int idx) {
        try {
            Cell c = row.getCell(idx);
            if (c == null) return null;
            if (c.getCellType() == CellType.NUMERIC) {
                return c.getNumericCellValue();
            } else {
                String s = c.getStringCellValue().trim();
                if (s.isEmpty()) return null;
                return Double.parseDouble(s);
            }
        } catch (Exception e) { return null; }
    }

    // Confirm bulk import: transactional
    @Transactional(rollbackFor = Exception.class)
    public ImportResultResponse confirmBulk(ImportConfirmRequest request) {
        String tenantId = tenantContext.getTenantId();
        if (tenantId == null) {
            throw new RuntimeException("Missing tenantId in JWT");
        }
        List<ApartmentImportRow> rows = Optional.ofNullable(request.getApartments()).orElse(Collections.emptyList());
        if (rows.isEmpty()) {
            return new ImportResultResponse(false, "No rows provided", 0);
        }

        // 1) Kiểm tra và tạo mới các Building nếu chưa tồn tại
        Map<String, String> buildingAddressMap = rows.stream()
                .collect(Collectors.toMap(
                    r -> r.getBuildingName().trim(),
                    ApartmentImportRow::getBuildingAddress,
                    (addr1, addr2) -> addr1 // giữ địa chỉ đầu tiên nếu trùng tên
                ));

        // Lấy tất cả Building của tenant
        List<Building> tenantBuildings = buildingRepository.findByTenantId(tenantId);
        Map<String, Building> buildingMap = tenantBuildings.stream()
                .collect(Collectors.toMap(Building::getName, b -> b));
        
        // Gom nhóm các dòng theo buildingName để tính tổng số tầng
        Map<String, Integer> buildingFloorsMap = rows.stream()
            .collect(Collectors.groupingBy(
                r -> r.getBuildingName().trim(),
                Collectors.collectingAndThen(
                    Collectors.mapping(ApartmentImportRow::getFloor, Collectors.toSet()),
                    floors -> floors.stream().max(Integer::compareTo).orElse(1) // lấy tầng cao nhất
                )
            ));

        // Tạo mới các Building chưa tồn tại
        for (Map.Entry<String, String> entry : buildingAddressMap.entrySet()) {
            String name = entry.getKey();
            String address = entry.getValue();

            if (!buildingMap.containsKey(name)) {
                int totalFloors = buildingFloorsMap.getOrDefault(name, 1); // mặc định là 1 nếu không tìm thấy
                Building newBuilding = Building.builder()
                    .name(name)
                    .address(address)
                    .tenantId(tenantId)
                    .totalFloors(totalFloors)
                    .build();
                buildingRepository.save(newBuilding);
                buildingMap.put(name, newBuilding);
            }
        }

        // 2) Chuẩn bị danh sách user cần tạo trong auth-service
        //    Chúng ta cần tạo user nếu ResidentProfile chưa có hoặc đã có nhưng chưa có ResidentAccount trong tenant này
        Map<String, ApartmentImportRow> byCccd = new LinkedHashMap<>();
        for (ApartmentImportRow r : rows) {
            byCccd.put(r.getResidentCccd(), r);
        }

        List<AuthUserCreateRequest> toCreateUsers = new ArrayList<>();
        Map<String, ApartmentImportRow> cccdToRow = new HashMap<>();

        for (ApartmentImportRow r : rows) {
            String cccd = r.getResidentCccd();
            cccdToRow.put(cccd, r);

            // check existing profile
            Optional<ResidentProfile> optProfile = residentProfileRepository.findByCccd(cccd);
            boolean hasAccountInTenant = false;
            if (optProfile.isPresent()) {
                ResidentProfile p = optProfile.get();
                // check if ResidentAccount exists for this tenant and this profile
                List<ResidentAccount> accounts = residentAccountRepository.findByTenantId(tenantId);
                hasAccountInTenant = accounts.stream()
                        .anyMatch(acc -> acc.getResidentProfile().getId().equals(p.getId()));
            }
            if (!hasAccountInTenant) {
                // Need to create user in auth-service for this resident
                AuthUserCreateRequest authReq = new AuthUserCreateRequest(
                        r.getResidentFullName(),
                        r.getResidentCccd(),
                        r.getResidentEmail(),
                        r.getResidentPhone(),
                        tenantId
                );
                toCreateUsers.add(authReq);
            }
        }

        // 3) Gọi auth-service để tạo users
        Map<String,String> createdUserIdByCccd = new HashMap<>(); // cccd -> userId
        if (!toCreateUsers.isEmpty()) {
            AuthBulkCreateResponse authResp = authFeignClient.bulkCreateUsers(toCreateUsers);
            if (authResp == null || !authResp.isSuccess()) {
                throw new RuntimeException("Auth service failed to create users");
            }
            for (AuthCreateResult r : authResp.getResults()) {
                if (r.getUserId() == null) {
                    // any failed creation -> rollback
                    throw new RuntimeException("Auth service failed for cccd=" + r.getCccd() + ", err=" + r.getError());
                }
                createdUserIdByCccd.put(r.getCccd(), r.getUserId());
            }
        }

        // 4) Tiến hành tạo mới Apartment, ResidentProfile, ResidentAccount, ApartmentOwnership
        int createdCount = 0;
        for (ApartmentImportRow row : rows) {
            // find building (guaranteed exists)
            Optional<Building> opt = Optional.ofNullable(buildingMap.get(row.getBuildingName()));
            Building building = opt.orElseThrow(() -> new RuntimeException("Building not found: " + row.getBuildingName()));

            // Check existing apartment by code
            Optional<Apartment> optApartment = apartmentRepository.findByBuildingId(building.getId())
                    .stream()
                    .filter(a -> a.getCode().equals(row.getApartmentCode()))
                    .findFirst();

            Apartment apartment;
            if (optApartment.isPresent()) {
                apartment = optApartment.get();
                // By your choice earlier: we are using option 1 => skip update
            } else {
                apartment = Apartment.builder()
                        .building(building)
                        .code(row.getApartmentCode())
                        .floor(row.getFloor())
                        .areaM2(BigDecimal.valueOf(row.getAreaM2()))
                        .build();
                apartmentRepository.save(apartment);
            }

            // ResidentProfile
            ResidentProfile profile = residentProfileRepository.findByCccd(row.getResidentCccd())
                    .orElseGet(() -> {
                        ResidentProfile p = ResidentProfile.builder()
                                .fullName(row.getResidentFullName())
                                .cccd(row.getResidentCccd())
                                .phone(row.getResidentPhone())
                                .email(row.getResidentEmail())
                                .dateOfBirth(parseDate(row.getResidentDateOfBirth()))
                                .build();
                        return residentProfileRepository.save(p);
                    });

            // Does ResidentAccount exist for this tenant & profile?
            boolean accountExists = residentAccountRepository.findByTenantId(tenantId).stream()
                    .anyMatch(acc -> acc.getResidentProfile().getId().equals(profile.getId()));

            if (!accountExists) {
                // userId phải có từ auth-service bulk create
                String userId = createdUserIdByCccd.get(row.getResidentCccd());
                if (userId == null) {
                    throw new RuntimeException("Auth service did not return userId for cccd=" + row.getResidentCccd());
                }

                ResidentAccount account = ResidentAccount.builder()
                        .tenantId(tenantId)
                        .userId(userId)
                        .residentProfile(profile)
                        .status(ResidentAccountStatus.PENDING) // cư dân chưa đổi mật khẩu lần đầu
                        .build();

                residentAccountRepository.save(account);
            }

            // ApartmentOwnership: link profile -> apartment
            boolean ownershipExists = ownershipRepository.findByApartmentId(apartment.getId())
                    .stream()
                    .anyMatch(o -> o.getResidentId().equals(profile.getId()));
            if (!ownershipExists) {
                ApartmentOwnership own = ApartmentOwnership.builder()
                        .apartment(apartment)
                        .residentId(profile.getId())
                        .startDate(LocalDate.now())
                        .isRepresentative(true)
                        .build();
                ownershipRepository.save(own);
                createdCount++;
            }
        }

        // 5) Optionally create Monitoring log here via MonitoringClient (omitted for brevity)
        return new ImportResultResponse(true, "Imported successfully", createdCount);
    }

    private LocalDate parseDate(String s) {
        try {
            if (s == null || s.isBlank()) return null;
            return LocalDate.parse(s);
        } catch (Exception e) {
            return null;
        }
    }
}
