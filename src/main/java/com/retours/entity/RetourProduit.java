package com.retours.entity;

import com.retours.enums.EtatTraitement;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "retours_produits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RetourProduit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String produit;

    @Column(nullable = false)
    private String client;

    @Column(nullable = false)
    private String raison;

    @Enumerated(EnumType.STRING)
    @Column(name = "etat_traitement", nullable = false)
    private EtatTraitement etatTraitement;

    @Column(nullable = false)
    private LocalDateTime date;

    @Column(name = "stock_mis_a_jour", nullable = false)
    private boolean stockMisAJour;
}
