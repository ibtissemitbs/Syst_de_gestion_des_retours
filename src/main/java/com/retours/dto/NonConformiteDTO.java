package com.retours.dto;

import com.retours.enums.Gravite;
import java.time.LocalDateTime;

import lombok.Data;


@Data
public class NonConformiteDTO {

    private Long id;
    private String description;
    private Gravite gravite;
    private String produit;
    private Long retourProduitId;
    private LocalDateTime date;
}
