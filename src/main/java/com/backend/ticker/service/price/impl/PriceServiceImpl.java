package com.backend.ticker.service.price.impl;

import com.backend.ticker.models.entity.persistence.Price;
import com.backend.ticker.service.price.PriceService;
import com.eucalyptuslabs.backend.common.model.ticker.response.CompleteRatesResponse;
import com.backend.ticker.service.persistence.PriceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

@Service
public class PriceServiceImpl implements PriceService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final PriceRepository priceRepository;

    public PriceServiceImpl(PriceRepository priceRepository) {
        this.priceRepository = priceRepository;
    }

    /**
     * Save historical prices
     *
     * @param cryptoResponseEntries
     * @param fiatSymbol
     * @param createdOn
     */
    @Override
    @Transactional
    public void saveHistoricalPrices(List<CompleteRatesResponse.CryptoResponseEntry> cryptoResponseEntries,
                                     String fiatSymbol, ZonedDateTime createdOn) {
        List<Price> prices = cryptoResponseEntries
                .stream()
                .map(cryptoResponseEntry -> Price.builder()
                        .eucId(cryptoResponseEntry.id())
                        .formattedPrice(cryptoResponseEntry.formattedPrice())
                        .createdOn(createdOn)
                        .fiatSymbol(fiatSymbol)
                        .build()).toList();
        priceRepository.saveAll(prices);
    }
}
