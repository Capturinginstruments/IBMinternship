package com.farmerassistant.repository;

import com.farmerassistant.model.DiseaseReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiseaseReportRepository extends JpaRepository<DiseaseReport, Long> {
    Page<DiseaseReport> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    long countByUserIdAndIsResolvedFalse(Long userId);
}
