package org.example.tasktrading212.strategy;

import org.example.tasktrading212.model.TradeSignal;

import java.math.BigDecimal;

public interface TradingStrategy {

    TradeSignal evaluate(BigDecimal currentPrice, BigDecimal usdtBalance, BigDecimal btcHoldings);
}