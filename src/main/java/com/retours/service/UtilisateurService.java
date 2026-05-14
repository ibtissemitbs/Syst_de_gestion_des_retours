package com.retours.service;

import com.retours.entity.Utilisateur;
import com.retours.enums.Role;
import com.retours.repository.HistoriqueRetourRepository;
import com.retours.repository.UtilisateurRepository;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final HistoriqueRetourRepository historiqueRetourRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Utilisateur create(Map<String, Object> request) {
        String nom = (String) request.get("nom");
        String email = (String) request.get("email");
        String password = (String) request.get("password");
        String roleStr = (String) request.get("role");

        if (nom == null || nom.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le nom est obligatoire");
        }
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "L'email est obligatoire");
        }
        if (password == null || password.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le mot de passe est obligatoire");
        }
        if (roleStr == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le role est obligatoire");
        }

        utilisateurRepository.findByEmail(email).ifPresent(existing -> {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email deja utilise");
        });

        Role role = Role.valueOf(roleStr.toUpperCase());
        Utilisateur utilisateur = Utilisateur.builder()
                .nom(nom)
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .enabled(true)
                .role(role)
                .build();

        return utilisateurRepository.save(utilisateur);
    }

    @Transactional(readOnly = true)
    public List<Utilisateur> listAll() {
        return utilisateurRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Utilisateur getById(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
    }

    @Transactional
    public Utilisateur update(Long id, Map<String, Object> request) {
        Utilisateur utilisateur = getById(id);

        String nom = (String) request.get("nom");
        String email = (String) request.get("email");
        String password = (String) request.get("password");
        String roleStr = (String) request.get("role");

        if (nom != null && !nom.isBlank()) {
            utilisateur.setNom(nom);
        }
        if (email != null && !email.isBlank()) {
            utilisateurRepository.findByEmail(email).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email deja utilise");
                }
            });
            utilisateur.setEmail(email);
        }
        if (roleStr != null) {
            utilisateur.setRole(Role.valueOf(roleStr.toUpperCase()));
        }
        if (password != null && !password.isBlank()) {
            utilisateur.setPasswordHash(passwordEncoder.encode(password));
        }

        return utilisateurRepository.save(utilisateur);
    }

    @Transactional
    public void delete(Long id) {
        Utilisateur utilisateur = getById(id);
        if (historiqueRetourRepository.existsByEmployeId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Suppression impossible: cet utilisateur est reference dans l'historique");
        }
        utilisateurRepository.delete(utilisateur);
    }
}