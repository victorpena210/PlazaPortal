package com.victorpena.plaza;

import static org.assertj.core.api.Assertions.*;

import java.sql.*;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

@EnabledIfEnvironmentVariable(named = "MIGRATION_TEST_URL", matches = ".+")
class MigrationTest {
  @Test
  void migratesAnExistingDatabaseAndValidatesSchema() throws Exception {
    String url = System.getenv("MIGRATION_TEST_URL"),
        user = System.getenv("MIGRATION_TEST_USER"),
        password = System.getenv("MIGRATION_TEST_PASSWORD");
    var initial =
        Flyway.configure()
            .callbacks(new com.victorpena.plaza.config.LegacyInvitationMigrationCallback())
            .dataSource(url, user, password)
            .target("27")
            .load();
    initial.migrate();
    try (var c = DriverManager.getConnection(url, user, password);
        var s = c.createStatement()) {
      s.executeUpdate(
          "INSERT INTO users(first_name,last_name,email,password,role,enabled)"
              + " VALUES('Legacy','Owner','migration@example.test','unused-test-hash','ADMIN',1)");
    }
    var latest =
        Flyway.configure()
            .callbacks(new com.victorpena.plaza.config.LegacyInvitationMigrationCallback())
            .dataSource(url, user, password)
            .load();
    latest.migrate();
    latest.validate();
    try (var c = DriverManager.getConnection(url, user, password);
        var s = c.createStatement()) {
      try (var rs = s.executeQuery("SELECT COUNT(*) FROM company_memberships")) {
        rs.next();
        assertThat(rs.getInt(1)).isEqualTo(1);
      }
      try (var rs = s.executeQuery("SELECT COUNT(*) FROM offices WHERE property_id IS NOT NULL")) {
        rs.next();
        assertThat(rs.getInt(1)).isEqualTo(11);
      }
      s.executeUpdate(
          "INSERT INTO offices(property_id,office_number,status) SELECT MIN(id),'NEW','AVAILABLE'"
              + " FROM properties");
    }
    var app = new org.springframework.boot.SpringApplication(PenaPlazaPortalApplication.class);
    try (var context =
        app.run(
            "--spring.datasource.url=" + url,
            "--spring.datasource.username=" + user,
            "--spring.datasource.password=" + password,
            "--spring.jpa.hibernate.ddl-auto=validate",
            "--server.port=0",
            "--app.jobs-enabled=false",
            "--app.mail-enabled=false",
            "--app.bootstrap-email=",
            "--app.bootstrap-password=")) {
      assertThat(context.isActive()).isTrue();
    }
  }
}
