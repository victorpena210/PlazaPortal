package com.victorpena.plaza.config;

import java.sql.SQLException;
import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.callback.Context;
import org.flywaydb.core.api.callback.Event;
import org.springframework.stereotype.Component;

/**
 * The supplied historical migrations omit creation of tenant_invitation.lease_id, but V25 adds its
 * foreign key. Bridge fresh installs without changing checksums of migrations that may already be
 * applied to an existing installation.
 */
@Component
public class LegacyInvitationMigrationCallback implements Callback {
  @Override
  public boolean supports(Event event, Context context) {
    return event == Event.BEFORE_EACH_MIGRATE;
  }

  @Override
  public boolean canHandleInTransaction(Event event, Context context) {
    return true;
  }

  @Override
  public String getCallbackName() {
    return "legacy-invitation-column";
  }

  @Override
  public void handle(Event event, Context context) {
    if (context.getMigrationInfo() == null
        || context.getMigrationInfo().getVersion() == null
        || !"25".equals(context.getMigrationInfo().getVersion().toString())) return;
    try {
      var connection = context.getConnection();
      try (var columns =
          connection
              .getMetaData()
              .getColumns(connection.getCatalog(), null, "tenant_invitation", "lease_id")) {
        if (columns.next()) return;
      }
      try (var statement = connection.createStatement()) {
        statement.execute("ALTER TABLE tenant_invitation ADD COLUMN lease_id BIGINT NULL");
      }
    } catch (SQLException exception) {
      throw new IllegalStateException("Cannot prepare legacy invitation schema for V25", exception);
    }
  }
}
