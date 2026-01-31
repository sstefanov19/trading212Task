package org.example.tasktrading212.dto;

import java.math.BigDecimal;

public record PortfolioResponse(
        Long userId,
        BigDecimal usdtBalance,
        BigDecimal btcHoldings,
        BigDecimal initialBalance,
        BigDecimal currentValue,
        BigDecimal profitLoss,
        BigDecimal profitLossPercent) {
}
