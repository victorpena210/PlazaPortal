package com.victorpena.plaza.repository;

import com.victorpena.plaza.model.*;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.*;
import org.springframework.data.jpa.repository.*;

public interface LeaseRepository extends JpaRepository<Lease, Long> {
  List<Lease> findByOfficePropertyCompanyIdOrderByStartDateDesc(Long companyId);

  List<Lease> findByPortalAccessIdOrderByStartDateDesc(Long userId);

  List<Lease> findByActiveTrue();

  @Query(
      "select l from Lease l where l.office.id=:officeId and l.active=true and l.startDate<=:end"
          + " and (l.endDate is null or l.endDate>=:start)")
  List<Lease> overlapping(Long officeId, LocalDate start, LocalDate end);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select l from Lease l where l.id=:id")
  Optional<Lease> lockById(Long id);
}
