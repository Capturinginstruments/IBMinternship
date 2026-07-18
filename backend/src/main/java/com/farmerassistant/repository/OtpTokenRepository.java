package com.farmerassistant.repository;

import com.farmerassistant.model.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {

    @Query("SELECT o FROM OtpToken o WHERE o.user.id = :userId AND o.purpose = :purpose " +
           "AND o.isUsed = false AND o.expiresAt > :now ORDER BY o.createdAt DESC LIMIT 1")
    Optional<OtpToken> findValidOtp(@Param("userId") Long userId,
                                    @Param("purpose") OtpToken.OtpPurpose purpose,
                                    @Param("now") LocalDateTime now);

    @Modifying
    @Query("DELETE FROM OtpToken o WHERE o.user.id = :userId AND o.purpose = :purpose")
    void deleteByUserIdAndPurpose(@Param("userId") Long userId,
                                  @Param("purpose") OtpToken.OtpPurpose purpose);

    @Modifying
    @Query("DELETE FROM OtpToken o WHERE o.expiresAt < :now")
    void deleteExpiredOtps(@Param("now") LocalDateTime now);
}
