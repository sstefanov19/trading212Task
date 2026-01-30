package org.example.tasktrading212.strategy;

import org.example.tasktrading212.model.TradeSignal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;

/*
    MA = Moving Average
    Short MA ↑ above Long MA → Buy
    Short MA ↓ below Long MA → Sell
*/
@Component
public class MovingAverageCrossoverStrategy implements TradingStrategy {

    private static final Logger logger = LoggerFactory.getLogger(MovingAverageCrossoverStrategy.class);

    private final int shortPeriod;
    private final int longPeriod;

    private final LinkedList<BigDecimal> priceHistory = new LinkedList<>();
    private boolean wasShortAboveLong = false;

    public MovingAverageCrossoverStrategy(
            @Value("${trading.ma.short-period:5}") int shortPeriod,
            @Value("${trading.ma.long-period:20}") int longPeriod) {
        this.shortPeriod = shortPeriod;
        this.longPeriod = longPeriod;
    }

    @Override
    public TradeSignal evaluate(BigDecimal currentPrice, BigDecimal usdtBalance, BigDecimal btcHoldings) {
        if (currentPrice == null) {
            return TradeSignal.HOLD;
        }

        priceHistory.addLast(currentPrice);

        if (priceHistory.size() > longPeriod) {
            priceHistory.removeFirst();
        }

        if (priceHistory.size() < longPeriod) {
            logger.debug("Collecting data: {}/{} prices", priceHistory.size(), longPeriod);
            return TradeSignal.HOLD;
        }

        BigDecimal shortMA = calculateMA(shortPeriod);
        BigDecimal longMA = calculateMA(longPeriod);

        boolean isShortAboveLong = shortMA.compareTo(longMA) > 0;

        logger.debug("Short MA: {}, Long MA: {}, Short > Long: {}",
                shortMA.setScale(2, RoundingMode.HALF_UP),
                longMA.setScale(2, RoundingMode.HALF_UP),
                isShortAboveLong);

        boolean hasPosition = btcHoldings.compareTo(BigDecimal.ZERO) > 0;
        TradeSignal signal = TradeSignal.HOLD;

        // Bullish crossover: short MA crosses above long MA
        if (isShortAboveLong && !wasShortAboveLong && !hasPosition) {
            if (usdtBalance.compareTo(BigDecimal.ZERO) > 0) {
                logger.info("BUY signal: Short MA ({}) crossed above Long MA ({})",
                        shortMA.setScale(2, RoundingMode.HALF_UP),
                        longMA.setScale(2, RoundingMode.HALF_UP));
                signal = TradeSignal.BUY;
            }
        }

        // Bearish crossover: short MA crosses below long MA
        if (!isShortAboveLong && wasShortAboveLong && hasPosition) {
            logger.info("SELL signal: Short MA ({}) crossed below Long MA ({})",
                    shortMA.setScale(2, RoundingMode.HALF_UP),
                    longMA.setScale(2, RoundingMode.HALF_UP));
            signal = TradeSignal.SELL;
        }

        wasShortAboveLong = isShortAboveLong;
        return signal;
    }

    private BigDecimal calculateMA(int period) {
        int startIndex = priceHistory.size() - period;
        BigDecimal sum = BigDecimal.ZERO;

        for (int i = startIndex; i < priceHistory.size(); i++) {
            sum = sum.add(priceHistory.get(i));
        }

        return sum.divide(BigDecimal.valueOf(period), 6, RoundingMode.HALF_UP);
    }

    @Override
    public void reset() {
        priceHistory.clear();
        wasShortAboveLong = false;
        logger.debug("Moving Average strategy reset");
    }
}