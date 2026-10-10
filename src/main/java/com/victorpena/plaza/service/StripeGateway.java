package com.victorpena.plaza.service;

import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.param.ChargeRetrieveParams;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.checkout.SessionRetrieveParams;
import com.victorpena.plaza.model.*;
import java.time.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StripeGateway {
  private final StripeClient client;
  private final Map<Long, String> accounts = new HashMap<>();
  private final NotificationService urls;
  private final boolean live;
  private final boolean configured;
  private final String integration;

  public StripeGateway(
      @Value("${stripe.api-key:}") String key,
      @Value("${stripe.webhook-secret:}") String webhookSecret,
      @Value("${stripe.company-accounts:}") String map,
      @Value("${stripe.live-mode:false}") boolean live,
      @Value("${stripe.integration-identifier:plazaportal-rent-qarvnbtk}") String integration,
      NotificationService urls) {
    this.client = key.isBlank() ? null : new StripeClient(key);
    this.configured = !key.isBlank() && !webhookSecret.isBlank();
    this.urls = urls;
    this.live = live;
    this.integration = integration;
    Set<String> used = new HashSet<>();
    for (String pair : map.split(",")) {
      if (pair.isBlank()) continue;
      var parts = pair.trim().split(":", 2);
      if (parts.length != 2
          || !(parts[1].equals("platform") || parts[1].matches("acct_[A-Za-z0-9]+")))
        throw new IllegalArgumentException("Invalid Stripe company account mapping.");
      if (!used.add(parts[1]) || accounts.put(Long.valueOf(parts[0]), parts[1]) != null)
        throw new IllegalArgumentException("Stripe account mappings must be unique.");
    }
    if (client != null && live && !urls.url("").startsWith("https://"))
      throw new IllegalArgumentException("Live payments require an HTTPS public URL.");
  }

  public boolean enabled(Long companyId) {
    return configured && accounts.containsKey(companyId);
  }

  public boolean live() {
    return live;
  }

  public String account(Long companyId) {
    if (!enabled(companyId))
      throw new IllegalArgumentException(
          "Online payments are not configured for this company. Contact your property manager.");
    return accounts.get(companyId);
  }

  private RequestOptions options(String account, String key) {
    var b = RequestOptions.builder();
    if (account != null && !account.equals("platform")) b.setStripeAccount(account);
    if (key != null) b.setIdempotencyKey(key);
    b.setConnectTimeout(10000);
    b.setReadTimeout(20000);
    return b.build();
  }

  public Session create(Payment p) {
    var i = p.getInvoice();
    var c = i.getLease().getOffice().getProperty().getCompany();
    var price =
        SessionCreateParams.LineItem.PriceData.builder()
            .setCurrency("usd")
            .setUnitAmount(p.getTotalAmount().movePointRight(2).longValueExact())
            .setProductData(
                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                    .setName(c.getName() + " · Invoice " + i.getId())
                    .build())
            .build();
    var params =
        SessionCreateParams.builder()
            .setMode(SessionCreateParams.Mode.PAYMENT)
            .setSuccessUrl(urls.url("/tenant/invoices?checkout=returned"))
            .setCancelUrl(urls.url("/tenant/invoices?checkout=canceled"))
            .setCustomerEmail(i.getLease().getTenantEmail())
            .setClientReferenceId(p.getId().toString())
            .setExpiresAt(p.getCheckoutExpiresAt().toEpochSecond(ZoneOffset.UTC))
            .putMetadata("payment_id", p.getId().toString())
            .putMetadata("invoice_id", i.getId().toString())
            .putMetadata("company_id", c.getId().toString())
            .putExtraParam("integration_identifier", integration)
            .addLineItem(
                SessionCreateParams.LineItem.builder().setQuantity(1L).setPriceData(price).build())
            .build();
    try {
      return client
          .v1()
          .checkout()
          .sessions()
          .create(params, options(p.getStripeAccountId(), p.getAttemptKey()));
    } catch (StripeException e) {
      throw new IllegalStateException(
          "Payment provider could not start checkout. Retry the same invoice; the pending attempt"
              + " is retained.",
          e);
    }
  }

  public Session expire(String id, String account) {
    try {
      return client
          .v1()
          .checkout()
          .sessions()
          .expire(
              id,
              com.stripe.param.checkout.SessionExpireParams.builder().build(),
              options(account, "expire-" + id));
    } catch (StripeException e) {
      throw new IllegalStateException(
          "Checkout could not be canceled. Check its payment status before retrying.", e);
    }
  }

  public Session retrieve(String id, String account) {
    try {
      return client
          .v1()
          .checkout()
          .sessions()
          .retrieve(id, SessionRetrieveParams.builder().build(), options(account, null));
    } catch (StripeException e) {
      throw new IllegalStateException("Payment verification is temporarily unavailable.", e);
    }
  }

  public Charge latestCharge(String intentId, String account) {
    if (intentId == null) return null;
    try {
      var intent =
          client
              .v1()
              .paymentIntents()
              .retrieve(
                  intentId,
                  com.stripe.param.PaymentIntentRetrieveParams.builder()
                      .addExpand("latest_charge")
                      .build(),
                  options(account, null));
      return intent.getLatestChargeObject();
    } catch (StripeException e) {
      throw new IllegalStateException("Payment reconciliation is temporarily unavailable.", e);
    }
  }

  public Charge charge(String id, String account) {
    try {
      return client
          .v1()
          .charges()
          .retrieve(id, ChargeRetrieveParams.builder().build(), options(account, null));
    } catch (StripeException e) {
      throw new IllegalStateException("Refund verification is temporarily unavailable.", e);
    }
  }
}
