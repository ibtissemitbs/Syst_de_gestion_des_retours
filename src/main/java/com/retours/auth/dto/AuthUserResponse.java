package com.retours.auth.dto;

import com.retours.entity.Utilisateur;
import com.retours.enums.Role;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthUserResponse {

    private Long id;
    private String nom;
    private String email;
    private Role role;

    public static AuthUserResponse fromEntity(Utilisateur utilisateur) {
        return AuthUserResponse.builder()
                .id(utilisateur.getId())
                .nom(utilisateur.getNom())
                .email(utilisateur.getEmail())
                .role(utilisateur.getRole())
                .build();
    }
}
