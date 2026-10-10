package com.victorpena.plaza.repository;

import com.victorpena.plaza.model.*;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoredDocumentRepository extends JpaRepository<StoredDocument, Long> {
  List<StoredDocument> findByLeaseIdOrderByCreatedAtDesc(Long leaseId);
}
