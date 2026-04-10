package com.retours.dto.response;

import com.retours.entity.NonConformite;
import com.retours.enums.Gravite;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NonConformiteDTO {

    private Long id;
    private String description;
    private Gravite gravite;
    private String produit;
    private Long retourId;
    private LocalDateTime date;

    public static NonConformiteDTO fromEntity(NonConformite entity) {
        return NonConformiteDTO.builder()
                .id(entity.getId())
                .description(entity.getDescription())
                .gravite(entity.getGravite())
                .produit(entity.getProduit())
                .retourId(entity.getRetour() != null ? entity.getRetour().getId() : null)
                .date(entity.getDate())
                .build();
    }
}
