package com.retours.controller;

import com.retours.dto.request.CreateRetourRequest;
import com.retours.dto.request.UpdateEtatRetourRequest;
import com.retours.dto.response.RetourDTO;
import com.retours.entity.RetourProduit;
import com.retours.enums.EtatTraitement;
import com.retours.service.RetourProduitService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/retours")
@RequiredArgsConstructor
public class RetourProduitController {

    private final RetourProduitService retourProduitService;

    @PostMapping
    public ResponseEntity<RetourDTO> create(@Valid @RequestBody CreateRetourRequest request) {
        RetourProduit saved = retourProduitService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(RetourDTO.fromEntity(saved));
    }

    @GetMapping
    public ResponseEntity<List<RetourDTO>> list(@RequestParam(required = false) EtatTraitement etat) {
        List<RetourProduit> retours = etat == null
                ? retourProduitService.listAll()
                : retourProduitService.listByEtat(etat);
        return ResponseEntity.ok(retours.stream().map(RetourDTO::fromEntity).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RetourDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(RetourDTO.fromEntity(retourProduitService.getById(id)));
    }

    @PutMapping("/{id}/etat")
    public ResponseEntity<RetourDTO> updateEtat(@PathVariable Long id,
                                                @Valid @RequestBody UpdateEtatRetourRequest request) {
        RetourProduit updated = retourProduitService.updateEtat(id, request.getEtatTraitement(), request.getEmployeId());
        return ResponseEntity.ok(RetourDTO.fromEntity(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        retourProduitService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
