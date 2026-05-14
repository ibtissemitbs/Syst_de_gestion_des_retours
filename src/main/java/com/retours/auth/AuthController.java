package com.retours.auth;

import java.util.HashMap;
import java.util.Map;
import com.retours.converter.UtilisateurConverter;
import com.retours.service.UtilisateurService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UtilisateurService utilisateurService;
    private final UtilisateurConverter utilisateurConverter;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Map<String, Object> request) {
        
        Map<String, Object> payload = new HashMap<>(request == null ? Map.of() : request);
        payload.putIfAbsent("role", "AGENT");

        var saved = utilisateurService.create(payload);
        return ResponseEntity.status(HttpStatus.CREATED).body(utilisateurConverter.toDto(saved));
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> me(Authentication authentication) {
        Map<String, Object> response = authService.me(authentication.getName());
        return ResponseEntity.ok(response);
    }
}
