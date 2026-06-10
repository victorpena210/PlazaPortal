package com.victorpena.plaza.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.victorpena.plaza.model.TenantInvitation;

@Repository
public interface TenantInvitationRepository extends JpaRepository<TenantInvitation, Long> {

	Optional<TenantInvitation> findByToken(String token);
	
	Optional<TenantInvitation>
	findByLeaseIdAndUsedFalse(Long leaseId);
}
