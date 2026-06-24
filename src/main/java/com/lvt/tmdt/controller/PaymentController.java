package com.lvt.tmdt.controller;

import com.lvt.tmdt.config.VNPayConfig;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.lvt.tmdt.repository.OrderRepository;
import com.lvt.tmdt.entity.Order;
import com.lvt.tmdt.enums.OrderStatus;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.nio.charset.StandardCharsets.US_ASCII;

import com.lvt.tmdt.service.intf.EmailService;

@RestController
@RequestMapping("/api/payment/vnpay")
@RequiredArgsConstructor
public class PaymentController {

    private final VNPayConfig vnPayConfig;
    private final OrderRepository orderRepository;
    private final EmailService emailService;

    @GetMapping("/create_payment")
    public ResponseEntity<?> createPayment(HttpServletRequest request,
                                           @RequestParam("amount") long amount,
                                           @RequestParam("orderId") String orderId) throws UnsupportedEncodingException {

        long vnpAmount = amount * 100L;

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", VNPayConfig.vnp_Version);
        vnp_Params.put("vnp_Command", VNPayConfig.vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnPayConfig.getVnp_TmnCode());
        vnp_Params.put("vnp_Amount", String.valueOf(vnpAmount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", orderId);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang: " + orderId);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnPayConfig.getVnp_ReturnUrl());
        vnp_Params.put("vnp_IpAddr", VNPayConfig.getIpAddress(request));

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, US_ASCII.toString()));
                
                query.append(URLEncoder.encode(fieldName, US_ASCII.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, US_ASCII.toString()));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = VNPayConfig.hmacSHA512(vnPayConfig.getSecretKey(), hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = vnPayConfig.getVnp_PayUrl() + "?" + queryUrl;

        Map<String, String> response = new HashMap<>();
        response.put("code", "00");
        response.put("message", "success");
        response.put("data", paymentUrl);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyPayment(HttpServletRequest request) {
        Map<String, String> fields = new HashMap<>();
        Enumeration<String> params = request.getParameterNames();
        while (params.hasMoreElements()) {
            String fieldName = params.nextElement();
            String fieldValue = request.getParameter(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0) && fieldName.startsWith("vnp_")) {
                fields.put(fieldName, fieldValue);
            }
        }

        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        if (fields.containsKey("vnp_SecureHashType")) {
            fields.remove("vnp_SecureHashType");
        }
        if (fields.containsKey("vnp_SecureHash")) {
            fields.remove("vnp_SecureHash");
        }

        String signValue = VNPayConfig.hashAllFields(fields, vnPayConfig.getSecretKey());
        
        Map<String, Object> response = new HashMap<>();
        
        if (signValue.equals(vnp_SecureHash)) {
            String orderIdStr = request.getParameter("vnp_TxnRef");
            String vnpAmountStr = request.getParameter("vnp_Amount");
            long vnpAmount = 0;
            if (vnpAmountStr != null) {
                try {
                    vnpAmount = Long.parseLong(vnpAmountStr);
                } catch (NumberFormatException e) {
                    // i    gnore
                }
            }
            try {
                Long orderId = Long.parseLong(orderIdStr);
                Order order = orderRepository.findById(orderId).orElse(null);

                if (order != null) {
                    long orderTotalAmount = order.getTotalAmount().longValue() * 100L;
                    boolean checkAmount = (vnpAmount == orderTotalAmount);

                    if (checkAmount) {
                        boolean checkOrderStatus = order.getOrderStatus() == OrderStatus.PENDING;

                        if (checkOrderStatus) {
                            if ("00".equals(request.getParameter("vnp_ResponseCode"))) {
                                order.setOrderStatus(OrderStatus.PAID);
                                orderRepository.save(order);

                                // Gửi email xác nhận sau khi VNPay thanh toán thành công
                                new Thread(() -> {
                                    try {
                                        emailService.sendOrderConfirmationEmail(order.getOrderId());
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                }).start();

                                response.put("code", "00");
                                response.put("message", "Giao dịch thành công");
                            } else {
                                response.put("code", "01");
                                response.put("message", "Giao dịch thất bại hoặc bị hủy");
                            }
                        } else {
                            response.put("code", "02");
                            response.put("message", "Đơn hàng đã được xác nhận trước đó");
                        }
                    } else {
                        response.put("code", "04");
                        response.put("message", "Số tiền thanh toán không khớp");
                    }
                } else {
                    response.put("code", "01");
                    response.put("message", "Không tìm thấy đơn hàng");
                }
            } catch (Exception e) {
                response.put("code", "99");
                response.put("message", "Lỗi hệ thống: " + e.getMessage());
            }
        } else {
            response.put("code", "97");
            response.put("message", "Chữ ký không hợp lệ (Dữ liệu bị can thiệp)");
        }
        return ResponseEntity.ok(response);
    }
}
