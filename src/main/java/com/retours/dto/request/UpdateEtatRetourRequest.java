package com.retours.dto.request;

import com.retours.enums.EtatTraitement;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateEtatRetourRequest {

    @NotNull(message = "L'etat est obligatoire")
    private EtatTraitement etatTraitement;
}
