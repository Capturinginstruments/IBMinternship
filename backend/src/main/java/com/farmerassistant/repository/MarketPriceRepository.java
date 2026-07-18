package com.farmerassistant.repository;

import com.farmerassistant.model.MarketPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MarketPriceRepository extends JpaRepository<MarketPrice, Long> {

    @Query("SELECT m FROM MarketPrice m WHERE LOWER(m.commodity) = :commodity " +
           "AND LOWER(m.state) = :state ORDER BY m.tradeDate DESC")
    List<MarketPrice> findByCommodityAndState(
        @Param("commodity") String commodity, @Param("state") String state);

    @Query("""
        SELECT m FROM MarketPrice m WHERE
        (:commodity IS NULL OR LOWER(m.commodity) LIKE :commodity)
        AND (:state IS NULL OR LOWER(m.state) = :state)
        AND (:district IS NULL OR LOWER(m.district) = :district)
        ORDER BY m.tradeDate DESC
        """)
    List<MarketPrice> findByFilters(
        @Param("commodity") String commodity,
        @Param("state") String state,
        @Param("district") String district);

    @Query("SELECT m FROM MarketPrice m WHERE LOWER(m.commodity) = :commodity " +
           "AND LOWER(m.state) = :state AND m.tradeDate >= :fromDate ORDER BY m.tradeDate ASC")
    List<MarketPrice> findPriceTrend(
        @Param("commodity") String commodity,
        @Param("state") String state,
        @Param("fromDate") LocalDate fromDate);

    @Query("SELECT DISTINCT m.commodity FROM MarketPrice m ORDER BY m.commodity")
    List<String> findDistinctCommodities();

    @Query("SELECT DISTINCT m.state FROM MarketPrice m ORDER BY m.state")
    List<String> findDistinctStates();
}
