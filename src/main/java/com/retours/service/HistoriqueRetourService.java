package com.retours.service;

import com.retours.entity.HistoriqueRetour;
import com.retours.entity.RetourProduit;
import com.retours.entity.Utilisateur;
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
public class HistoriqueRetourService {

    private final HistoriqueRetourRepository historiqueRetourRepository;
    private final RetourProduitRepository retourProduitRepository;
    private final UtilisateurRepository utilisateurRepository;

    @Transactional(readOnly = true)
    public List<HistoriqueRetour> listAll() {
        return historiqueRetourRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<HistoriqueRetour> listByRetour(Long retourId) {
        return historiqueRetourRepository.findByRetourProduitIdOrderByDateDesc(retourId);
    }

    @Transactional
    public void logAction(Long retourId, String action, Long employeId) {
        RetourProduit retour = retourProduitRepository.findById(retourId).orElse(null);
        if (retour == null) {
            return;
        }

        Utilisateur employe = employeId == null ? null : utilisateurRepository.findById(employeId).orElse(null);

        HistoriqueRetour historique = HistoriqueRetour.builder()
                .retourProduit(retour)
                .action(action)
                .employe(employe)
                .date(LocalDateTime.now())
                .build();
        historiqueRetourRepository.save(historique);
    }
}
