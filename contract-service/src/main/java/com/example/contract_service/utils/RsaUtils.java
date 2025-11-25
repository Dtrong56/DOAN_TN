package com.example.contract_service.utils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;

public class RsaUtils {

    /**
     * Load public key từ PEM content (string).
     *
     * @param pem public key dạng:
     *            -----BEGIN PUBLIC KEY-----
     *            MIIBIjANBgkqh...
     *            -----END PUBLIC KEY-----
     */
    public static PublicKey loadPublicKey(String pem) {
        try {
            String sanitized = pem
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", ""); // bỏ newline, space

            // Loại bỏ mọi ký tự không phải Base64 (chỉ giữ A-Z, a-z, 0-9, +, /)
            sanitized = sanitized.replaceAll("[^A-Za-z0-9+/=]", "");

            byte[] encoded = Base64.getDecoder().decode(sanitized);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(keySpec);

        } catch (Exception e) {
            throw new RuntimeException("Cannot load public key: " + e.getMessage(), e);
        }
    }


    /**
     * Verify chữ ký: FE gửi:
     * - signedHash = hash(PDF) dạng base64
     * - signatureValue = chữ ký dạng base64(private-key)
     */
    public static boolean verifySignatureRaw(
        String rawContent,
        String signatureBase64,
        PublicKey publicKey,
        String algorithm
    ) {
        try {
            byte[] signature = Base64.getDecoder().decode(signatureBase64);
            System.out.println("signatureBase64 decoded length: "+signature.length);
            System.out.println("Raw content bytes: " + Arrays.toString(rawContent.getBytes(StandardCharsets.UTF_8)));
            System.out.println("Raw content: " + rawContent);
            System.out.println("Algorithm is: "+algorithm);

            InputStream is = RsaUtils.class.getResourceAsStream("/data.txt");
            byte[] fileBytes = is.readAllBytes();
            String rawContent_test = rawContent + "\n";
            System.out.println(Arrays.equals(fileBytes, rawContent_test.getBytes(StandardCharsets.UTF_8)));

            Signature sig = Signature.getInstance(algorithm); // ví dụ "SHA256withRSA"
            sig.initVerify(publicKey);
            sig.update(rawContent.getBytes(StandardCharsets.UTF_8)); // truyền dữ liệu gốc

            return sig.verify(signature);

        } catch (Exception e) {
            throw new RuntimeException("Error verifying signature: " + e.getMessage(), e);
        }
    }

    /**
     * Hash string → base64
     * (Nếu bạn muốn tự hash server-side để debug)
     */
    public static String hashSHA256ToBase64(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error hashing content: " + e.getMessage(), e);
        }
    }
}
