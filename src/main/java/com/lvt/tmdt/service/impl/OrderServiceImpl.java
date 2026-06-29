package com.lvt.tmdt.service.impl;

import com.lvt.tmdt.dto.request.OrderRequest;
import com.lvt.tmdt.dto.response.OrderResponse;
import com.lvt.tmdt.entity.*;
import com.lvt.tmdt.enums.CommissionStatus;
import com.lvt.tmdt.enums.PaymentMethod;
import com.lvt.tmdt.enums.ShopOrderStatus;
import com.lvt.tmdt.dto.response.ShopOrderResponse;
import com.lvt.tmdt.mapper.OrderMapper;
import com.lvt.tmdt.repository.CartItemRepository;
import com.lvt.tmdt.repository.CategoryRepository;
import com.lvt.tmdt.repository.CommissionConfigRepository;
import com.lvt.tmdt.repository.NotificationRepository;
import com.lvt.tmdt.repository.OrderRepository;
import com.lvt.tmdt.repository.OrderStatusHistoryRepository;
import com.lvt.tmdt.repository.ProductRepository;
import com.lvt.tmdt.repository.ProductVariantRepository;
import com.lvt.tmdt.repository.ShopOrderRepository;
import com.lvt.tmdt.repository.ShopRepository;
import com.lvt.tmdt.repository.UserRepository;
import com.lvt.tmdt.service.intf.OrderService;
import com.lvt.tmdt.service.intf.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private CommissionConfigRepository commissionConfigRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private ShopOrderRepository shopOrderRepository;

    @Autowired
    private OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Override
    @Transactional
    public OrderResponse placeOrder(Integer userId, OrderRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

        List<CartItem> cartItems = cartItemRepository.findAllById(request.getCartItemIds());
        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("Giỏ hàng trống hoặc không hợp lệ");
        }

        Map<Shop, List<CartItem>> itemsByShop = new HashMap<>();
        Map<Shop, BigDecimal> shopSubtotals = new HashMap<>();
        Map<Shop, BigDecimal> shopCommissions = new HashMap<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem item : cartItems) {
            Shop shop = item.getProduct().getShop();
            if (!itemsByShop.containsKey(shop)) {
                itemsByShop.put(shop, new ArrayList<>());
                shopSubtotals.put(shop, BigDecimal.ZERO);
                shopCommissions.put(shop, BigDecimal.ZERO);
            }
            itemsByShop.get(shop).add(item);

            BigDecimal price;
            if (item.getProductVariant() != null) {
                price = item.getProductVariant().getPrice();
            } else {
                price = item.getProduct().getPrice();
            }
            BigDecimal itemSubtotal = price.multiply(BigDecimal.valueOf(item.getQuantity()));

            BigDecimal currentShopTotal = shopSubtotals.get(shop);
            shopSubtotals.put(shop, currentShopTotal.add(itemSubtotal));
            totalAmount = totalAmount.add(itemSubtotal);

            Category itemCategory = item.getProduct().getCategory();
            Category rootCategory = itemCategory;

            // Tìm thể loại cha gốc
            while (rootCategory.getParentId() != null) {
                Category parentCat = categoryRepository.findById(rootCategory.getParentId()).orElse(null);
                if (parentCat != null) {
                    rootCategory = parentCat;
                } else {
                    break;
                }
            }

            // Lấy hoa hồng của thể loại cha gốc
            CommissionConfig config = commissionConfigRepository
                    .findLatestByCategoryAndStatus(rootCategory.getCategoryId(), CommissionStatus.ACTIVE)
                    .orElse(null);
            
            BigDecimal commRate = config != null ? config.getCommissionRate() : BigDecimal.valueOf(0.05); // Default 5%

            BigDecimal itemCommission = itemSubtotal.multiply(commRate);

            BigDecimal currentShopCommission = shopCommissions.get(shop);
            shopCommissions.put(shop, currentShopCommission.add(itemCommission));
        }


        Order order = orderMapper.mapToEntity(request, user, totalAmount);
        for (Shop shop : itemsByShop.keySet()) {
            List<CartItem> shopCartItems = itemsByShop.get(shop);

            BigDecimal shopSubtotal = shopSubtotals.get(shop);
            BigDecimal commission = shopCommissions.get(shop);
            BigDecimal sellerAmount = shopSubtotal.subtract(commission);

            ShopOrder shopOrder = orderMapper.mapToShopOrderEntity(order, shop, shopSubtotal, commission, sellerAmount);

            for (CartItem cartItem : shopCartItems) {
                Product product = cartItem.getProduct();
                ProductVariant variant = cartItem.getProductVariant();
                int quantity = cartItem.getQuantity();

                BigDecimal itemPrice;
                int availableStock;
                if (variant != null) {
                    itemPrice = variant.getPrice();
                    availableStock = variant.getStockQuantity();
                } else {
                    itemPrice = product.getPrice();
                    availableStock = product.getStockQuantity();
                }
                if (availableStock < quantity) {
                    throw new IllegalArgumentException(
                            "Sản phẩm " + product.getProductName() + " không đủ số lượng trong kho");
                }
                // Cập nhật stock
                if (variant != null) {
                    variant.setStockQuantity(availableStock - quantity);
                    productVariantRepository.save(variant);
                } else {
                    product.setStockQuantity(availableStock - quantity);
                    productRepository.save(product);
                }
                BigDecimal itemSubtotal = itemPrice.multiply(BigDecimal.valueOf(quantity));
                OrderItem orderItem = orderMapper.mapToOrderItemEntity(
                        shopOrder,
                        product,
                        variant,
                        quantity,
                        itemPrice,
                        itemSubtotal);
                shopOrder.getOrderItems().add(orderItem);
            }

            order.getShopOrders().add(shopOrder);
        }
        Order savedOrder = orderRepository.save(order);


        if (savedOrder.getPaymentMethod() == PaymentMethod.COD) {
            for (ShopOrder shopOrder : savedOrder.getShopOrders()) {
                StringBuilder contentBuilder = new StringBuilder();
                contentBuilder.append("Khách hàng ").append(user.getFullName()).append(" vừa đặt mua:\n");
                
                for (OrderItem item : shopOrder.getOrderItems()) {
                    contentBuilder.append("- ").append(item.getProduct().getProductName());
                    
                    if (item.getVariant() != null) {
                        List<String> attributes = new ArrayList<>();
                        for (VariantAttribute attr : item.getVariant().getVariantAttributes()) {
                            attributes.add(attr.getCategoryAttribute().getAttrName() + ": " + attr.getValueString());
                        }
                        contentBuilder.append(" (").append(String.join(", ", attributes)).append(")");
                    }
                    
                    contentBuilder.append(" x").append(item.getQuantity()).append("\n");
                }

                Notification notification = Notification.builder()
                        .user(shopOrder.getShop().getUser())
                        .title("Đơn hàng mới")
                        .content(contentBuilder.toString().trim())
                        .isRead(false)
                        .build();
                notificationRepository.save(notification);
            }
        }

        cartItemRepository.deleteAll(cartItems);


        if (savedOrder.getPaymentMethod() == PaymentMethod.COD) {
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    emailService.sendOrderConfirmationEmail(savedOrder.getOrderId());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }).start();
        }

        return orderMapper.mapToResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders(Integer userId) {
        return orderRepository.findByUser_UserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(orderMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ShopOrderResponse> getSellerOrders(Integer userId, String keyword, List<ShopOrderStatus> statuses, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));
        Shop shop = shopRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Bạn chưa có gian hàng"));
                
        Page<ShopOrder> page = shopOrderRepository.findSellerOrdersFiltered(
                shop.getShopId(),
                keyword,
                statuses != null && statuses.isEmpty() ? null : statuses,
                startDate,
                endDate,
                pageable
        );
        return page.map(orderMapper::mapShopOrderToResponse);
    }

    @Override
    @Transactional
    public ShopOrderResponse updateShopOrderStatus(Integer userId, Long shopOrderId, ShopOrderStatus status, String cancelReason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));
        Shop shop = shopRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Bạn chưa có gian hàng"));
                
        ShopOrder shopOrder = shopOrderRepository.findById(shopOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng của shop"));
                
        if (!shopOrder.getShop().getShopId().equals(shop.getShopId())) {
            throw new IllegalArgumentException("Đơn hàng không thuộc về gian hàng của bạn");
        }
        
        String oldStatusStr = shopOrder.getStatus().name();
        String newStatusStr = status.name();

        shopOrder.setStatus(status);
        if (status == ShopOrderStatus.CANCELLED && cancelReason != null) {
            shopOrder.setCancelReason(cancelReason);
        }
        ShopOrder saved = shopOrderRepository.save(shopOrder);
        
        OrderStatusHistory history = OrderStatusHistory.builder()
                .shopOrder(saved)
                .oldStatus(oldStatusStr)
                .newStatus(newStatusStr)
                .updatedBy(user)
                .build();
        orderStatusHistoryRepository.save(history);
        
        return orderMapper.mapShopOrderToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getOrderCountsByStatus(Integer userId) {
        Shop shop = shopRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng chưa có gian hàng"));
        
        List<Object[]> results = shopOrderRepository.countOrdersByStatus(shop.getShopId());
        Map<String, Long> counts = new HashMap<>();

        for (ShopOrderStatus status : ShopOrderStatus.values()) {
            if (status != ShopOrderStatus.UNPAID) {
                counts.put(status.name(), 0L);
            }
        }
        
        for (Object[] row : results) {
            ShopOrderStatus status = (ShopOrderStatus) row[0];
            Long count = (Long) row[1];
            counts.put(status.name(), count);
        }
        
        return counts;
    }
}
