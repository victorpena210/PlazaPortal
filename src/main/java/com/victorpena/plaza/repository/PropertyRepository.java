package com.victorpena.plaza.repository;

import com.victorpena.plaza.model.Property;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropertyRepository extends JpaRepository<Property, Long> {
  List<Property> findByCompanyIdOrderByName(Long companyId);
}
