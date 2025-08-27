package org.stefan.backend.service;

import org.springframework.stereotype.Service;
import org.stefan.backend.model.TradeType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;


@Service
public class StrategyEvaluator {
    static final int PERIOD = 14;
    private static final BigDecimal RSI_OVERBOUGHT = new BigDecimal("70");
    private static final BigDecimal RSI_OVERSOLD = new BigDecimal("30");

    public TradeType evaluate(List<BigDecimal> historicalPrices, BigDecimal currentPrice) {
        if (historicalPrices.size() < PERIOD + 1) {
            System.out.println("Not enough historical data");
            return TradeType.HOLD;
        }

        List<BigDecimal> prices = new ArrayList<>(historicalPrices);
        prices.add(currentPrice);

        if (prices.size() > PERIOD + 1) {
            prices = prices.subList(prices.size() - (PERIOD + 1), prices.size());
        }


        BigDecimal rsi = calculateRSI(prices);
        System.out.println("Price: " + currentPrice + ", RSI: " + rsi);

        if (rsi.compareTo(RSI_OVERSOLD) <= 0) {
            return TradeType.BUY;
        } else if (rsi.compareTo(RSI_OVERBOUGHT) >= 0) {
            return TradeType.SELL;
        }

        return TradeType.HOLD;
    }

    private BigDecimal calculateRSI(List<BigDecimal> prices) {
        List<BigDecimal> gains = new ArrayList<>();
        List<BigDecimal> losses = new ArrayList<>();

        // Calculate price changes
        for (int i = 1; i < prices.size(); i++) {
            BigDecimal change = prices.get(i).subtract(prices.get(i - 1));
            if (change.compareTo(BigDecimal.ZERO) > 0) {
                gains.add(change);
                losses.add(BigDecimal.ZERO);
            } else {
                gains.add(BigDecimal.ZERO);
                losses.add(change.abs());
            }
        }

        // Calculate average gain and loss
        BigDecimal avgGain = calculateAverage(gains.subList(gains.size() - PERIOD, gains.size()));
        BigDecimal avgLoss = calculateAverage(losses.subList(losses.size() - PERIOD, losses.size()));

        if (avgLoss.compareTo(BigDecimal.ZERO) == 0) {
            return new BigDecimal("100");
        }

        // Calculate RS and RSI
        BigDecimal rs = avgGain.divide(avgLoss, 4, RoundingMode.HALF_UP);
        return new BigDecimal("100").subtract(
                new BigDecimal("100").divide(BigDecimal.ONE.add(rs), 2, RoundingMode.HALF_UP)
        );
    }

    private BigDecimal calculateAverage(List<BigDecimal> numbers) {
        if (numbers.isEmpty()) return BigDecimal.ZERO;

        BigDecimal sum = numbers.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(new BigDecimal(numbers.size()), 4, RoundingMode.HALF_UP);
    }
}