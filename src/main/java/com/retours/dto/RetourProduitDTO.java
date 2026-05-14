package com.retours.dto;

import com.retours.enums.EtatTraitement;
import java.time.LocalDateTime;

import lombok.Data;


@Data
public class RetourProduitDTO {

    private Long id;
    private String produit;
    private String client;
    private String raison;
    private EtatTraitement etatTraitement;
    private LocalDateTime date;
    private boolean stockMisAJour;
    private Long utilisateurId;
}
