package com.retours.controller;

import com.retours.dto.request.CreateUtilisateurRequest;
import com.retours.dto.request.UpdateUtilisateurRequest;
import com.retours.entity.Utilisateur;
import com.retours.service.UtilisateurService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    @PostMapping
    public ResponseEntity<Utilisateur> create(@Valid @RequestBody CreateUtilisateurRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(utilisateurService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<Utilisateur>> listAll() {
        return ResponseEntity.ok(utilisateurService.listAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Utilisateur> getById(@PathVariable Long id) {
        return ResponseEntity.ok(utilisateurService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Utilisateur> update(@PathVariable Long id,
                                              @Valid @RequestBody UpdateUtilisateurRequest request) {
        return ResponseEntity.ok(utilisateurService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        utilisateurService.delete(id);
        return ResponseEntity.noContent().build();
    }
}