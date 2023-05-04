package com.eucalyptuslabs.backend.common.model.coinapi;

public record CoinApiTransactionNotification(
    String blockchainId,
    String eucId,
    String transactionChainId,
    String relevantAddress,
    String chainIdFrom,
    String chainIdTo,
    String transactionId,
    String fee,
    CoinApiTransactionStatus status,
    String addressFrom,
    String addressTo,
    String assetId,
    String assetName,
    String symbol,
    String amount,
    String blockHash,
    String blockNumber,
    Long timestamp) {}
