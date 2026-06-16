package com.lvt.tmdt.repository;

import com.lvt.tmdt.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCartCartId(Long cartId);
    Optional<CartItem> findByCartCartIdAndProductProductIdAndProductVariantVariantId(Long cartId, Integer productId, Long variantId);
    Optional<CartItem> findByCartCartIdAndProductProductIdAndProductVariantIsNull(Long cartId, Integer productId);
}
