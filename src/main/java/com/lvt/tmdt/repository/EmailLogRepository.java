package com.lvt.tmdt.repository;

import com.lvt.tmdt.entity.EmailLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailLogRepository extends JpaRepository<EmailLog, Long> {

    @Query("SELECT e FROM EmailLog e WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR e.recipientEmail LIKE %:keyword% OR e.subject LIKE %:keyword%) " +
           "AND (:status IS NULL OR :status = '' OR e.sendStatus = :status)")
    Page<EmailLog> searchEmailLogs(@Param("keyword") String keyword, @Param("status") String status, Pageable pageable);
}
