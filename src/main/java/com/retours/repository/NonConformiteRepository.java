package com.retours.repository;

import com.retours.entity.NonConformite;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NonConformiteRepository extends JpaRepository<NonConformite, Long> {
    List<NonConformite> findByProduitContainingIgnoreCase(String produit);
}
