package org.example.tasktrading212.strategy;

import org.example.tasktrading212.model.TradeSignal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class ThresholdStrategy implements TradingStrategy {

    private static final Logger logger = LoggerFactory.getLogger(ThresholdStrategy.class);
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    @Value("${trading.buy-drop-percent}")
    private BigDecimal buyDropPercent;

    @Value("${trading.sell-gain-percent}")
    private BigDecimal sellGainPercent;

    @Value("${trading.stop-loss-percent}")
    private BigDecimal stopLossPercent;

    private volatile BigDecimal buyPrice;
    private volatile BigDecimal peakPriceAfterSell;
    private volatile boolean waitingToBuy = true;

    @Override
    public TradeSignal evaluate(BigDecimal currentPrice, BigDecimal usdtBalance, BigDecimal btcHoldings) {
        if (currentPrice == null) {
            return TradeSignal.HOLD;
        }

        boolean hasUsdt = usdtBalance.compareTo(BigDecimal.ZERO) > 0;
        boolean hasBtc = btcHoldings.compareTo(BigDecimal.ZERO) > 0;

        if (waitingToBuy && hasUsdt) {
            logger.info("Looking to buy!");
            return evaluateBuySignal(currentPrice);
        }

        if (hasBtc) {
            if (buyPrice == null) {
                buyPrice = currentPrice;
                waitingToBuy = false;
                logger.info("Existing BTC detected, setting buy price to current: {}", currentPrice);
            }
            logger.debug("Looking to sell!");
            return evaluateSellSignal(currentPrice);
        }

        return TradeSignal.HOLD;
    }

    private TradeSignal evaluateBuySignal(BigDecimal currentPrice) {
        if (peakPriceAfterSell == null) {
            return executeInitialBuy(currentPrice);
        }

        if (currentPrice.compareTo(peakPriceAfterSell) > 0) {
            peakPriceAfterSell = currentPrice;
            logger.debug("Peak updated to {}", peakPriceAfterSell);
            return TradeSignal.HOLD;
        }

        BigDecimal dropFromPeak = calculatePercentChange(peakPriceAfterSell, currentPrice);
        logger.debug("Waiting to buy - Peak: {}, Current: {}, Drop: {}%",
                peakPriceAfterSell, currentPrice, dropFromPeak.setScale(4, RoundingMode.HALF_UP));

        if (dropFromPeak.compareTo(buyDropPercent) >= 0) {
            return executeBuyOnDip(currentPrice, dropFromPeak);
        }

        return TradeSignal.HOLD;
    }

    private TradeSignal evaluateSellSignal(BigDecimal currentPrice) {
        BigDecimal percentChange = calculatePercentChange(buyPrice, currentPrice).negate();

        logger.debug("Holding BTC - BuyPrice: {}, Current: {}, Change: {}%",
                buyPrice, currentPrice, percentChange.negate().setScale(4, RoundingMode.HALF_UP));

        if (percentChange.negate().compareTo(stopLossPercent) >= 0) {
            return executeStopLoss(currentPrice, percentChange);
        }

        if (percentChange.negate().negate().compareTo(sellGainPercent) >= 0) {
            return executeTakeProfit(currentPrice, percentChange);
        }

        return TradeSignal.HOLD;
    }

    private TradeSignal executeInitialBuy(BigDecimal currentPrice) {
        peakPriceAfterSell = currentPrice;
        buyPrice = currentPrice;
        waitingToBuy = false;
        logger.info("Initial BUY at price {}", currentPrice);
        return TradeSignal.BUY;
    }

    private TradeSignal executeBuyOnDip(BigDecimal currentPrice, BigDecimal dropPercent) {
        logger.info("BUY signal: price {} dropped {}% from peak {}",
                currentPrice, dropPercent.setScale(2, RoundingMode.HALF_UP), peakPriceAfterSell);
        buyPrice = currentPrice;
        peakPriceAfterSell = null;
        waitingToBuy = false;
        return TradeSignal.BUY;
    }

    private TradeSignal executeStopLoss(BigDecimal currentPrice, BigDecimal percentChange) {
        logger.warn("STOP LOSS triggered: price {} is {}% from buy price {}",
                currentPrice, percentChange.setScale(2, RoundingMode.HALF_UP), buyPrice);
        resetToWaitingState(currentPrice);
        return TradeSignal.SELL;
    }

    private TradeSignal executeTakeProfit(BigDecimal currentPrice, BigDecimal percentChange) {
        logger.info("SELL signal: price {} is +{}% from buy price {}",
                currentPrice, percentChange.negate().setScale(2, RoundingMode.HALF_UP), buyPrice);
        resetToWaitingState(currentPrice);
        return TradeSignal.SELL;
    }

    private void resetToWaitingState(BigDecimal currentPrice) {
        peakPriceAfterSell = currentPrice;
        buyPrice = null;
        waitingToBuy = true;
    }

    private BigDecimal calculatePercentChange(BigDecimal fromPrice, BigDecimal toPrice) {
        return fromPrice.subtract(toPrice)
                .divide(fromPrice, 6, RoundingMode.HALF_UP)
                .multiply(HUNDRED);
    }
}