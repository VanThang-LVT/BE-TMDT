package com.lvt.tmdt.repository;

import com.lvt.tmdt.entity.ProductApprovalHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductApprovalHistoryRepository extends JpaRepository<ProductApprovalHistory, Integer> {

}
