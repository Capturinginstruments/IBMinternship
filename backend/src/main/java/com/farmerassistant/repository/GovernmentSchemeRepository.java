package com.farmerassistant.repository;

import com.farmerassistant.model.GovernmentScheme;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GovernmentSchemeRepository extends JpaRepository<GovernmentScheme, Long> {

    Page<GovernmentScheme> findByIsActiveTrue(Pageable pageable);

    @Query("""
        SELECT s FROM GovernmentScheme s WHERE s.isActive = true
        AND (:category IS NULL OR s.category = :category)
        AND (:state IS NULL OR s.applicableStates LIKE %:state% OR s.applicableStates = 'All India')
        AND (:crop IS NULL OR s.applicableCrops LIKE %:crop% OR s.applicableCrops = 'All crops')
        AND (:minLand IS NULL OR s.maxLandAcres >= :minLand)
        AND (:maxLand IS NULL OR s.minLandAcres <= :maxLand)
        """)
    Page<GovernmentScheme> findByFilters(
        @Param("category") GovernmentScheme.SchemeCategory category,
        @Param("state") String state,
        @Param("crop") String crop,
        @Param("minLand") Double minLand,
        @Param("maxLand") Double maxLand,
        Pageable pageable);

    @Query("""
        SELECT s FROM GovernmentScheme s WHERE s.isActive = true AND (
        LOWER(s.title) LIKE :keyword OR
        LOWER(s.description) LIKE :keyword OR
        LOWER(s.benefits) LIKE :keyword OR
        LOWER(s.eligibility) LIKE :keyword
        )""")
    Page<GovernmentScheme> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
