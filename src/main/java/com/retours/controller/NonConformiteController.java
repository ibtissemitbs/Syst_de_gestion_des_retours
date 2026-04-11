package com.retours.controller;

import com.retours.dto.request.CreateNonConformiteRequest;
import com.retours.dto.request.UpdateNonConformiteRequest;
import com.retours.dto.response.NonConformiteDTO;
import com.retours.entity.NonConformite;
import com.retours.service.NonConformiteService;
import jakarta.validation.Valid;
import java.util.List;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/non-conformites")
@RequiredArgsConstructor
public class NonConformiteController {

    private final NonConformiteService nonConformiteService;

    @PostMapping
    @PreAuthorize("hasAnyRole('AGENT','QUALITE','ADMIN')")
    public ResponseEntity<NonConformiteDTO> create(@Valid @RequestBody CreateNonConformiteRequest request) {
        NonConformite saved = nonConformiteService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(NonConformiteDTO.fromEntity(saved));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('AGENT','QUALITE','ADMIN')")
    public ResponseEntity<List<NonConformiteDTO>> list(@RequestParam(required = false) String produit) {
        List<NonConformite> list = produit == null
                ? nonConformiteService.listAll()
                : nonConformiteService.listByProduit(produit);
        return ResponseEntity.ok(list.stream().map(NonConformiteDTO::fromEntity).toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('AGENT','QUALITE','ADMIN')")
    public ResponseEntity<NonConformiteDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(NonConformiteDTO.fromEntity(nonConformiteService.getById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('QUALITE','ADMIN')")
    public ResponseEntity<NonConformiteDTO> update(@PathVariable Long id,
                                                    @Valid @RequestBody UpdateNonConformiteRequest request) {
        return ResponseEntity.ok(NonConformiteDTO.fromEntity(nonConformiteService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('QUALITE','ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        nonConformiteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}