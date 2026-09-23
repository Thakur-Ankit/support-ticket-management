package com.supporttickets.persistence;

import com.supporttickets.domain.TicketStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TicketRepository extends JpaRepository<TicketEntity, UUID> {

  List<TicketEntity> findAllByOrderByCreatedAtDesc();

  @Query("""
      SELECT t FROM TicketEntity t
      WHERE (:status IS NULL OR t.status = :status)
        AND (
          :keyword IS NULL
          OR LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
          OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
      ORDER BY t.createdAt DESC
      """)
  List<TicketEntity> search(@Param("keyword") String keyword, @Param("status") TicketStatus status);
}
