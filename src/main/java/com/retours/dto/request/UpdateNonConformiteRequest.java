package com.retours.dto.request;

import com.retours.enums.Gravite;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateNonConformiteRequest {

    @NotBlank(message = "La description est obligatoire")
    private String description;

    @NotNull(message = "La gravite est obligatoire")
    private Gravite gravite;

    @NotBlank(message = "Le produit est obligatoire")
    private String produit;

    private Long retourId;
}
