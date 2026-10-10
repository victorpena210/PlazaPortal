# Validation for this delivery

Validated on 2026-10-09 (America/Chicago).

- Java 17 / Spring Boot 4.0.5 compilation and executable JAR packaging: passed.
- Full Maven suite: **32 tests, 0 failures, 0 errors, 0 skipped**.
- MySQL 8.0.36: original migrations through V27, legacy owner preservation, upgrade
  through V30, unit insertion, and full Spring Boot startup with Hibernate schema
  validation: passed on an isolated disposable database.
- H2 workflow tests: company and tenant access restrictions, role boundaries, unit
  uniqueness, page rendering, lease overlap/validation, proration, invoice generation,
  late fees, historical billing start, offline partial payments, signed webhooks,
  duplicate events, delayed payments, amount/account/mode mismatches, refunds,
  invitation consumption, password reset, private PDFs/photos, maintenance updates,
  and payment history before invitation acceptance: passed.
- Simultaneous invoice generation and simultaneous checkout reservation: passed.
- Packaged application: launched against MySQL and signed in successfully.
- Browser navigation: ten owner/tenant pages returned HTTP 200.
- Mobile checks at 390px: six primary pages had no document-level horizontal overflow.
- Desktop and phone screenshots visually reviewed. No browser page errors observed.
- Original supplied SQL migrations are byte-for-byte unchanged.
- Old embedded database and SMTP passwords are absent from the replacement source.
- Backup shell script syntax: passed; a real backup/restore with your production
  credentials remains part of the pilot checklist.

## Test suites

| Suite | Tests | Failures | Errors | Skipped |
|---|---:|---:|---:|---:|
| ConcurrencyTest | 2 | 0 | 0 | 0 |
| MigrationTest | 1 | 0 | 0 | 0 |
| PenaPlazaPortalApplicationTests | 1 | 0 | 0 | 0 |
| PortalWorkflowTest | 28 | 0 | 0 | 0 |

## Checks requiring your configured environment

No actual Stripe charge, refund, account onboarding, SMTP delivery or production
backup/restore was performed. Provider calls in the automated suite are mocked;
webhook signatures are exercised using real HMAC verification with a test secret.
Use the Stripe sandbox and pilot checklist before enabling live payments. Review
historical balances against your records, since the previous code could mark an
invoice paid without a payment provider confirmation.

Run a single application instance during the pilot. Session invalidation, auth
rate limiting and the mail worker are per instance. Property accounting policies
and the correct receiving Stripe accounts must be confirmed by the owner.
