package com.retours.dto;

import java.time.LocalDateTime;

import lombok.Data;


@Data
public class HistoriqueRetourDTO {

    private Long id;
    private Long retourProduitId;
    private String action;
    private Long employeId;
    private LocalDateTime date;
}
