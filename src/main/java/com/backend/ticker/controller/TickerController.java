package com.backend.ticker.controller;

import com.backend.ticker.service.CompleteRatesServiceV1;
import com.backend.ticker.service.CompleteRatesServiceV2;
import com.eucalyptuslabs.backend.common.model.exception.DataNotAvailableException;
import com.eucalyptuslabs.backend.common.model.ticker.response.CompleteRatesResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "${cors.allowed-origins}")
public class TickerController {
    private final CompleteRatesServiceV1 completeRatesServiceV1;

    private final CompleteRatesServiceV2 completeRatesServiceV2;

    public TickerController(CompleteRatesServiceV1 completeRatesServiceV1,
                            CompleteRatesServiceV2 completeRatesServiceV2) {
        this.completeRatesServiceV1 = completeRatesServiceV1;
        this.completeRatesServiceV2 = completeRatesServiceV2;
    }

    @Deprecated
    @GetMapping({"ticker/rates/complete/fiat/{fiatSymbol}", "ticker/v1/rates/complete/fiat/{fiatSymbol}"})
    public CompleteRatesResponse getCompleteRatesForCoinGecko(
            @PathVariable(value = "fiatSymbol") String fiatSymbol
    ) throws DataNotAvailableException {
        return completeRatesServiceV1.getCompleteRates(fiatSymbol.toUpperCase());
    }

    @GetMapping("/ticker/v2/rates/all/{fiatSymbol}")
    public CompleteRatesResponse getCompleteRatesForCoinGeckoAndKdSwap(
            @PathVariable(value = "fiatSymbol") String fiatSymbol
    ) throws DataNotAvailableException {
        return completeRatesServiceV2.getCompleteRates(fiatSymbol.toUpperCase());
    }

}
