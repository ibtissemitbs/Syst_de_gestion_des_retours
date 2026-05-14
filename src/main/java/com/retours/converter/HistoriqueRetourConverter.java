package com.retours.converter;

import com.retours.dto.HistoriqueRetourDTO;
import com.retours.entity.HistoriqueRetour;
import org.springframework.stereotype.Component;

@Component
public class HistoriqueRetourConverter {

    public HistoriqueRetourDTO toDto(HistoriqueRetour h) {
        HistoriqueRetourDTO dto = new HistoriqueRetourDTO();

        dto.setId(h.getId());
        dto.setRetourProduitId(h.getRetourProduit() != null ? h.getRetourProduit().getId() : null);
        dto.setAction(h.getAction());
        dto.setEmployeId(h.getEmploye() != null ? h.getEmploye().getId() : null);
        dto.setDate(h.getDate());

        return dto;
    }

    public HistoriqueRetour fromDto(HistoriqueRetourDTO dto) {
        HistoriqueRetour h = new HistoriqueRetour();

        h.setId(dto.getId());
        h.setAction(dto.getAction());
        h.setDate(dto.getDate());

        return h;
    }
}
