package com.victorpena.plaza.repository;

import com.victorpena.plaza.model.*;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;

public interface OfficeRepository extends JpaRepository<Office, Long> {
  List<Office> findByPropertyCompanyIdOrderByPropertyNameAscOfficeNumberAsc(Long companyId);

  boolean existsByPropertyIdAndOfficeNumberAndIdNot(Long propertyId, String officeNumber, Long id);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select o from Office o where o.id=:id")
  Optional<Office> lockById(Long id);
}
