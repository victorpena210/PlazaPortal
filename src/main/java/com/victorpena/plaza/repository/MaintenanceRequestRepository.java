package com.victorpena.plaza.repository;

import com.victorpena.plaza.model.*;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaintenanceRequestRepository extends JpaRepository<MaintenanceRequest, Long> {
  List<MaintenanceRequest> findByOfficePropertyCompanyIdOrderByCreatedAtDesc(Long companyId);

  List<MaintenanceRequest> findByUserIdOrderByCreatedAtDesc(Long userId);

  @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @org.springframework.data.jpa.repository.Query(
      "select r from MaintenanceRequest r where r.id=:id")
  java.util.Optional<MaintenanceRequest> lockById(Long id);
}
