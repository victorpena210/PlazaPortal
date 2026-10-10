package com.victorpena.plaza.controller;

import com.victorpena.plaza.repository.*;
import com.victorpena.plaza.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class PaymentController {
  private final PaymentService payments;
  private final PaymentRepository records;
  private final AccessService access;

  public PaymentController(
      PaymentService payments, PaymentRepository records, AccessService access) {
    this.payments = payments;
    this.records = records;
    this.access = access;
  }

  @PostMapping({"/pay/{id}", "/tenant/invoices/{id}/checkout"})
  public String checkout(@PathVariable Long id) {
    String url = payments.checkout(id);
    if (url == null || !url.startsWith("https://checkout.stripe.com/"))
      throw new IllegalStateException("Unexpected checkout URL.");
    return "redirect:" + url;
  }

  @GetMapping("/receipts/{id}")
  public String receipt(@PathVariable Long id, Model m) {
    var p = records.findById(id).orElseThrow(AccessService::missing);
    if (p.getLease() != null) access.leaseRead(p.getLease());
    else access.member(p.getOffice().getProperty().getCompany().getId());
    if (p.getPaidAt() == null)
      throw new IllegalArgumentException("A receipt is available after payment is received.");
    m.addAttribute("payment", p);
    return "receipt";
  }
}
