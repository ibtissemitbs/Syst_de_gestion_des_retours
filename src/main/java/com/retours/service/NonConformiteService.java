package com.retours.service;

import com.retours.dto.request.CreateNonConformiteRequest;
import com.retours.dto.request.UpdateNonConformiteRequest;
import com.retours.entity.NonConformite;
import com.retours.entity.RetourProduit;
import com.retours.exception.ResourceNotFoundException;
import com.retours.repository.NonConformiteRepository;
import com.retours.repository.RetourProduitRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NonConformiteService {

    private final NonConformiteRepository nonConformiteRepository;
    private final RetourProduitRepository retourProduitRepository;

    @Transactional
    public NonConformite create(CreateNonConformiteRequest request) {
        RetourProduit retour = null;
        if (request.getRetourId() != null) {
            retour = retourProduitRepository.findById(request.getRetourId())
                    .orElseThrow(() -> new ResourceNotFoundException("Retour introuvable avec id=" + request.getRetourId()));
        }

        NonConformite nonConformite = NonConformite.builder()
                .description(request.getDescription())
                .gravite(request.getGravite())
                .produit(request.getProduit())
                .retour(retour)
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
                .orElseThrow(() -> new ResourceNotFoundException("Non-conformite introuvable avec id=" + id));
    }

    @Transactional
    public NonConformite update(Long id, UpdateNonConformiteRequest request) {
        NonConformite nonConformite = getById(id);

        RetourProduit retour = null;
        if (request.getRetourId() != null) {
            retour = retourProduitRepository.findById(request.getRetourId())
                    .orElseThrow(() -> new ResourceNotFoundException("Retour introuvable avec id=" + request.getRetourId()));
        }

        nonConformite.setDescription(request.getDescription());
        nonConformite.setGravite(request.getGravite());
        nonConformite.setProduit(request.getProduit());
        nonConformite.setRetour(retour);

        return nonConformiteRepository.save(nonConformite);
    }

    @Transactional
    public void delete(Long id) {
        NonConformite nonConformite = getById(id);
        nonConformiteRepository.delete(nonConformite);
    }
}