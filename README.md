# PlazaPortal

Commercial property and tenant management for independent owner/company accounts.
Built with Java 17, Spring Boot 4.0.5, Thymeleaf, Spring Security, MySQL and Flyway.

## Start here

1. Read [UPGRADE.md](UPGRADE.md) before replacing an existing installation.
2. Configure the environment using [.env.example](.env.example) as a reference.
3. Run `bash ./mvnw test`, then `bash ./mvnw spring-boot:run`.
4. Use [docs/PILOT-CHECKLIST.md](docs/PILOT-CHECKLIST.md) for the acceptance run.

The `.env.example` file is documentation. Spring Boot does **not** automatically load
a `.env` file. Add the variables in IntelliJ's Run Configuration → Environment
variables, your shell, or your hosting service. Never commit real credentials.

## Implemented workflows

- Company accounts, company switching, owner/manager memberships, expiring team
  invitations, and company-scoped access on every management operation.
- Properties and rentable units. Unit numbers are unique within each property.
- Lease creation, overlap prevention, renewals, early end dates, active occupancy,
  invitation acceptance by new or existing tenants, and private lease PDFs.
- Monthly rent invoices with unique billing periods, a selected billing-start month, catch-up generation through
  next month, configurable due day, optional first-month proration, and an optional
  one-time late fee per rent invoice after its grace period.
- One-off deposits/utilities/other agreed charges. Unpaid invoices can be voided
  with a reason. Offline partial payments require a method, reference and note.
- Hosted Stripe Checkout. Amounts are calculated on the server. Confirmed payment
  status comes from verified provider data, never a browser redirect. Account,
  invoice, payment, amount, currency and test/live mode must match.
- Duplicate checkout prevention, stable provider idempotency keys, signed webhook
  verification, async success/failure handling, expiration, payment reconciliation,
  checkout recovery/cancellation, refunds and private receipts.
- Maintenance requests, priority, completion status, tenant comments, manager
  updates and up to five private JPEG/PNG photos per request. Photos are decoded
  and re-encoded to JPEG to remove embedded metadata.
- Password reset, session invalidation after reset, CSRF protection, content
  security policy, authentication-form rate limits and audit entries.
- Durable mail outbox with retry, responsive pages, `/health`, Dockerfile and CI.

## Local setup

Create an empty MySQL database and a dedicated application user. Supply `DB_URL`,
`DB_USERNAME`, and `DB_PASSWORD`. The database is not created automatically.

For an initial owner, set `BOOTSTRAP_ADMIN_EMAIL` and a unique
`BOOTSTRAP_ADMIN_PASSWORD` of at least 12 characters. The initializer creates the
account only if that email does not exist. It never resets an existing password.
Remove the bootstrap password from the environment after the account is created.

Public owner signup is off by default. An existing owner can create another company
from Company → Add another company. Set `OWNER_SIGNUP_ENABLED=true` only if you want
public company-account registration. Tenants use a lease invitation.

Open http://localhost:8080. New databases include the original project's eleven
example units under Peña Plaza. Replace the sample details with the actual inventory
before inviting tenants. Existing records are assigned to the initial company.

## Email

Set `APP_BASE_URL` to the public HTTPS origin before inviting remote users. Configure
`MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM` and
`MAIL_ENABLED=true`. Port 587 defaults to STARTTLS. For a provider using implicit TLS
on port 465, set `MAIL_PORT=465`, `MAIL_SSL=true`, `MAIL_STARTTLS=false`.

Invitations, password resets, invoices, payments and maintenance updates are queued
in `mail_outbox`. With mail disabled they remain queued; they are not delivered.
Failed deliveries are retried with increasing delay. Delivered bodies are removed
from the outbox record. Test delivery to addresses you control before a pilot.

## Payments

Use a separate Stripe sandbox for development, and another for CI/integration tests.
The automated suite uses a mock gateway; it does not send live Stripe requests.

Configure:

- `STRIPE_API_KEY`: preferably a restricted API key with the required Checkout,
  PaymentIntent and Charge read/create permissions. Match permissions to the
  integration, including session expiration. Keep it in environment secrets.
- `STRIPE_WEBHOOK_SECRET`: the signing secret for `/webhooks/stripe`.
- `STRIPE_COMPANY_ACCOUNTS`: an explicit company-to-receiving-account mapping.
  Example `1:acct_ownerone,2:acct_ownertwo`. Company IDs appear under Company.
- `STRIPE_LIVE_MODE=false` for the sandbox; enable live mode only after acceptance.

For a single business collecting its own rent, `1:platform` is supported **only if
the supplied API key belongs to that business's Stripe account**. For independent
owners, configure their approved connected accounts and use the `acct_...` mapping.
The application creates direct charges on the selected account and does not collect
a platform fee. Accounts must already be connected and enabled through Stripe.
Account creation/onboarding and pricing/liability agreements are not silently
performed by this project. There is no default recipient for unconfigured companies,
and an account can map to only one company. Tenants cannot choose payment recipients.

Subscribe the webhook to:

- `checkout.session.completed`
- `checkout.session.async_payment_succeeded`
- `checkout.session.async_payment_failed`
- `checkout.session.expired`
- `charge.refunded`

For connected accounts, subscribe to events on connected accounts. If you also use
the platform account, configure the destination to receive the relevant platform
events with the signing secret used here. Use one compatible event destination per
deployment, or extend the configuration to support independently signed endpoints.
Payment methods are configured in Stripe; the code does not hard-code an allowlist.

Webhooks require the raw body and signature. The success page only shows a status
message. Duplicate events do not duplicate payments or receipts. Account/amount/
currency/mode mismatches are rejected. Provider errors return retryable server errors.
Refund notifications and reconciliation reopen the appropriate balance; the owner
must void or adjust the associated obligation if it is no longer owed.

If an attempt is left pending after an interrupted connection, use Payments → Check /
recover payment status. The same idempotency key is reused within its safe retry
window. Do not manually delete attempts or manufacture a new payment to bypass an
unresolved result. For an older unresolved attempt, locate its metadata in Stripe
and reconcile it before accepting another payment. Cancel unpaid checkout verifies
expiration at Stripe before allowing an offline payment.

## Billing policy

Rent defaults to full months, due on day 1, with no late fee. Choose the first month
to bill in this app when adding a historical lease; the form defaults to the current
month, so rent paid elsewhere is not automatically treated as years of arrears. Proration and late fees
are opt-in lease settings. Confirm the signed commercial lease's terms with the owner.
Proration uses occupied days divided by calendar days in the first month; money rounds
half up to two decimals. Final-month rent is not automatically prorated. Termination
voids unpaid later-month rent only and blocks if future payments require resolution.
Invoices for a period already voided are not silently regenerated.

Deposits and other charges are tracked as labeled invoices, not trust accounting.
This release is a property/tenant operations tool, not a general ledger, tax filing
system, escrow ledger, or automatic legal-fee policy engine. Reconcile owner records
and bank/processor statements throughout the pilot.

## Deployment and operation

- Build with `bash ./mvnw clean package`, or build the included Dockerfile.
- Use a persistent MySQL database and a persistent volume for `UPLOAD_DIR`.
  A container's temporary filesystem is not durable document storage.
- Set `APP_BASE_URL=https://your-portal-domain`, `SESSION_COOKIE_SECURE=true`,
  and keep the public site linked to that URL's `/login` page.
- Run **one application instance** for the pilot. Invoice/payment writes use database
  locks and constraints, but mail delivery, login rate limits and session invalidation
  are currently per instance. Add distributed coordination before scaling replicas.
- Daily billing runs at 1:00 a.m. in `APP_TIME_ZONE` (default America/Chicago).
  Use Billing → Generate missing rent invoices for catch-up after downtime.
- Use `/health` for process/database health checks. Monitor application errors,
  failed webhook deliveries, stale pending payments and unsent outbox records.
- Back up MySQL and the upload volume together. Perform an actual restore into a
  separate database and upload folder before launch. Schedule backups in your host. The included `scripts/backup.sh` creates a
  private SQL dump and upload archive when supplied `BACKUP_DB_HOST`,
  `BACKUP_DB_NAME`, `DB_USERNAME`, and `DB_PASSWORD`. TLS is required by default.
  Restore into an empty *different* database with `mysql -u USER -p RESTORE_DB <
  database.sql`, then extract uploads into a different folder and point a test
  deployment at both. Confirm records and downloads before relying on the backup.

## Tests

`bash ./mvnw test` runs the H2-backed workflow and concurrency tests. The MySQL
migration test runs only when `MIGRATION_TEST_URL`, `MIGRATION_TEST_USER` and
`MIGRATION_TEST_PASSWORD` are present. Use a **new empty disposable database**:

```bash
MIGRATION_TEST_URL='jdbc:mysql://localhost:3306/plaza_test' MIGRATION_TEST_USER='plaza_test_user' MIGRATION_TEST_PASSWORD='YOUR_TEST_DATABASE_PASSWORD' bash ./mvnw -Dtest=MigrationTest test
```

That test applies the original schema through V27, adds a legacy owner, upgrades to
the new schema, checks data preservation, and starts Spring Boot with Hibernate
schema validation against MySQL. CI supplies an isolated MySQL service for this test.
Never point migration tests at the production database.

See [docs/VALIDATION.md](docs/VALIDATION.md) for the checks performed on this delivery.

## Integration references

- [Stripe Checkout Sessions](https://docs.stripe.com/payments/checkout-sessions)
- [Direct charges for independent accounts](https://docs.stripe.com/connect/direct-charges)
- [Webhook verification](https://docs.stripe.com/webhooks)
