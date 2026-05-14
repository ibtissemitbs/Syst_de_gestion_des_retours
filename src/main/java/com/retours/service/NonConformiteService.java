package com.retours.service;

import com.retours.entity.NonConformite;
import com.retours.entity.RetourProduit;
import com.retours.enums.Gravite;
import com.retours.repository.NonConformiteRepository;
import com.retours.repository.RetourProduitRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class NonConformiteService {

    private final NonConformiteRepository nonConformiteRepository;
    private final RetourProduitRepository retourProduitRepository;

    @Transactional
    public NonConformite create(Map<String, Object> request) {
        String description = (String) request.get("description");
        String graviteStr = (String) request.get("gravite");
        String produit = (String) request.get("produit");
        Long retourId = request.get("retourId") != null ? ((Number) request.get("retourId")).longValue() : null;

        if (description == null || description.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La description est obligatoire");
        }
        if (graviteStr == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La gravite est obligatoire");
        }
        if (produit == null || produit.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le produit est obligatoire");
        }

        RetourProduit retour = null;
        if (retourId != null) {
            retour = retourProduitRepository.findById(retourId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Retour introuvable"));
        }

        Gravite gravite = Gravite.valueOf(graviteStr.toUpperCase());
        NonConformite nonConformite = NonConformite.builder()
                .description(description)
                .gravite(gravite)
                .produit(produit)
                .retourProduit(retour)
                .date(LocalDateTime.now())
                .build();

        return nonConformiteRepository.save(nonConformite);
    }

    @Transactional(readOnly = true)
    public List<NonConformite> listAll() {
        return nonConformiteRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<NonConformite> listByProduit(String produit) {
        return nonConformiteRepository.findByProduitContainingIgnoreCase(produit);
    }

    @Transactional(readOnly = true)
    public NonConformite getById(Long id) {
        return nonConformiteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Non-conformite introuvable"));
    }

    @Transactional
    public NonConformite update(Long id, Map<String, Object> request) {
        NonConformite nonConformite = getById(id);

        String description = (String) request.get("description");
        String graviteStr = (String) request.get("gravite");
        String produit = (String) request.get("produit");
        Long retourId = request.get("retourId") != null ? ((Number) request.get("retourId")).longValue() : null;

        if (description != null && !description.isBlank()) {
            nonConformite.setDescription(description);
        }
        if (graviteStr != null) {
            nonConformite.setGravite(Gravite.valueOf(graviteStr.toUpperCase()));
        }
        if (produit != null && !produit.isBlank()) {
            nonConformite.setProduit(produit);
        }

        RetourProduit retour = null;
        if (retourId != null) {
            retour = retourProduitRepository.findById(retourId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Retour introuvable"));
        }
        nonConformite.setRetourProduit(retour);

        return nonConformiteRepository.save(nonConformite);
    }

    @Transactional
    public void delete(Long id) {
        NonConformite nonConformite = getById(id);
        nonConformiteRepository.delete(nonConformite);
    }
}