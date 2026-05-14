package com.retours.converter;

import com.retours.dto.UtilisateurDTO;
import com.retours.entity.Utilisateur;
import org.springframework.stereotype.Component;

@Component
public class UtilisateurConverter {

    public UtilisateurDTO toDto(Utilisateur u) {
        UtilisateurDTO dto = new UtilisateurDTO();

        dto.setId(u.getId());
        dto.setNom(u.getNom());
        dto.setEmail(u.getEmail());
        dto.setEnabled(u.isEnabled());
        dto.setRole(u.getRole());

        return dto;
    }

    public Utilisateur fromDto(UtilisateurDTO dto) {
        Utilisateur u = new Utilisateur();

        u.setId(dto.getId());
        u.setNom(dto.getNom());
        u.setEmail(dto.getEmail());
        u.setEnabled(dto.isEnabled());
        u.setRole(dto.getRole());

        return u;
    }
}
