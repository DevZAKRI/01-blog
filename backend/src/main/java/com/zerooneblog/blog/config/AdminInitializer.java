package com.zerooneblog.blog.config;

import java.util.logging.Logger;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.zerooneblog.blog.model.User;
import com.zerooneblog.blog.repository.UserRepository;

/**
 * Initializes a default admin account on first server startup if no admin exists.
 */
@Component
public class AdminInitializer implements ApplicationRunner {

    private static final Logger logger = Logger.getLogger(AdminInitializer.class.getName());

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // Check if any admin user exists
        boolean adminExists = userRepository.findAll().stream()
                .anyMatch(user -> "ADMIN".equals(user.getRole()));

        if (!adminExists) {
            logger.info("[AdminInitializer] No admin user found. Creating default admin account...");

            // Check if username or email already exists
            if (userRepository.existsByUsernameIgnoreCase("admin")) {
                logger.warning("[AdminInitializer] Username 'admin' already exists. Skipping admin creation.");
                return;
            }
            if (userRepository.existsByEmailIgnoreCase("admin@admin.com")) {
                logger.warning("[AdminInitializer] Email 'admin@admin.com' already exists. Skipping admin creation.");
                return;
            }

            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setEmail("admin@admin.com");
            admin.setRole("ADMIN");
            admin.setBio("System Administrator");
            admin.setTokenVersion(0L);

            userRepository.save(admin);
            logger.info("[AdminInitializer] Default admin account created successfully.");
            logger.info("[AdminInitializer] Username: admin");
            logger.info("[AdminInitializer] Password: admin");
            logger.info("[AdminInitializer] Email: admin@admin.com");
            logger.warning("[AdminInitializer] Please change the default admin password after first login!");
        } else {
            logger.info("[AdminInitializer] Admin user already exists. Skipping initialization.");
        }
    }
}
