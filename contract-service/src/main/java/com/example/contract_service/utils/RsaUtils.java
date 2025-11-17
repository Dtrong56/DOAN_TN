package com.example.contract_service.utils;

import java.security.*;
import java.security.spec.X509EncodedKeySpec;
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
                    .replaceAll("\\s", ""); // xóa newline

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
    public static boolean verifyBase64(
            String signedHashBase64,
            String signatureBase64,
            PublicKey publicKey,
            String algorithm
    ) {
        try {
            byte[] signedHash = Base64.getDecoder().decode(signedHashBase64);
            byte[] signature = Base64.getDecoder().decode(signatureBase64);

            Signature sig = Signature.getInstance(algorithm);
            sig.initVerify(publicKey);
            sig.update(signedHash);

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
