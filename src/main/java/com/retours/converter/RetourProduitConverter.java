package com.retours.converter;

import com.retours.dto.RetourProduitDTO;
import com.retours.entity.RetourProduit;
import org.springframework.stereotype.Component;

@Component
public class RetourProduitConverter {

    public RetourProduitDTO toDto(RetourProduit r) {
        RetourProduitDTO dto = new RetourProduitDTO();

        dto.setId(r.getId());
        dto.setProduit(r.getProduit());
        dto.setClient(r.getClient());
        dto.setRaison(r.getRaison());
        dto.setEtatTraitement(r.getEtatTraitement());
        dto.setDate(r.getDate());
        dto.setStockMisAJour(r.isStockMisAJour());
        dto.setUtilisateurId(r.getUtilisateur() != null ? r.getUtilisateur().getId() : null);

        return dto;
    }

    public RetourProduit fromDto(RetourProduitDTO dto) {
        RetourProduit r = new RetourProduit();

        r.setId(dto.getId());
        r.setProduit(dto.getProduit());
        r.setClient(dto.getClient());
        r.setRaison(dto.getRaison());
        r.setEtatTraitement(dto.getEtatTraitement());
        r.setDate(dto.getDate());
        r.setStockMisAJour(dto.isStockMisAJour());

        return r;
    }
}
