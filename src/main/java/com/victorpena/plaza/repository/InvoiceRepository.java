package com.victorpena.plaza.repository;

import com.victorpena.plaza.model.*;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.*;
import org.springframework.data.jpa.repository.*;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
  List<Invoice> findByLeaseOfficePropertyCompanyIdOrderByDueDateDesc(Long companyId);

  List<Invoice> findByLeasePortalAccessIdOrderByDueDateDesc(Long userId);

  List<Invoice> findByLeaseIdOrderByDueDate(Long leaseId);

  boolean existsByLeaseIdAndBillingPeriodAndKind(Long leaseId, LocalDate period, String kind);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select i from Invoice i where i.id=:id")
  Optional<Invoice> lockById(Long id);
}
