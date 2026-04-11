package com.victorpena.plaza.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.victorpena.plaza.model.Office;
import com.victorpena.plaza.model.OfficeStatus;

public interface OfficeRepository extends JpaRepository<Office, Long> {

    List<Office> findByStatus(OfficeStatus status);

    Optional<Office> findByOfficeNumber(String officeNumber);
}