package com.victorpena.plaza.repository;

import com.victorpena.plaza.model.*;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;

public interface TenantInvitationRepository extends JpaRepository<TenantInvitation, Long> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select i from TenantInvitation i where i.token=:token")
  Optional<TenantInvitation> lockByToken(String token);

  List<TenantInvitation> findByLeaseIdAndUsedFalse(Long id);
}
