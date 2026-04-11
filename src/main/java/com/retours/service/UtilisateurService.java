package com.retours.service;

import com.retours.dto.request.CreateUtilisateurRequest;
import com.retours.dto.request.UpdateUtilisateurRequest;
import com.retours.entity.Utilisateur;
import com.retours.exception.ConflictException;
import com.retours.exception.ResourceNotFoundException;
import com.retours.repository.HistoriqueRetourRepository;
import com.retours.repository.UtilisateurRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final HistoriqueRetourRepository historiqueRetourRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Utilisateur create(CreateUtilisateurRequest request) {
        utilisateurRepository.findByEmail(request.getEmail()).ifPresent(existing -> {
            throw new IllegalArgumentException("Email deja utilise");
        });

        Utilisateur utilisateur = Utilisateur.builder()
                .nom(request.getNom())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .role(request.getRole())
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
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable avec id=" + id));
    }

    @Transactional
    public Utilisateur update(Long id, UpdateUtilisateurRequest request) {
        Utilisateur utilisateur = getById(id);

        utilisateurRepository.findByEmail(request.getEmail()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new IllegalArgumentException("Email deja utilise");
            }
        });

        utilisateur.setNom(request.getNom());
        utilisateur.setEmail(request.getEmail());
        utilisateur.setRole(request.getRole());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            utilisateur.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        return utilisateurRepository.save(utilisateur);
    }

    @Transactional
    public void delete(Long id) {
        Utilisateur utilisateur = getById(id);
        if (historiqueRetourRepository.existsByEmployeId(id)) {
            throw new ConflictException("Suppression impossible: cet utilisateur est reference dans l'historique (id=" + id + ")");
        }
        utilisateurRepository.delete(utilisateur);
    }
}