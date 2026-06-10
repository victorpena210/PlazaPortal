package com.victorpena.plaza.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.victorpena.plaza.model.Lease;

public interface LeaseRepository extends JpaRepository<Lease, Long> {

    List<Lease> findByActiveTrue();

    List<Lease> findByPortalAccess_Id(Long userId);

    Optional<Lease> findByPortalAccess_IdAndOffice_IdAndActiveTrue(
            Long userId,
            Long officeId);

    @Query("SELECT l FROM Lease l WHERE l.tenantEmail = :tenantEmail")
    Optional<Lease> findByTenantEmail(String tenantEmail);
    
    List<Lease> findByPortalAccess_IdAndActiveTrue(Long userId);
    


}