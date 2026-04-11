package com.retours.config;

import com.retours.entity.Utilisateur;
import com.retours.enums.Role;
import com.retours.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedUser("admin@retours.local", "Admin", Role.ADMIN, "Admin@123");
        seedUser("agent@retours.local", "Agent", Role.AGENT, "Agent@123");
        seedUser("qualite@retours.local", "Qualite", Role.QUALITE, "Qualite@123");
    }

    private void seedUser(String email, String nom, Role role, String rawPassword) {
        if (utilisateurRepository.findByEmail(email).isPresent()) {
            return;
        }

        Utilisateur utilisateur = Utilisateur.builder()
                .nom(nom)
                .email(email)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .enabled(true)
                .role(role)
                .build();

        utilisateurRepository.save(utilisateur);
    }
}
