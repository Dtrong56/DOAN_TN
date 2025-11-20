package com.example.contract_service.dto;

import com.example.contract_service.entity.MainContract;
import com.example.contract_service.entity.ServiceAppendix;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

@Data
public class ContractDto {
    private String contractId;
    private String contractCode;
    private LocalDate signedDate;
    private LocalDate effectiveDate;
    private LocalDate expirationDate;
    private String pdfUrl;
    private List<AppendixDto> appendices = new ArrayList<>();

    public static ContractDto from(MainContract contract, List<ServiceAppendix> appendices, String baseUrl) {
        ContractDto dto = new ContractDto();
        dto.setContractId(contract.getId());
        dto.setContractCode(contract.getContractCode());
        dto.setSignedDate(contract.getSignedDate());
        dto.setEffectiveDate(contract.getEffectiveDate());
        dto.setExpirationDate(contract.getExpirationDate());
        dto.setPdfUrl(baseUrl + "/contracts/" + contract.getId() + "/pdf");
        if (appendices != null) {
            List<AppendixDto> appendixDtos = appendices.stream().map(a -> {
                AppendixDto adto = new AppendixDto();
                adto.setAppendixId(a.getId());
                adto.setServiceId(a.getServiceId());
                adto.setPackageId(a.getPackageId());
                adto.setResidentId(a.getResidentId());
                adto.setApartmentId(a.getApartmentId());
                adto.setSignedDate(a.getSignedDate());
                adto.setEffectiveDate(a.getEffectiveDate());
                adto.setExpirationDate(a.getExpirationDate());
                adto.setAppendixPdfUrl(baseUrl + "/appendices/" + a.getId() + "/pdf");
                adto.setAppendixStatus(a.getAppendixStatus());
                return adto;
            }).toList();
            dto.setAppendices(appendixDtos);
        }
        return dto;
    }
}
