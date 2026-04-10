package com.retours.service;

import com.retours.dto.request.CreateRetourRequest;
import com.retours.entity.RetourProduit;
import com.retours.entity.Utilisateur;
import com.retours.enums.EtatTraitement;
import com.retours.enums.Role;
import com.retours.exception.ConflictException;
import com.retours.exception.ResourceNotFoundException;
import com.retours.repository.HistoriqueRetourRepository;
import com.retours.repository.RetourProduitRepository;
import com.retours.repository.UtilisateurRepository;
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
    private final UtilisateurRepository utilisateurRepository;
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

        if (employeId == null) {
            throw new IllegalArgumentException("L'identifiant de l'employe qualite est obligatoire pour traiter le retour");
        }

        Utilisateur employe = utilisateurRepository.findById(employeId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable avec id=" + employeId));

        if (employe.getRole() != Role.QUALITE) {
            throw new IllegalArgumentException("Seul le service qualite peut valider ou traiter les retours");
        }

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