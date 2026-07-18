package com.farmerassistant.repository;

import com.farmerassistant.model.SchemeBookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SchemeBookmarkRepository extends JpaRepository<SchemeBookmark, Long> {

    boolean existsByUserIdAndSchemeId(Long userId, Long schemeId);

    Page<SchemeBookmark> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Modifying
    @Query("DELETE FROM SchemeBookmark b WHERE b.user.id = :userId AND b.scheme.id = :schemeId")
    void deleteByUserIdAndSchemeId(@Param("userId") Long userId, @Param("schemeId") Long schemeId);
}
