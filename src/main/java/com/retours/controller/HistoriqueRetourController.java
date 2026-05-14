package com.retours.controller;

import com.retours.converter.HistoriqueRetourConverter;
import com.retours.dto.HistoriqueRetourDTO;
//import com.retours.dto.HistoriqueRetourDTO;
//import com.retours.entity.HistoriqueRetour;
import com.retours.service.HistoriqueRetourService;
//import java.util.List;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/historiques")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('AGENT','QUALITE','ADMIN')")
public class HistoriqueRetourController {

    private final HistoriqueRetourService historiqueRetourService;
    private final HistoriqueRetourConverter historiqueRetourConverter;

    @GetMapping
    public ResponseEntity<List<HistoriqueRetourDTO>> listAll() {
        return ResponseEntity.ok(
            historiqueRetourService.listAll().stream().map(historiqueRetourConverter::toDto).toList()
        );
    }

    @GetMapping("/retour/{retourId}")
    public ResponseEntity<List<HistoriqueRetourDTO>> listByRetour(@PathVariable Long retourId) {
        return ResponseEntity.ok(
            historiqueRetourService.listByRetour(retourId).stream().map(historiqueRetourConverter::toDto).toList()
        );
    }
}
