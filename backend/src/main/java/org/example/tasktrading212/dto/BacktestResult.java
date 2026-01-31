package org.example.tasktrading212.dto;


import java.math.BigDecimal;

public record BacktestResult(
        String backtestId,
        int pricePoints,
        int buyCount,
        int sellCount,
        BigDecimal initialBalance,
        BigDecimal finalValue,
        BigDecimal profitLoss
) {}