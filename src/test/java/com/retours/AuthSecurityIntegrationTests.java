package com.retours;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.retours.entity.Utilisateur;
import com.retours.enums.Role;
import com.retours.repository.UtilisateurRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthSecurityIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUpUsers() {
        ensureUser("admin@retours.local", "Admin", Role.ADMIN, "Admin@123");
        ensureUser("agent@retours.local", "Agent", Role.AGENT, "Agent@123");
        ensureUser("qualite@retours.local", "Qualite", Role.QUALITE, "Qualite@123");
    }

    @Test
    void loginSuccessAndFailure() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@retours.local\",\"password\":\"Admin@123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.role").value("ADMIN"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@retours.local\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userManagementIsAdminOnly() throws Exception {
        String adminToken = loginAndGetToken("admin@retours.local", "Admin@123");
        String agentToken = loginAndGetToken("agent@retours.local", "Agent@123");

        mockMvc.perform(get("/api/utilisateurs")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/utilisateurs")
                        .header("Authorization", "Bearer " + agentToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void rolesAccessRulesOnRetours() throws Exception {
        String agentToken = loginAndGetToken("agent@retours.local", "Agent@123");
        String qualiteToken = loginAndGetToken("qualite@retours.local", "Qualite@123");

        mockMvc.perform(post("/api/retours")
                        .header("Authorization", "Bearer " + agentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"produit\":\"P1\",\"client\":\"C1\",\"raison\":\"Defaut\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(put("/api/retours/1/etat")
                        .header("Authorization", "Bearer " + agentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"etatTraitement\":\"VALIDE\"}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/retours/1/etat")
                        .header("Authorization", "Bearer " + qualiteToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"etatTraitement\":\"VALIDE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.etatTraitement").value("VALIDE"))
                .andExpect(jsonPath("$.stockMisAJour").value(true));
    }

    @Test
    void expiredTokenIsRejected() throws Exception {
        String expiredToken = buildExpiredToken("admin@retours.local", "ADMIN");

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void missingTokenIsRejected() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    void malformedTokenIsRejected() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer invalid.token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.details").isNotEmpty());
    }

    @Test
    void disabledUserCannotLogin() throws Exception {
        Utilisateur disabledUser = utilisateurRepository.findByEmail("agent@retours.local").orElseThrow();
        disabledUser.setEnabled(false);
        utilisateurRepository.save(disabledUser);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"agent@retours.local\",\"password\":\"Agent@123\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Compte desactive"));
    }

    private void ensureUser(String email, String nom, Role role, String password) {
        Utilisateur existing = utilisateurRepository.findByEmail(email).orElse(null);
        if (existing == null) {
            utilisateurRepository.save(Utilisateur.builder()
                    .nom(nom)
                    .email(email)
                    .passwordHash(passwordEncoder.encode(password))
                    .enabled(true)
                    .role(role)
                    .build());
            return;
        }

        existing.setNom(nom);
        existing.setRole(role);
        existing.setEnabled(true);
        existing.setPasswordHash(passwordEncoder.encode(password));
        utilisateurRepository.save(existing);
    }

    private String loginAndGetToken(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", email, "password", password))))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.get("accessToken").asText();
    }

    private String buildExpiredToken(String email, String role) {
        String secret = Encoders.BASE64.encode("Systeme-de-gestion-des-retours-secret-key-2026-secure-spring-boot".getBytes(StandardCharsets.UTF_8));
        SecretKey key = Keys.hmacShaKeyFor(io.jsonwebtoken.io.Decoders.BASE64.decode(secret));
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(Date.from(now.minusSeconds(120)))
                .expiration(Date.from(now.minusSeconds(60)))
                .signWith(key)
                .compact();
    }
}
