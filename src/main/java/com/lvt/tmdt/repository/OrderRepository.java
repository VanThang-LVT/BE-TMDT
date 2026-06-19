package com.lvt.tmdt.repository;

import com.lvt.tmdt.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    java.util.List<Order> findByUser_UserIdOrderByCreatedAtDesc(Integer userId);
}
