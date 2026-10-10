package com.victorpena.plaza.repository;

import com.victorpena.plaza.model.*;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
  @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @org.springframework.data.jpa.repository.Query(
      "select t from PasswordResetToken t where t.tokenHash=:hash")
  Optional<PasswordResetToken> lockByHash(String hash);

  List<PasswordResetToken> findByUserIdAndUsedFalse(Long userId);
}
