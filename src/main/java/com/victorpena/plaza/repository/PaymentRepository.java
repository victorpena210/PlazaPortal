package com.victorpena.plaza.repository;

import com.victorpena.plaza.model.*;
import java.util.*;
import org.springframework.data.jpa.repository.*;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
  List<Payment> findByInvoiceIdOrderById(Long invoiceId);

  List<Payment> findByOfficePropertyCompanyIdOrderByCreatedAtDesc(Long companyId);

  @Query(
      "select p from Payment p left join p.lease l left join l.portalAccess t left join p.user u"
          + " where t.id=:userId or u.id=:userId order by p.createdAt desc")
  List<Payment> findByUserIdOrderByCreatedAtDesc(Long userId);

  Optional<Payment> findByStripeSessionId(String sessionId);

  Optional<Payment> findByStripePaymentIntentId(String intentId);

  Optional<Payment> findByAttemptKey(String key);

  List<Payment> findByStatusIn(Collection<PaymentStatus> statuses);
}
