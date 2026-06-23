package com.lvt.tmdt.repository;

import com.lvt.tmdt.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.lvt.tmdt.enums.OrderStatus;
import com.lvt.tmdt.enums.PaymentMethod;
import java.time.LocalDateTime;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    java.util.List<Order> findByUser_UserIdOrderByCreatedAtDesc(Integer userId);
    
    java.util.List<Order> findByOrderStatusAndPaymentMethodAndCreatedAtBefore(
        OrderStatus status, 
        PaymentMethod method, 
        LocalDateTime timeLimit
    );
}
