package com.lvt.tmdt.repository;

import com.lvt.tmdt.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    List<Product> findByShop_ShopId(Integer shopId);
}
