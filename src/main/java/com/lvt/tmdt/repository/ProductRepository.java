package com.lvt.tmdt.repository;

import com.lvt.tmdt.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    List<Product> findByShop_ShopId(Integer shopId);
    
    @Query("SELECT p FROM Product p WHERE p.shop.shopId = :shopId AND (:keyword IS NULL OR LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.brand) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Product> searchBySeller(@Param("shopId") Integer shopId, @Param("keyword") String keyword);

    @Query("SELECT p FROM Product p WHERE :keyword IS NULL OR LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.shop.shopName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> searchForAdmin(@Param("keyword") String keyword);
}
