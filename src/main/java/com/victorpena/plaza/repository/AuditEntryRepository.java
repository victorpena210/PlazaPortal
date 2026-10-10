package com.victorpena.plaza.repository;

import com.victorpena.plaza.model.*;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditEntryRepository extends JpaRepository<AuditEntry, Long> {
  List<AuditEntry> findTop100ByCompanyIdOrderByCreatedAtDesc(Long companyId);
}
