package com.victorpena.plaza.repository;

import com.victorpena.plaza.model.*;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;

public interface MembershipInvitationRepository extends JpaRepository<MembershipInvitation, Long> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select i from MembershipInvitation i where i.tokenHash=:hash")
  Optional<MembershipInvitation> lockByHash(String hash);

  List<MembershipInvitation> findByCompanyIdAndEmailAndUsedFalse(Long companyId, String email);
}
