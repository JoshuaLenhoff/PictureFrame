package de.pictureframe.server.security;

import de.pictureframe.server.user.Role;
import de.pictureframe.server.user.User;
import de.pictureframe.server.user.UserRepository;

import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements ApplicationRunner
{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.initial-admin.username:}")
    private String adminUsername;

    @Value("${app.initial-admin.password:}")
    private String adminPassword;

    public AdminInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(@NonNull ApplicationArguments args) {
        if (adminUsername.isBlank() || adminPassword.isBlank()) {
            return;
        }

        if (userRepository.findByUsername(adminUsername).isPresent()) {
            return;
        }

        if (adminPassword.length() < 12) {
            throw new IllegalStateException("Das Admin-Passwort muss mindestens 12 Zeichen haben.");
        }

        User admin = new User(adminUsername, passwordEncoder.encode(adminPassword), Role.ADMIN);
        userRepository.save(admin);
    }

}
