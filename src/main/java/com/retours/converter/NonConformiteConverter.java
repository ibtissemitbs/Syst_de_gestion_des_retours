package com.retours.converter;

import com.retours.dto.NonConformiteDTO;
import com.retours.entity.NonConformite;
import org.springframework.stereotype.Component;

@Component
public class NonConformiteConverter {

    public NonConformiteDTO toDto(NonConformite n) {
        NonConformiteDTO dto = new NonConformiteDTO();

        dto.setId(n.getId());
        dto.setDescription(n.getDescription());
        dto.setGravite(n.getGravite());
        dto.setProduit(n.getProduit());
        dto.setRetourProduitId(n.getRetourProduit() != null ? n.getRetourProduit().getId() : null);
        dto.setDate(n.getDate());

        return dto;
    }

    public NonConformite fromDto(NonConformiteDTO dto) {
        NonConformite n = new NonConformite();

        n.setId(dto.getId());
        n.setDescription(dto.getDescription());
        n.setGravite(dto.getGravite());
        n.setProduit(dto.getProduit());
        n.setDate(dto.getDate());

        return n;
    }
}
