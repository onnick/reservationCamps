package com.onnick.reservationcamps.config;

import com.onnick.reservationcamps.domain.UserRole;
import com.onnick.reservationcamps.service.UserService;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
public class LocalAdminBootstrap implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(LocalAdminBootstrap.class);
    private static final SecureRandom rng = new SecureRandom();

    private final UserService userService;
    private final String adminEmail;
    private final String configuredPassword;

    public LocalAdminBootstrap(
            UserService userService,
            @Value("${bootstrap.admin.email:admin@example.com}") String adminEmail,
            @Value("${bootstrap.admin.password:}") String configuredPassword) {
        this.userService = userService;
        this.adminEmail = adminEmail;
        this.configuredPassword = configuredPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        var normalized = adminEmail == null ? "" : adminEmail.trim().toLowerCase();
        if (normalized.isBlank()) {
            log.warn("Local admin bootstrap skipped: bootstrap.admin.email is blank.");
            return;
        }

        if (userService.findByEmail(normalized).isPresent()) {
            return;
        }

        var password = configuredPassword == null ? "" : configuredPassword.trim();
        Path credsFile = Path.of("data", "bootstrap-admin.txt");
        if (password.isBlank()) {
            password = generatePassword(14);
            try {
                Files.createDirectories(credsFile.getParent());
                Files.writeString(
                        credsFile,
                        "email=" + normalized + "\npassword=" + password + "\n",
                        StandardCharsets.UTF_8);
            } catch (IOException e) {
                log.warn("Could not write local admin credentials file at {}.", credsFile, e);
            }
        }

        userService.createUser(normalized, password, UserRole.ADMIN);

        if ((configuredPassword == null || configuredPassword.isBlank()) && Files.exists(credsFile)) {
            log.info("Local admin created. Credentials stored in {}.", credsFile.toAbsolutePath());
        } else {
            log.info("Local admin created: {} (password provided via bootstrap.admin.password).", normalized);
        }
    }

    private static String generatePassword(int len) {
        // Avoid confusing characters for manual typing.
        final String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(alphabet.charAt(rng.nextInt(alphabet.length())));
        }
        return sb.toString();
    }
}

