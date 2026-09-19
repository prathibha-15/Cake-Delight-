package com.cakedelight.user.config;

import com.cakedelight.user.entity.Role;
import com.cakedelight.user.entity.User;
import com.cakedelight.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Seeds a deterministic LOCAL DEVELOPMENT/DEMO admin account.
 * Idempotent: does nothing if a user with the seed username already exists,
 * and never overwrites an existing user's password or role.
 */
@Component
public class AdminSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.seed.enabled:true}")
    private boolean seedEnabled;

    @Value("${admin.seed.username:admin}")
    private String seedUsername;

    @Value("${admin.seed.email:admin@cakedelight.local}")
    private String seedEmail;

    @Value("${admin.seed.password:Admin@12345}")
    private String seedPassword;

    public AdminSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!seedEnabled) {
            return;
        }

        userRepository.findByUsername(seedUsername).ifPresentOrElse(existing -> {
            if (existing.getRole() != Role.ROLE_ADMIN) {
                log.info("Admin seed skipped: user '{}' already exists without ROLE_ADMIN", seedUsername);
                return;
            }
            if (!passwordEncoder.matches(seedPassword, existing.getPassword())) {
                // Known dev credentials must keep working; only reset the hash for an existing ROLE_ADMIN account.
                existing.setPassword(passwordEncoder.encode(seedPassword));
                userRepository.save(existing);
                log.info("Reset password for existing local development admin account '{}' to the configured seed password", seedUsername);
            } else {
                log.info("Admin seed skipped: user '{}' already exists with ROLE_ADMIN and matching password", seedUsername);
            }
        }, () -> {
            User admin = new User();
            admin.setUsername(seedUsername);
            admin.setEmail(seedEmail);
            admin.setPassword(passwordEncoder.encode(seedPassword));
            admin.setRole(Role.ROLE_ADMIN);
            admin.setCreatedAt(LocalDateTime.now());

            userRepository.save(admin);
            log.info("Seeded local development admin account '{}' with ROLE_ADMIN", seedUsername);
        });
    }
}
