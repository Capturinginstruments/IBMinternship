package com.farmerassistant.repository;

import com.farmerassistant.model.CropRecommendation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CropRecommendationRepository extends JpaRepository<CropRecommendation, Long> {
    Page<CropRecommendation> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    List<CropRecommendation> findTop5ByUserIdOrderByCreatedAtDesc(Long userId);
}
