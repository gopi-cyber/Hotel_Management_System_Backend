package com.luxestay.hotel.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/otp")
public class OtpController {

    private static class OtpRecord {
        final String code;
        final long expiresAt;
        int attempts;

        OtpRecord(String code, long expiresAt) {
            this.code = code;
            this.expiresAt = expiresAt;
            this.attempts = 0;
        }
    }

    private final Map<String, OtpRecord> otpStore = new ConcurrentHashMap<>();

    private String cleanPhone(String raw) {
        if (raw == null) return "";
        return raw.replaceAll("\\D", "");
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> body) {
        String phone = cleanPhone(body.get("phone"));
        if (phone.length() < 8) {
            return ResponseEntity.badRequest().body(Map.of("error", "Valid mobile number required"));
        }

        String code = String.valueOf((int) (100000 + Math.random() * 900000));
        long expiresAt = System.currentTimeMillis() + (5 * 60 * 1000);

        otpStore.put(phone, new OtpRecord(code, expiresAt));

        return ResponseEntity.ok(Map.of(
            "success", true,
            "phone", phone,
            "message", "OTP generated and ready for verification",
            "otp", code,
            "expiresInSeconds", 300
        ));
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> body) {
        String phone = cleanPhone(body.get("phone"));
        String inputCode = body.getOrDefault("code", "").trim();

        OtpRecord record = otpStore.get(phone);
        if (record == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "No OTP requested for this mobile number."));
        }

        if (System.currentTimeMillis() > record.expiresAt) {
            otpStore.remove(phone);
            return ResponseEntity.badRequest().body(Map.of("error", "OTP has expired. Please request a new code."));
        }

        if (record.attempts >= 5) {
            otpStore.remove(phone);
            return ResponseEntity.status(429).body(Map.of("error", "Maximum attempts exceeded. Request a new OTP."));
        }

        record.attempts++;

        if (!record.code.equals(inputCode)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid OTP code. Please try again."));
        }

        otpStore.remove(phone);
        return ResponseEntity.ok(Map.of(
            "verified", true,
            "phone", phone,
            "message", "Phone number verified successfully"
        ));
    }
}
