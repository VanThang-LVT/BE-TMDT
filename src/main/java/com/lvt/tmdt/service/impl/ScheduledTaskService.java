package com.lvt.tmdt.service.impl;

import com.lvt.tmdt.entity.Order;
import com.lvt.tmdt.entity.ShopOrder;
import com.lvt.tmdt.entity.OrderItem;
import com.lvt.tmdt.entity.Product;
import com.lvt.tmdt.entity.ProductVariant;
import com.lvt.tmdt.enums.OrderStatus;
import com.lvt.tmdt.enums.PaymentMethod;
import com.lvt.tmdt.enums.ShopOrderStatus;
import com.lvt.tmdt.repository.OrderRepository;
import com.lvt.tmdt.repository.ProductRepository;
import com.lvt.tmdt.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledTaskService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cancelUnpaidOrders() {
        LocalDateTime timeLimit = LocalDateTime.now().minusMinutes(15);

        List<Order> pendingOrders = orderRepository.findByOrderStatusAndPaymentMethodAndCreatedAtBefore(
                OrderStatus.PENDING,
                PaymentMethod.VNPAY,
                timeLimit
        );

        for (Order order : pendingOrders) {
            log.info("Hủy đơn hàng quá hạn thanh toán VNPAY: {}", order.getOrderId());
            order.setOrderStatus(OrderStatus.CANCELLED);

            for (ShopOrder shopOrder : order.getShopOrders()) {
                shopOrder.setStatus(ShopOrderStatus.CANCELLED);

                for (OrderItem item : shopOrder.getOrderItems()) {
                    Product product = item.getProduct();
                    ProductVariant variant = item.getVariant();
                    int quantity = item.getQuantity();

                    if (variant != null) {
                        variant.setStockQuantity(variant.getStockQuantity() + quantity);
                        productVariantRepository.save(variant);
                    } else if (product != null) {
                        product.setStockQuantity(product.getStockQuantity() + quantity);
                        productRepository.save(product);
                    }
                }
            }
            orderRepository.save(order);
        }
    }
}
