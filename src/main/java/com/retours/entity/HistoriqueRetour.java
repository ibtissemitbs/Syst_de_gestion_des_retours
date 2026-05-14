package com.retours.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;


@Entity
@Table(name = "historique_retours")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoriqueRetour {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "retour_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private RetourProduit retourProduit;

    @Column(nullable = false)
    private String action;

    @ManyToOne
    @JoinColumn(name = "employe_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Utilisateur employe;

    @Column(nullable = false)
    private LocalDateTime date;
}
