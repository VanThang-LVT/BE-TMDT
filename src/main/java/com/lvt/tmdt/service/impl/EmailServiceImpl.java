package com.lvt.tmdt.service.impl;

import com.lvt.tmdt.entity.EmailLog;
import com.lvt.tmdt.entity.User;
import com.lvt.tmdt.mapper.EmailLogMapper;
import com.lvt.tmdt.repository.EmailLogRepository;
import com.lvt.tmdt.repository.UserRepository;
import com.lvt.tmdt.service.intf.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.lvt.tmdt.entity.Order;
import com.lvt.tmdt.entity.ShopOrder;
import com.lvt.tmdt.entity.OrderItem;
import com.lvt.tmdt.entity.VariantAttribute;
import com.lvt.tmdt.dto.response.EmailLogResponse;
import com.lvt.tmdt.repository.OrderRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private EmailLogRepository emailLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailLogMapper emailLogMapper;

    @Override
    public void sendEmail(String to, String subject, String content) {
        User user = userRepository.findByEmail(to).orElse(null);

        EmailLog emailLog = emailLogMapper.toEntity(user, to, subject, content);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@eoviti.com");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);
            
            emailLog.setSendStatus("SUCCESS");
        } catch (Exception e) {
            emailLog.setSendStatus("FAILED");
            throw e;
        } finally {
            emailLogRepository.save(emailLog);
        }
    }

    @Override
    public Page<EmailLogResponse> getAllEmailLogs(String keyword, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("sentAt").descending());
        return emailLogRepository.searchEmailLogs(keyword, status, pageable)
                .map(emailLogMapper::mapToResponse);
    }

    @Override
    @Transactional
    public void sendOrderConfirmationEmail(Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) return;
        
        User user = order.getUser();
        String to = user.getEmail();
        String subject = "Xác nhận đặt hàng thành công #" + order.getOrderId();
        
        StringBuilder content = new StringBuilder();
        content.append("Chào ").append(user.getFullName()).append(",\n\n");
        content.append("Cảm ơn bạn đã mua sắm tại EoViTi.\n");
        content.append("Đơn hàng #").append(order.getOrderId())
               .append(" của bạn đã được hệ thống ghi nhận thành công.\n\n");
               
        content.append("CHI TIẾT ĐƠN HÀNG:\n");
        content.append("--------------------------------------------------\n");
        
        for (ShopOrder shopOrder : order.getShopOrders()) {
            for (OrderItem item : shopOrder.getOrderItems()) {
                content.append("- Tên sản phẩm: ").append(item.getProduct().getProductName()).append("\n");
                
                if (item.getVariant() != null) {
                    content.append("  Phân loại: ");
                    
                    List<String> attributes = new ArrayList<>();
                    for (VariantAttribute attr : item.getVariant().getVariantAttributes()) {
                        String tenThuocTinh = attr.getCategoryAttribute().getAttrName();
                        String giaTri = attr.getValueString();
                        attributes.add(tenThuocTinh + ": " + giaTri);
                    }
                    
                    content.append(String.join(", ", attributes)).append("\n");
                }
                
                content.append("  Số lượng: ").append(item.getQuantity()).append("\n");
                content.append("  Thành tiền: ").append(item.getSubtotal()).append(" VNĐ\n");
                content.append("--------------------------------------------------\n");
            }
        }
        
        content.append("\nTỔNG CỘNG: ").append(order.getTotalAmount()).append(" VNĐ\n");
        content.append("Phương thức thanh toán: ").append(order.getPaymentMethod().name()).append("\n\n");
        content.append("Thông tin giao hàng:\n");
        content.append("Người nhận: ").append(order.getReceiverName()).append("\n");
        content.append("SĐT: ").append(order.getReceiverPhone()).append("\n");
        content.append("Địa chỉ: ").append(order.getShippingAddress()).append("\n\n");
        content.append("Chúng tôi sẽ sớm chuẩn bị hàng và giao đến bạn.\n\n");
        content.append("Trân trọng,\nĐội ngũ EoViTi");

        String finalContent = content.toString();
        EmailLog emailLog = emailLogMapper.toEntity(user, order, to, subject, finalContent);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@eoviti.com");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(finalContent);
            mailSender.send(message);
            
            emailLog.setSendStatus("SUCCESS");
        } catch (Exception e) {
            emailLog.setSendStatus("FAILED");
        } finally {
            emailLogRepository.save(emailLog);
        }
    }
}
