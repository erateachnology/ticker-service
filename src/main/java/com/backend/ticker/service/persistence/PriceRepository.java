package com.backend.ticker.service.persistence;

import com.backend.ticker.models.entity.persistence.Price;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.Optional;

@Repository
public interface PriceRepository extends ListCrudRepository<Price, Long> {

    @Query(value = "SELECT p.formatted_price FROM Price p WHERE p.euc_id = :eucId AND p.fiat_symbol = :fiatSymbol AND (p.created_on >= :startTime) ORDER BY p.created_on ASC LIMIT 1", nativeQuery = true)
    Optional<String> findClosestPricesByTime(@Param("eucId") String eucId,
                                        @Param("fiatSymbol") String fiatSymbol,
                                        @Param("startTime") ZonedDateTime startTime);

}

