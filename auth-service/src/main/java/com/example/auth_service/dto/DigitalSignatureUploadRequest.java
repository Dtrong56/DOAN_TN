package com.example.auth_service.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;


@Data
public class DigitalSignatureUploadRequest {

    private MultipartFile publicKeyFile;    // public.pem
    private MultipartFile certificateFile;  // optional .cer
    private LocalDate validFrom;
    private LocalDate validTo;
}

