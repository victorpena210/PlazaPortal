package com.victorpena.plaza.repository;

import java.time.LocalDate;
import java.util.List;

import com.victorpena.plaza.model.Invoice;
import com.victorpena.plaza.model.InvoiceStatus;
import com.victorpena.plaza.model.Lease;

public class InvoiceRepository {

	public boolean existsByLeaseAndDueDate(Lease lease, LocalDate nextMonth) {
		// TODO Auto-generated method stub
		return false;
	}

	public void save(Invoice invoice) {
		// TODO Auto-generated method stub
		
	}

	public List<Invoice> findByStatus(InvoiceStatus pending) {
		// TODO Auto-generated method stub
		return null;
	}

}
