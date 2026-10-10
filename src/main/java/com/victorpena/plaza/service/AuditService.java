package com.victorpena.plaza.service;

import com.victorpena.plaza.model.*;
import com.victorpena.plaza.repository.*;
import org.springframework.stereotype.Service;

@Service
public class AuditService {
  private final AuditEntryRepository entries;

  public AuditService(AuditEntryRepository entries) {
    this.entries = entries;
  }

  public void record(Company company, String actor, String action, String details) {
    var e = new AuditEntry();
    e.setCompany(company);
    e.setActor(actor);
    e.setAction(action);
    e.setDetails(details);
    entries.save(e);
  }
}
