package com.retours.service;

import com.retours.dto.request.CreateRetourRequest;
import com.retours.entity.RetourProduit;
import com.retours.enums.EtatTraitement;
import com.retours.exception.ConflictException;
import com.retours.exception.ResourceNotFoundException;
import com.retours.repository.HistoriqueRetourRepository;
import com.retours.repository.RetourProduitRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RetourProduitService {

    private final RetourProduitRepository retourProduitRepository;
    private final HistoriqueRetourRepository historiqueRetourRepository;
    private final HistoriqueRetourService historiqueRetourService;

    @Transactional
    public RetourProduit create(CreateRetourRequest request) {
        RetourProduit retour = RetourProduit.builder()
                .produit(request.getProduit())
                .client(request.getClient())
                .raison(request.getRaison())
                .etatTraitement(EtatTraitement.ENREGISTRE)
                .date(LocalDateTime.now())
                .stockMisAJour(false)
                .build();

        RetourProduit saved = retourProduitRepository.save(retour);
        historiqueRetourService.logAction(saved.getId(), "Retour enregistre", null);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<RetourProduit> listAll() {
        return retourProduitRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<RetourProduit> listByEtat(EtatTraitement etatTraitement) {
        return retourProduitRepository.findByEtatTraitement(etatTraitement);
    }

    @Transactional(readOnly = true)
    public RetourProduit getById(Long id) {
        return retourProduitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Retour introuvable avec id=" + id));
    }

    @Transactional
    public RetourProduit updateEtat(Long id, EtatTraitement etatTraitement, Long employeId) {
        RetourProduit retour = getById(id);
        retour.setEtatTraitement(etatTraitement);

        if (etatTraitement == EtatTraitement.VALIDE) {
            retour.setStockMisAJour(true);
        }

        RetourProduit saved = retourProduitRepository.save(retour);
        historiqueRetourService.logAction(saved.getId(), "Etat passe a " + etatTraitement, employeId);
        return saved;
    }

    @Transactional
    public void delete(Long id) {
        RetourProduit retour = getById(id);
        if (historiqueRetourRepository.existsByRetourId(id)) {
            throw new ConflictException("Suppression impossible: un historique existe pour ce retour (id=" + id + ")");
        }
        retourProduitRepository.delete(retour);
    }
}
