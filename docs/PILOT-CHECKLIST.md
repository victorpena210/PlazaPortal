# PlazaPortal pilot acceptance

Use a restored test database, fake tenant details and Stripe sandbox payments first.
Do not use real card numbers, real rent transfers or real tenant addresses for testing.

## Suggested test setup

- Company A: Peña Plaza Demo; parent A is OWNER, parent B is OWNER or MANAGER.
- Company B: Independent Owner Demo, using a different owner account.
- Each company has a building and a Unit 101 (the repeated number must be accepted).
- Each company has a separate tenant email you control. Use distinct browser profiles.
- Monthly rent $1,000; current-month start; a twelve-month end date; no automatic late fee.

## Owner and access checks

- [ ] Sign in as parent A; create the building and unit.
- [ ] Invite parent B, accept, and verify both see Company A.
- [ ] Create Company B and confirm its lists never include Company A's records.
- [ ] Attempt another company's lease, document, maintenance and invoice URLs.
      They must be denied or reported missing.
- [ ] As a manager, confirm Company/team controls are unavailable.
- [ ] Confirm the last owner cannot be removed.
- [ ] Confirm sign-out and password reset work; a reset link cannot be reused.

## Leases and invoices

- [ ] Create a lease and accept the email invitation as a new tenant.
- [ ] Add a second lease for the same tenant and accept while signed in.
- [ ] Reject a same-unit overlapping lease and a negative rent amount.
- [ ] Enter a historical lease with billing starting this month; no earlier invoices
      should be invented. Record any verified opening balance as a labeled charge.
- [ ] Test a lease starting in a later month; no rent may be billed before that month.
- [ ] With $3,100 rent and an October 16 start, first-month proration should be $1,600.
- [ ] Run invoice generation twice; each lease/month must still have one rent invoice.
- [ ] Add a clearly labeled one-off charge and check the tenant's balance.
- [ ] Record a $250 check against a $1,000 invoice; remaining balance must be $750.
- [ ] Retry that form submission; it must not record a second $250 payment.
- [ ] Renew the lease and end another early; retain all history and review final rent.
- [ ] Upload a lease PDF and confirm only its tenant and company team can download it.

## Stripe sandbox

- [ ] Configure a separate sandbox, explicit company account mappings and a webhook.
- [ ] Confirm a company without a mapping cannot start an online payment.
- [ ] Pay an invoice with Stripe's documented sandbox test method.
- [ ] Close the browser before returning; the webhook must still update the invoice.
- [ ] Send the same signed event twice; only one payment and receipt should exist.
- [ ] Test a delayed method: invoice stays unpaid while the payment is processing.
- [ ] Test failure, session expiry, cancellation, and a interrupted/retried request.
- [ ] Refund a payment in the sandbox; verify the refunded amount and new balance.
- [ ] Verify the payment landed in the intended company's account, not another owner's.
- [ ] Verify the receipt, amount, currency, reference and email notification.

## Maintenance and mobile

- [ ] Tenant submits a request for a current lease and adds a PNG/JPEG photo.
- [ ] Another tenant cannot view the photo or update that request.
- [ ] Manager sees the request, changes status, and adds an update.
- [ ] Tenant receives email and sees that update on the dashboard.
- [ ] Complete and reopen a request; completion timestamp should follow the status.
- [ ] Use a phone to check login, invoices, lease documents, requests and photos.

## Operational handoff

- [ ] Confirm actual property addresses, lease terms, balances and owner permissions.
- [ ] Confirm the outgoing address and public links in real test emails.
- [ ] Confirm persistent upload storage survives an application restart/redeployment.
- [ ] Take a database + upload backup, restore both separately, and open a document.
- [ ] Check `/health` and make sure errors/failed webhooks have an owner to review them.
- [ ] Confirm the app runs as one instance for the pilot.
- [ ] Add the Tenant Login link to the public Peña Plaza website.
- [ ] Invite one property and a few tenants; compare the first full billing cycle
      with existing records before expanding.
