package com.retours.dto;

import com.retours.enums.Role;

import lombok.Data;


@Data
public class UtilisateurDTO {

    private Long id;
    private String nom;
    private String email;
    private boolean enabled;
    private Role role;
}
