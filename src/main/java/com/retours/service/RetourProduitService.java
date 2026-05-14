package com.retours.service;

import com.retours.entity.RetourProduit;
import com.retours.entity.Utilisateur;
import com.retours.enums.EtatTraitement;
import com.retours.enums.Role;
import com.retours.repository.HistoriqueRetourRepository;
import com.retours.repository.RetourProduitRepository;
import com.retours.repository.UtilisateurRepository;
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
public class RetourProduitService {

    private final RetourProduitRepository retourProduitRepository;
    private final HistoriqueRetourRepository historiqueRetourRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final HistoriqueRetourService historiqueRetourService;

    @Transactional
    public RetourProduit create(Map<String, Object> request) {
        String produit = (String) request.get("produit");
        String client = (String) request.get("client");
        String raison = (String) request.get("raison");

        if (produit == null || produit.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le produit est obligatoire");
        }
        if (client == null || client.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le client est obligatoire");
        }
        if (raison == null || raison.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La raison est obligatoire");
        }

        RetourProduit retour = RetourProduit.builder()
                .produit(produit)
                .client(client)
                .raison(raison)
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Retour introuvable"));
    }

    @Transactional
    public RetourProduit updateEtat(Long id, EtatTraitement etatTraitement, String employeEmail) {
        RetourProduit retour = getById(id);

        if (employeEmail == null || employeEmail.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Utilisateur authentifie introuvable");
        }

        Utilisateur employe = utilisateurRepository.findByEmail(employeEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));

        if (employe.getRole() != Role.QUALITE && employe.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Seuls les profils qualite ou admin peuvent traiter les retours");
        }

        retour.setEtatTraitement(etatTraitement);

        if (etatTraitement == EtatTraitement.VALIDE || etatTraitement == EtatTraitement.REJETE) {
            retour.setStockMisAJour(etatTraitement == EtatTraitement.VALIDE);
        }

        RetourProduit saved = retourProduitRepository.save(retour);
        historiqueRetourService.logAction(saved.getId(), "Etat passe a " + etatTraitement, employe.getId());
        return saved;
    }

    @Transactional
    public void delete(Long id) {
        RetourProduit retour = getById(id);
        if (historiqueRetourRepository.existsByRetourProduitId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Suppression impossible: un historique existe pour ce retour");
        }
        retourProduitRepository.delete(retour);
    }
}