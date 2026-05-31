package com.victorpena.plaza.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.victorpena.plaza.model.Invoice;
import com.victorpena.plaza.model.InvoiceStatus;
import com.victorpena.plaza.model.Lease;
import com.victorpena.plaza.model.User;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    boolean existsByLeaseAndDueDate(
            Lease lease,
            LocalDate dueDate);

    List<Invoice> findByStatus(
            InvoiceStatus status);
    
    List<Invoice> findByLeaseTenant(User tenant);
}
