package com.retours.repository;

import com.retours.entity.HistoriqueRetour;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoriqueRetourRepository extends JpaRepository<HistoriqueRetour, Long> {
    List<HistoriqueRetour> findByRetourIdOrderByDateDesc(Long retourId);
    boolean existsByRetourId(Long retourId);
    boolean existsByEmployeId(Long employeId);
}
