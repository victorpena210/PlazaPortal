package com.victorpena.plaza.service;

import com.victorpena.plaza.model.*;
import com.victorpena.plaza.repository.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentService {
  private final Path root;
  private final StoredDocumentRepository documents;
  private final LeaseService leases;
  private final AccessService access;
  private final AuditService audit;

  public DocumentService(
      @Value("${app.upload-dir:./data/uploads}") String root,
      StoredDocumentRepository documents,
      LeaseService leases,
      AccessService access,
      AuditService audit) {
    this.root = Path.of(root).toAbsolutePath().normalize();
    this.documents = documents;
    this.leases = leases;
    this.access = access;
    this.audit = audit;
  }

  @Transactional
  public void upload(Long companyId, Long leaseId, MultipartFile file) throws IOException {
    var lease = leases.managed(companyId, leaseId);
    if (file.isEmpty() || file.getSize() > 10 * 1024 * 1024)
      throw new IllegalArgumentException("Choose a PDF up to 10 MB.");
    byte[] bytes = file.getBytes();
    if (bytes.length < 5
        || !new String(bytes, 0, 5, java.nio.charset.StandardCharsets.US_ASCII).equals("%PDF-"))
      throw new IllegalArgumentException("Only PDF lease documents are accepted.");
    var d = new StoredDocument();
    d.setLease(lease);
    d.setCompany(lease.getOffice().getProperty().getCompany());
    String original =
        Optional.ofNullable(file.getOriginalFilename())
            .orElse("lease.pdf")
            .replaceAll("[^A-Za-z0-9._ -]", "_");
    d.setOriginalName(original.substring(0, Math.min(original.length(), 255)));
    d.setStorageKey(UUID.randomUUID() + ".pdf");
    d.setContentType("application/pdf");
    d.setSizeBytes(bytes.length);
    Files.createDirectories(root);
    Path target = root.resolve(d.getStorageKey());
    Files.write(target, bytes, StandardOpenOption.CREATE_NEW);
    org.springframework.transaction.support.TransactionSynchronizationManager
        .registerSynchronization(
            new org.springframework.transaction.support.TransactionSynchronization() {
              @Override
              public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED)
                  try {
                    Files.deleteIfExists(target);
                  } catch (IOException ignored) {
                  }
              }
            });
    documents.save(d);
    audit.record(
        d.getCompany(), access.user().getEmail(), "LEASE_DOCUMENT_UPLOADED", "Lease " + leaseId);
  }

  public StoredDocument readable(Long id) {
    var d = documents.findById(id).orElseThrow(AccessService::missing);
    access.leaseRead(d.getLease());
    return d;
  }

  public Path path(StoredDocument d) {
    var p = root.resolve(d.getStorageKey()).normalize();
    if (!p.startsWith(root)) throw AccessService.missing();
    return p;
  }
}
