package com.retours.controller;

import com.retours.entity.HistoriqueRetour;
import com.retours.service.HistoriqueRetourService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/historiques")
@RequiredArgsConstructor
public class HistoriqueRetourController {

    private final HistoriqueRetourService historiqueRetourService;

    @GetMapping
    public ResponseEntity<List<HistoriqueRetour>> listAll() {
        return ResponseEntity.ok(historiqueRetourService.listAll());
    }

    @GetMapping("/retour/{retourId}")
    public ResponseEntity<List<HistoriqueRetour>> listByRetour(@PathVariable Long retourId) {
        return ResponseEntity.ok(historiqueRetourService.listByRetour(retourId));
    }
}
