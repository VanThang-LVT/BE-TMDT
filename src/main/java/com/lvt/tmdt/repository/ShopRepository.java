package com.lvt.tmdt.repository;

import com.lvt.tmdt.entity.Shop;
import com.lvt.tmdt.entity.User;
import com.lvt.tmdt.enums.ShopStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Integer> {
    List<Shop> findByStatus(ShopStatus status);
    Optional<Shop> findByUser(User user);
    boolean existsByUser(User user);
}
