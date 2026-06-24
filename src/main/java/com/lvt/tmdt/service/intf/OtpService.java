package com.lvt.tmdt.service.intf;

public interface OtpService {
    String generateOtp(String email);
    boolean validateOtp(String email, String otp);
    void clearOtp(String email);
}
