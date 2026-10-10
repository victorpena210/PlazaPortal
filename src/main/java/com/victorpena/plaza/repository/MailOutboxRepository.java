package com.victorpena.plaza.repository;

import com.victorpena.plaza.model.*;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MailOutboxRepository extends JpaRepository<MailOutbox, Long> {
  List<MailOutbox> findTop20BySentAtIsNullAndNextAttemptAtBeforeOrderById(
      java.time.LocalDateTime time);

  long countBySentAtIsNull();
}
