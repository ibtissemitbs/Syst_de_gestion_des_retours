package com.retours.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateRetourRequest {

    @NotBlank(message = "Le produit est obligatoire")
    private String produit;

    @NotBlank(message = "Le client est obligatoire")
    private String client;

    @NotBlank(message = "La raison est obligatoire")
    private String raison;
}
