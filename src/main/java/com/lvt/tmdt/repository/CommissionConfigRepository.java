package com.lvt.tmdt.repository;

import com.lvt.tmdt.entity.CommissionConfig;
import com.lvt.tmdt.enums.CommissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CommissionConfigRepository extends JpaRepository<CommissionConfig, Integer> {
    
    @Query("SELECT c FROM CommissionConfig c WHERE c.category.categoryId = :categoryId AND c.status = :status ORDER BY c.createdAt DESC LIMIT 1")
    Optional<CommissionConfig> findLatestByCategoryAndStatus(@Param("categoryId") Short categoryId, @Param("status") CommissionStatus status);

}
