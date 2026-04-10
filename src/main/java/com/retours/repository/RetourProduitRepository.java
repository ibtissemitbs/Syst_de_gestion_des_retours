package com.retours.repository;

import com.retours.entity.RetourProduit;
import com.retours.enums.EtatTraitement;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RetourProduitRepository extends JpaRepository<RetourProduit, Long> {
    List<RetourProduit> findByEtatTraitement(EtatTraitement etatTraitement);
}
