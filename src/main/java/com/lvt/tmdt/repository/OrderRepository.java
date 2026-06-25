package com.lvt.tmdt.repository;

import com.lvt.tmdt.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.lvt.tmdt.enums.OrderStatus;
import com.lvt.tmdt.enums.PaymentMethod;
import java.time.LocalDateTime;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser_UserIdOrderByCreatedAtDesc(Integer userId);
    
    List<Order> findByOrderStatusAndPaymentMethodAndCreatedAtBefore(
        OrderStatus status, 
        PaymentMethod method, 
        LocalDateTime timeLimit
    );

    @Query("SELECT COALESCE(SUM(oi.quantity), 0) " +
            "FROM OrderItem oi JOIN oi.shopOrder so JOIN so.order o " +
            "WHERE oi.product.productId = :productId AND o.orderStatus <> 'CANCELLED'")
    Integer getTotalSalesByProductId(@Param("productId") Integer productId);
}
