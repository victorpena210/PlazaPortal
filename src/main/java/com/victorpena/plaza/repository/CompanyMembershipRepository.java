package com.victorpena.plaza.repository;

import com.victorpena.plaza.model.*;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyMembershipRepository extends JpaRepository<CompanyMembership, Long> {
  Optional<CompanyMembership> findByCompanyIdAndUserId(Long companyId, Long userId);

  List<CompanyMembership> findByUserIdOrderById(Long userId);

  List<CompanyMembership> findByCompanyIdOrderById(Long companyId);

  long countByCompanyIdAndRole(Long companyId, MembershipRole role);
}
