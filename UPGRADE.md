# Upgrade this project

This is a complete replacement source tree based on the uploaded `PlazaPortal 2(1).zip`.
The source retains the original Java package and Maven coordinates.

## Before running against existing data

1. Keep a copy of your current project, and back up your MySQL database and files.
2. Rotate the email/database credentials previously embedded in the uploaded source.
   Change the old default admin password before exposing the app publicly. With email
   configured, the password-reset flow can change an existing account's password.
3. Open this extracted `PlazaPortal` folder in IntelliJ as a Maven project. Use Java 17.
   Start in a separate folder to avoid mixing removed controllers with the new ones.
   Your existing Git repository can be updated after reviewing the replacement files.
4. Enter the environment variables from `.env.example` into the run configuration.
   Configuration no longer includes database or email passwords. Use the same database
   only after testing the upgrade on a restored copy.
5. Run `bash ./mvnw test`, then start the app against the restored test database.
6. Review owners, properties, units, active leases and balances before inviting tenants.

## Database compatibility

- Original SQL migrations V1–V27 are unchanged. Existing Flyway checksums are preserved.
- The uploaded history references `tenant_invitation.lease_id` in V25 without creating
  it. `LegacyInvitationMigrationCallback` supplies that missing column immediately
  before V25 on a fresh installation. It does nothing when the column already exists.
- V28 adds companies, memberships, properties, billing safeguards, mail/reset/audit
  tables, document storage metadata and payment-verification fields.
- Existing admins become owners of the initial Peña Plaza company; existing agents
  become its managers. Existing offices are assigned to its initial property.
- V29 adds private maintenance-photo metadata.
- V30 starts new automatic billing in the current month (or a future lease start),
  preserving all existing invoices without inventing historical arrears.
- No existing leases, invoices or payments are deleted. Historical duplicate invoices
  remain visible for manual review. Only the earliest invoice per lease/month reserves
  the billing period against future duplicate generation.
- Original raw invitation tokens are invalidated. Resend invitations from the lease list.
- Previously recorded `PAID` values are preserved; the old app could set those without
  verifying money collection, so reconcile them with actual receipts before the pilot.

If an existing database has an applied migration missing from the uploaded history
(for example a separate V22 file), restore that exact migration from your original
repository. Do not disable Flyway validation or run `repair` to conceal a mismatch.

## First login after upgrade

Existing accounts keep their passwords and log in as before. Owners see the initial
company automatically. Update its name and contact email under Company, then edit
the initial property's address and unit inventory. Invite the other parent as an
owner so both have individual logins to the same company.

For a completely new database, supply the bootstrap admin variables. They create an
owner only when that email does not exist, and never overwrite an existing account.

## Going live

Use the pilot checklist, configure actual email and payment accounts, confirm your
parents' lease terms, test backups and restore, and invite a small number of tenants.
Keep `penaplaza.com` as the public property site. Its Tenant Login button should link
to `https://YOUR_PORTAL_DOMAIN/login`.
