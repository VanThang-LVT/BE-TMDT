package com.lvt.tmdt.repository;

import com.lvt.tmdt.entity.ShopOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import com.lvt.tmdt.enums.ShopOrderStatus;

@Repository
public interface ShopOrderRepository extends JpaRepository<ShopOrder, Long> {
    @Query("SELECT so FROM ShopOrder so WHERE so.shop.shopId = :shopId " +
           "AND so.status != 'UNPAID' " +
           "AND (:statuses IS NULL OR so.status IN :statuses) " +
           "AND (:keyword IS NULL OR :keyword = '' OR LOWER(so.order.receiverName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR so.order.receiverPhone LIKE CONCAT('%', :keyword, '%') OR CAST(so.shopOrderId AS string) LIKE CONCAT('%', :keyword, '%')) " +
           "AND (CAST(:startDate AS timestamp) IS NULL OR so.createdAt >= :startDate) " +
           "AND (CAST(:endDate AS timestamp) IS NULL OR so.createdAt <= :endDate)")
    Page<ShopOrder> findSellerOrdersFiltered(
            @Param("shopId") Integer shopId, 
            @Param("keyword") String keyword, 
            @Param("statuses") List<ShopOrderStatus> statuses, 
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate, 
            Pageable pageable);

    @Query("SELECT so.status, COUNT(so) FROM ShopOrder so WHERE so.shop.shopId = :shopId AND so.status != 'UNPAID' GROUP BY so.status")
    List<Object[]> countOrdersByStatus(@Param("shopId") Integer shopId);
}
