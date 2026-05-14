package com.retours.controller;

import com.retours.converter.RetourProduitConverter;
import com.retours.dto.RetourProduitDTO;
//import com.retours.dto.RetourProduitDTO;
import com.retours.entity.RetourProduit;
import com.retours.enums.EtatTraitement;
import com.retours.service.RetourProduitService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
    private final RetourProduitConverter retourProduitConverter;

    @PostMapping
    @PreAuthorize("hasAnyRole('AGENT','QUALITE','ADMIN')")
    public ResponseEntity<RetourProduitDTO> create(@RequestBody Map<String, Object> request) {
        RetourProduit saved = retourProduitService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(retourProduitConverter.toDto(saved));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('AGENT','QUALITE','ADMIN')")
    public ResponseEntity<List<RetourProduitDTO>> list(@RequestParam(required = false) String etat) {
        List<RetourProduit> retours;
        if (etat == null) {
            retours = retourProduitService.listAll();
        } else {
            retours = retourProduitService.listByEtat(EtatTraitement.valueOf(etat.toUpperCase()));
        }
        return ResponseEntity.ok(retours.stream().map(retourProduitConverter::toDto).toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('AGENT','QUALITE','ADMIN')")
    public ResponseEntity<RetourProduitDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(retourProduitConverter.toDto(retourProduitService.getById(id)));
    }

    @PutMapping("/{id}/etat")
    @PreAuthorize("hasAnyRole('QUALITE','ADMIN')")
    public ResponseEntity<RetourProduitDTO> updateEtat(@PathVariable Long id,
                                        @RequestBody Map<String, Object> request,
                                        Authentication authentication) {
        String etatStr = (String) request.get("etatTraitement");
        EtatTraitement etat = EtatTraitement.valueOf(etatStr.toUpperCase());
        RetourProduit updated = retourProduitService.updateEtat(id, etat, authentication.getName());
        return ResponseEntity.ok(retourProduitConverter.toDto(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('QUALITE','ADMIN')")
    public ResponseEntity<RetourProduitDTO> delete(@PathVariable Long id) {
        retourProduitService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
