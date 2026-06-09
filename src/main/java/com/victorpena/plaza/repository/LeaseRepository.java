package com.victorpena.plaza.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.victorpena.plaza.model.Lease;

public interface LeaseRepository extends JpaRepository<Lease, Long> {

    List<Lease> findByActiveTrue();

    List<Lease> findByPortalAccess_Id(Long userId);

    Optional<Lease> findByPortalAccess_IdAndOffice_IdAndActiveTrue(
            Long userId,
            Long officeId);

    Optional<Lease> findByTenantEmail(String tenantEmail);
}