package com.lvt.tmdt.mapper;

import com.lvt.tmdt.dto.response.OrderItemResponse;
import com.lvt.tmdt.dto.response.OrderResponse;
import com.lvt.tmdt.dto.response.ShopOrderResponse;
import com.lvt.tmdt.entity.Order;
import com.lvt.tmdt.entity.OrderItem;
import com.lvt.tmdt.entity.ShopOrder;
import com.lvt.tmdt.entity.VariantAttribute;
import com.lvt.tmdt.dto.request.OrderRequest;
import com.lvt.tmdt.entity.User;
import com.lvt.tmdt.enums.OrderStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    public Order mapToEntity(OrderRequest request, User user, BigDecimal totalAmount) {
        if (request == null || user == null) return null;

        return Order.builder()
                .user(user)
                .receiverName(request.getReceiverName())
                .receiverPhone(request.getReceiverPhone())
                .shippingAddress(request.getShippingAddress())
                .paymentMethod(request.getPaymentMethod())
                .orderStatus(OrderStatus.PENDING)
                .voucherId(request.getVoucherId())
                .totalAmount(totalAmount != null ? totalAmount : BigDecimal.ZERO)
                .shopOrders(new ArrayList<>())
                .build();
    }

    public ShopOrder mapToShopOrderEntity(Order order, com.lvt.tmdt.entity.Shop shop, BigDecimal subtotalAmount, BigDecimal commissionAmount, BigDecimal sellerAmount) {
        return ShopOrder.builder()
                .order(order)
                .shop(shop)
                .status(com.lvt.tmdt.enums.ShopOrderStatus.PENDING)
                .subtotalAmount(subtotalAmount)
                .commissionAmount(commissionAmount)
                .sellerAmount(sellerAmount)
                .orderItems(new ArrayList<>())
                .build();
    }

    public OrderItem mapToOrderItemEntity(ShopOrder shopOrder, com.lvt.tmdt.entity.Product product, com.lvt.tmdt.entity.ProductVariant variant, int quantity, BigDecimal price, BigDecimal subtotal) {
        return OrderItem.builder()
                .shopOrder(shopOrder)
                .product(product)
                .variant(variant)
                .quantity(quantity)
                .price(price)
                .subtotal(subtotal)
                .build();
    }

    public OrderResponse mapToResponse(Order order) {
        if (order == null) return null;

        List<ShopOrderResponse> shopOrderResponses = null;
        if (order.getShopOrders() != null) {
            shopOrderResponses = new ArrayList<>();
            for (ShopOrder so : order.getShopOrders()) {
                shopOrderResponses.add(mapShopOrderToResponse(so));
            }
        }

        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .userId(order.getUser().getUserId())
                .voucherId(order.getVoucherId())
                .receiverName(order.getReceiverName())
                .receiverPhone(order.getReceiverPhone())
                .shippingAddress(order.getShippingAddress())
                .totalAmount(order.getTotalAmount())
                .paymentMethod(order.getPaymentMethod())
                .orderStatus(order.getOrderStatus())
                .createdAt(order.getCreatedAt())
                .shopOrders(shopOrderResponses)
                .build();
    }

    public ShopOrderResponse mapShopOrderToResponse(ShopOrder shopOrder) {
        if (shopOrder == null)
            return null;

        List<OrderItemResponse> itemResponses = null;
        if (shopOrder.getOrderItems() != null) {
            itemResponses = new ArrayList<>();
            for (OrderItem item : shopOrder.getOrderItems()) {
                itemResponses.add(mapOrderItemToResponse(item));
            }
        }

        return ShopOrderResponse.builder()
                .shopOrderId(shopOrder.getShopOrderId())
                .shopId(shopOrder.getShop().getShopId())
                .shopName(shopOrder.getShop().getShopName())
                .subtotalAmount(shopOrder.getSubtotalAmount())
                .commissionAmount(shopOrder.getCommissionAmount())
                .sellerAmount(shopOrder.getSellerAmount())
                .status(shopOrder.getStatus())
                .createdAt(shopOrder.getCreatedAt())
                .orderItems(itemResponses)
                .build();
    }

    public OrderItemResponse mapOrderItemToResponse(OrderItem item) {
        if (item == null)
            return null;

        String imageUrl = null;
        if (item.getVariant() != null && item.getVariant().getImageUrl() != null && !item.getVariant().getImageUrl().isEmpty()) {
            imageUrl = item.getVariant().getImageUrl();
        } else if (item.getProduct().getImages() != null && !item.getProduct().getImages().isEmpty()) {
            imageUrl = "/api/images/products/" + item.getProduct().getImages().get(0).getImageId();
        }

        String variantAttributes = null;
        if (item.getVariant() != null && item.getVariant().getVariantAttributes() != null) {
            StringBuilder sb = new StringBuilder();
            List<VariantAttribute> attrs = item.getVariant().getVariantAttributes();
            for (int i = 0; i < attrs.size(); i++) {
                sb.append(attrs.get(i).getValueString());
                if (i < attrs.size() - 1) {
                    sb.append(", ");
                }
            }
            variantAttributes = sb.toString();
        }
        return OrderItemResponse.builder()
                .orderItemId(item.getOrderItemId())
                .productId(item.getProduct().getProductId())
                .productName(item.getProduct().getProductName())
                .variantId(item.getVariant() != null ? item.getVariant().getVariantId() : null)
                .variantAttributes(variantAttributes)
                .imageUrl(imageUrl)
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .subtotal(item.getSubtotal())
                .build();
    }
}
