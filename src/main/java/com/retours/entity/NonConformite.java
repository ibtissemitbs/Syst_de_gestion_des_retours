package com.retours.entity;

import com.retours.enums.Gravite;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "non_conformites")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NonConformite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gravite gravite;

    @Column(nullable = false)
    private String produit;

    @ManyToOne
    @JoinColumn(name = "retour_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private RetourProduit retourProduit;

    @Column(nullable = false)
    private LocalDateTime date;
}
