package org.example.tasktrading212.strategy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class StrategyFactory {

    @Value("${trading.ma.short-period:5}")
    private int shortPeriod;

    @Value("${trading.ma.long-period:20}")
    private int longPeriod;

    @Value("${trading.mr.period:20}")
    private int mrPeriod;

    @Value("${trading.mr.buy-threshold:2.0}")
    private BigDecimal mrBuyThreshold;

    @Value("${trading.mr.sell-threshold:2.0}")
    private BigDecimal mrSellThreshold;

    public TradingStrategy createStrategy(StrategyType type) {
        return switch (type) {
            case MOVING_AVERAGE -> new MovingAverageCrossoverStrategy(shortPeriod, longPeriod);
            case MEAN_REVERSION -> new MeanReversionStrategy(mrPeriod, mrBuyThreshold, mrSellThreshold);
        };
    }
}