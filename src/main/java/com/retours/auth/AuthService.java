package com.retours.auth;

import com.retours.auth.dto.AuthUserResponse;
import com.retours.auth.dto.LoginRequest;
import com.retours.auth.dto.LoginResponse;
import com.retours.entity.Utilisateur;
import com.retours.exception.ResourceNotFoundException;
import com.retours.repository.UtilisateurRepository;
import java.time.LocalDateTime;
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
    public LoginResponse login(LoginRequest request) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Email ou mot de passe invalide"));

        if (!utilisateur.isEnabled()) {
            throw new BadCredentialsException("Compte desactive");
        }

        if (!passwordEncoder.matches(request.getPassword(), utilisateur.getPasswordHash())) {
            throw new BadCredentialsException("Email ou mot de passe invalide");
        }

        utilisateur.setLastLoginAt(LocalDateTime.now());
        utilisateurRepository.save(utilisateur);

        return LoginResponse.builder()
                .accessToken(jwtService.generateToken(utilisateur))
                .tokenType("Bearer")
                .expiresIn(jwtService.getJwtExpirationMs() / 1000)
                .user(AuthUserResponse.fromEntity(utilisateur))
                .build();
    }

    @Transactional(readOnly = true)
    public AuthUserResponse me(String email) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
        return AuthUserResponse.fromEntity(utilisateur);
    }
}
