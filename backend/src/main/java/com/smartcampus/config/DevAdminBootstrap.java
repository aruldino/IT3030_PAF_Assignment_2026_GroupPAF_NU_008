package com.smartcampus.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.smartcampus.enums.UserRole;
import com.smartcampus.enums.UserStatus;
import com.smartcampus.model.User;
import com.smartcampus.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Development-only admin bootstrap from environment values.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DevAdminBootstrap implements CommandLineRunner {

  private final UserRepository userRepository;

  @Value("${app.dev-admin.enabled:false}")
  private boolean enabled;

  @Value("${app.dev-admin.email:}")
  private String adminEmail;

  @Value("${app.dev-admin.name:Admin User}")
  private String adminName;

  @Override
  public void run(String... args) {
    if (!enabled || adminEmail == null || adminEmail.isBlank()) {
      return;
    }

    String email = adminEmail.trim().toLowerCase();

    User admin = userRepository.findByEmail(email).orElseGet(() -> User.builder()
        .email(email)
        .name(adminName)
        .role(UserRole.ADMIN)
        .status(UserStatus.ACTIVE)
        .build());

    admin.setName(adminName);
    admin.setRole(UserRole.ADMIN);
    admin.setStatus(UserStatus.ACTIVE);

    userRepository.save(admin);
    log.info("Dev admin is ready: {}", email);
  }
}
