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
    Mean Reversion Strategy
    - Price below mean by X% → Buy (oversold)
    - Price above mean by X% → Sell (overbought)
*/
@Component
public class MeanReversionStrategy implements TradingStrategy {

    private static final Logger logger = LoggerFactory.getLogger(MeanReversionStrategy.class);
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private final int period;
    private final BigDecimal buyThreshold;
    private final BigDecimal sellThreshold;

    private final LinkedList<BigDecimal> priceHistory = new LinkedList<>();

    public MeanReversionStrategy(
            @Value("${trading.mr.period:20}") int period,
            @Value("${trading.mr.buy-threshold:2.0}") BigDecimal buyThreshold,
            @Value("${trading.mr.sell-threshold:2.0}") BigDecimal sellThreshold) {
        this.period = period;
        this.buyThreshold = buyThreshold;
        this.sellThreshold = sellThreshold;
    }

    @Override
    public TradeSignal evaluate(BigDecimal currentPrice, BigDecimal usdtBalance, BigDecimal btcHoldings) {
        if (currentPrice == null) {
            return TradeSignal.HOLD;
        }

        priceHistory.addLast(currentPrice);

        if (priceHistory.size() > period) {
            priceHistory.removeFirst();
        }

        if (priceHistory.size() < period) {
            logger.debug("Collecting data: {}/{} prices", priceHistory.size(), period);
            return TradeSignal.HOLD;
        }

        BigDecimal mean = calculateMean();
        BigDecimal deviationPercent = calculateDeviationPercent(currentPrice, mean);

        logger.debug("Price: {}, Mean: {}, Deviation: {}%",
                currentPrice.setScale(2, RoundingMode.HALF_UP),
                mean.setScale(2, RoundingMode.HALF_UP),
                deviationPercent.setScale(2, RoundingMode.HALF_UP));

        boolean hasPosition = btcHoldings.compareTo(BigDecimal.ZERO) > 0;

        // Price is below mean by threshold% → BUY (oversold)
        if (deviationPercent.negate().compareTo(buyThreshold) >= 0 && !hasPosition) {
            if (usdtBalance.compareTo(BigDecimal.ZERO) > 0) {
                logger.info("BUY signal: Price {} is {}% below mean {}",
                        currentPrice.setScale(2, RoundingMode.HALF_UP),
                        deviationPercent.negate().setScale(2, RoundingMode.HALF_UP),
                        mean.setScale(2, RoundingMode.HALF_UP));
                return TradeSignal.BUY;
            }
        }

        // Price is above mean by threshold% → SELL (overbought)
        if (deviationPercent.compareTo(sellThreshold) >= 0 && hasPosition) {
            logger.info("SELL signal: Price {} is {}% above mean {}",
                    currentPrice.setScale(2, RoundingMode.HALF_UP),
                    deviationPercent.setScale(2, RoundingMode.HALF_UP),
                    mean.setScale(2, RoundingMode.HALF_UP));
            return TradeSignal.SELL;
        }

        return TradeSignal.HOLD;
    }

    private BigDecimal calculateMean() {
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal price : priceHistory) {
            sum = sum.add(price);
        }
        return sum.divide(BigDecimal.valueOf(period), 6, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateDeviationPercent(BigDecimal price, BigDecimal mean) {
        return price.subtract(mean)
                .divide(mean, 6, RoundingMode.HALF_UP)
                .multiply(HUNDRED);
    }

    @Override
    public void reset() {
        priceHistory.clear();
        logger.debug("Mean Reversion strategy reset");
    }
}