package com.victorpena.plaza.service;

import com.victorpena.plaza.model.*;
import com.victorpena.plaza.repository.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PropertyService {
  private final PropertyRepository properties;
  private final OfficeRepository offices;
  private final LeaseRepository leases;
  private final AccessService access;
  private final AuditService audit;
  private final java.time.Clock clock;

  public PropertyService(
      PropertyRepository properties,
      OfficeRepository offices,
      LeaseRepository leases,
      AccessService access,
      AuditService audit,
      java.time.Clock clock) {
    this.properties = properties;
    this.offices = offices;
    this.leases = leases;
    this.access = access;
    this.audit = audit;
    this.clock = clock;
  }

  public Property property(Long companyId, Long id) {
    var p = properties.findById(id).orElseThrow(AccessService::missing);
    access.sameCompany(companyId, p.getCompany());
    return p;
  }

  public Office office(Long companyId, Long id) {
    var o = offices.findById(id).orElseThrow(AccessService::missing);
    access.sameCompany(companyId, o.getProperty().getCompany());
    return o;
  }

  @Transactional
  public Property saveProperty(
      Company company, Long id, String name, String address, boolean active) {
    access.member(company.getId());
    var p = id == null ? new Property() : property(company.getId(), id);
    p.setCompany(company);
    p.setName(Checks.text(name, "Property name", 150));
    p.setAddress(Checks.text(address, "Address", 500));
    p.setActive(active);
    properties.save(p);
    audit.record(company, access.user().getEmail(), "PROPERTY_SAVED", "Property " + p.getId());
    return p;
  }

  @Transactional
  public Office saveOffice(
      Long companyId,
      Long id,
      Long propertyId,
      String number,
      Integer size,
      String description,
      OfficeStatus status) {
    var p = property(companyId, propertyId);
    var o = id == null ? new Office() : office(companyId, id);
    if (id != null && !o.getProperty().getId().equals(propertyId))
      throw new IllegalArgumentException("A unit cannot be moved between properties.");
    number = Checks.text(number, "Unit number", 20);
    if (offices.existsByPropertyIdAndOfficeNumberAndIdNot(
        propertyId, number, id == null ? -1L : id))
      throw new IllegalArgumentException("That unit number already exists at this property.");
    if (size != null && size < 1)
      throw new IllegalArgumentException("Square feet must be positive.");
    if (description != null && description.length() > 255)
      throw new IllegalArgumentException("Description is too long.");
    if (status == OfficeStatus.OCCUPIED)
      throw new IllegalArgumentException("Occupancy is calculated from current leases.");
    if (id != null
        && !leases
            .overlapping(id, java.time.LocalDate.now(clock), java.time.LocalDate.now(clock))
            .isEmpty()) status = OfficeStatus.OCCUPIED;
    o.setProperty(p);
    o.setOfficeNumber(number);
    o.setSquareFeet(size);
    o.setDescription(description);
    o.setStatus(status == null ? OfficeStatus.AVAILABLE : status);
    offices.save(o);
    audit.record(p.getCompany(), access.user().getEmail(), "UNIT_SAVED", "Unit " + o.getId());
    return o;
  }
}
