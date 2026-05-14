package com.retours.repository;

import com.retours.entity.HistoriqueRetour;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoriqueRetourRepository extends JpaRepository<HistoriqueRetour, Long> {
    List<HistoriqueRetour> findByRetourProduitIdOrderByDateDesc(Long retourId);
    boolean existsByRetourProduitId(Long retourId);
    boolean existsByEmployeId(Long employeId);
}
