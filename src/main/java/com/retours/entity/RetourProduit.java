package com.retours.entity;

import com.retours.enums.EtatTraitement;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import lombok.NoArgsConstructor;
import lombok.ToString;


@Entity
@Table(name = "retours_produits")
@Data
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

    @ManyToOne
    @JoinColumn(name = "utilisateur_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Utilisateur utilisateur;

    @OneToMany(mappedBy = "retourProduit")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<NonConformite> nonConformites = new ArrayList<>();

    @OneToMany(mappedBy = "retourProduit")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<HistoriqueRetour> historiquesRetours = new ArrayList<>();
}
