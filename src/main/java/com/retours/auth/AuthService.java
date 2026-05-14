package com.retours.auth;

import com.retours.entity.Utilisateur;
import com.retours.repository.UtilisateurRepository;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public Map<String, Object> login(Map<String, Object> request) {
        String email = (String) request.get("email");
        String password = (String) request.get("password");

        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Email ou mot de passe invalide"));

        if (!utilisateur.isEnabled()) {
            throw new BadCredentialsException("Compte desactive");
        }

        if (!passwordEncoder.matches(password, utilisateur.getPasswordHash())) {
            throw new BadCredentialsException("Email ou mot de passe invalide");
        }

        utilisateur.setLastLoginAt(LocalDateTime.now());
        utilisateurRepository.save(utilisateur);

        String token = jwtService.generateToken(utilisateur);

        Map<String, Object> response = new HashMap<>();
        response.put("accessToken", token);
        response.put("tokenType", "Bearer");
        response.put("expiresIn", jwtService.getJwtExpirationMs() / 1000);
        
        Map<String, Object> user = new HashMap<>();
        user.put("id", utilisateur.getId());
        user.put("nom", utilisateur.getNom());
        user.put("email", utilisateur.getEmail());
        user.put("role", utilisateur.getRole());
        response.put("user", user);

        return response;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> me(String email) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Utilisateur introuvable"));
        
        Map<String, Object> response = new HashMap<>();
        response.put("id", utilisateur.getId());
        response.put("nom", utilisateur.getNom());
        response.put("email", utilisateur.getEmail());
        response.put("role", utilisateur.getRole());
        
        return response;
    }
}
