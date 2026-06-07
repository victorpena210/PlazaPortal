package com.victorpena.plaza.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.victorpena.plaza.model.Lease;

public interface LeaseRepository
        extends JpaRepository<Lease, Long> {

    List<Lease> findByActiveTrue();

    List<Lease> findByTenantId(Long tenantId);
    
    Optional<Lease> findByTenantIdAndOfficeIdAndActiveTrue(
            Long tenantId,
            Long officeId);
}