package com.retours.controller;

import com.retours.converter.UtilisateurConverter;
import com.retours.dto.UtilisateurDTO;
import com.retours.entity.Utilisateur;
import com.retours.service.UtilisateurService;

import java.util.List;
//import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/utilisateurs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;
    private final UtilisateurConverter utilisateurConverter;

    @PostMapping
    public ResponseEntity<UtilisateurDTO> create(@RequestBody Map<String, Object> request) {
        Utilisateur saved = utilisateurService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(utilisateurConverter.toDto(saved));
    }

    @GetMapping
    public ResponseEntity<List<UtilisateurDTO>> listAll() {
        return ResponseEntity.ok(
            utilisateurService.listAll().stream().map(utilisateurConverter::toDto).toList()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<UtilisateurDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(utilisateurConverter.toDto(utilisateurService.getById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UtilisateurDTO> update(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        Utilisateur updated = utilisateurService.update(id, request);
        return ResponseEntity.ok(utilisateurConverter.toDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<UtilisateurDTO> delete(@PathVariable Long id) {
        utilisateurService.delete(id);
        return ResponseEntity.noContent().build();
    }
}