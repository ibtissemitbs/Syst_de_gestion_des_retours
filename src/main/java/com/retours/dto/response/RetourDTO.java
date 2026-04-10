package com.retours.dto.response;

import com.retours.entity.RetourProduit;
import com.retours.enums.EtatTraitement;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RetourDTO {

    private Long id;
    private String produit;
    private String client;
    private String raison;
    private EtatTraitement etatTraitement;
    private LocalDateTime date;
    private boolean stockMisAJour;

    public static RetourDTO fromEntity(RetourProduit entity) {
        return RetourDTO.builder()
                .id(entity.getId())
                .produit(entity.getProduit())
                .client(entity.getClient())
                .raison(entity.getRaison())
                .etatTraitement(entity.getEtatTraitement())
                .date(entity.getDate())
                .stockMisAJour(entity.isStockMisAJour())
                .build();
    }
}
