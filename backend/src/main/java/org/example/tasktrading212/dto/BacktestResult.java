package org.example.tasktrading212.dto;


import java.math.BigDecimal;
import java.util.List;

public record BacktestResult(
        String backtestId,
        int pricePoints,
        int buyCount,
        int sellCount,
        BigDecimal initialBalance,
        BigDecimal finalValue,
        BigDecimal profitLoss,
        List<PricePoint> portfolioOverTime
) {}