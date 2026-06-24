package com.lvt.tmdt.service.impl;

import com.lvt.tmdt.service.intf.OtpService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpServiceImpl implements OtpService {

    private static class OtpData {
        String otpCode;
        long expireTime;

        public OtpData(String otpCode, long expireTime) {
            this.otpCode = otpCode;
            this.expireTime = expireTime;
        }
    }

    private final Map<String, OtpData> otpCache = new ConcurrentHashMap<>();
    private final long EXPIRE_MINS = 5;

    @Override
    public String generateOtp(String email) {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        String otpCode = String.valueOf(otp);
        
        long expireTime = System.currentTimeMillis() + (EXPIRE_MINS * 60 * 1000);
        otpCache.put(email, new OtpData(otpCode, expireTime));
        
        return otpCode;
    }

    @Override
    public boolean validateOtp(String email, String otp) {
        OtpData data = otpCache.get(email);
        if (data == null) {
            return false;
        }
        if (System.currentTimeMillis() > data.expireTime) {
            otpCache.remove(email);
            return false;
        }
        return data.otpCode.equals(otp);
    }

    @Override
    public void clearOtp(String email) {
        otpCache.remove(email);
    }
}
