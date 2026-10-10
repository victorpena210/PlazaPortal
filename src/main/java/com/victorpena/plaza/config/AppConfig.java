package com.victorpena.plaza.config;

import java.time.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.security.core.session.*;

@Configuration
public class AppConfig {
  @Bean
  Clock clock(@Value("${app.time-zone:America/Chicago}") String zone) {
    return Clock.system(ZoneId.of(zone));
  }

  @Bean
  SessionRegistry sessionRegistry() {
    return new SessionRegistryImpl();
  }

  @Bean
  org.springframework.security.web.session.HttpSessionEventPublisher httpSessionEventPublisher() {
    return new org.springframework.security.web.session.HttpSessionEventPublisher();
  }
}
