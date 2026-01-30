package org.example.tasktrading212.strategy;

import org.example.tasktrading212.model.TradeSignal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MeanReversionStrategyTest {

    private MeanReversionStrategy strategy;

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal USDT_BALANCE = new BigDecimal("1000");
    private static final BigDecimal BTC_HOLDINGS = new BigDecimal("0.01");

    @BeforeEach
    void setUp() {
        strategy = new MeanReversionStrategy(5, new BigDecimal("2.0"), new BigDecimal("2.0"));
    }

    @Test
    void shouldHoldWhileCollectingData() {
        for (int i = 0; i < 4; i++) {
            TradeSignal signal = strategy.evaluate(new BigDecimal("100"), USDT_BALANCE, ZERO);
            assertEquals(TradeSignal.HOLD, signal);
        }
    }

    @Test
    void shouldBuyWhenPriceBelowMean() {
        for (int i = 0; i < 5; i++) {
            strategy.evaluate(new BigDecimal("100"), USDT_BALANCE, ZERO);
        }
        strategy.reset();

        strategy.evaluate(new BigDecimal("100"), USDT_BALANCE, ZERO);
        strategy.evaluate(new BigDecimal("100"), USDT_BALANCE, ZERO);
        strategy.evaluate(new BigDecimal("100"), USDT_BALANCE, ZERO);
        strategy.evaluate(new BigDecimal("100"), USDT_BALANCE, ZERO);

        TradeSignal signal = strategy.evaluate(new BigDecimal("97"), USDT_BALANCE, ZERO);
        assertEquals(TradeSignal.BUY, signal);
    }

    @Test
    void shouldSellWhenPriceAboveMean() {
        strategy.evaluate(new BigDecimal("100"), USDT_BALANCE, ZERO);
        strategy.evaluate(new BigDecimal("100"), USDT_BALANCE, ZERO);
        strategy.evaluate(new BigDecimal("100"), USDT_BALANCE, ZERO);
        strategy.evaluate(new BigDecimal("100"), USDT_BALANCE, ZERO);
        strategy.evaluate(new BigDecimal("97"), USDT_BALANCE, ZERO); // BUY triggered

        TradeSignal signal = strategy.evaluate(new BigDecimal("103"), ZERO, BTC_HOLDINGS);
        assertEquals(TradeSignal.SELL, signal);
    }

    @Test
    void shouldHoldWhenPriceNearMean() {
        for (int i = 0; i < 5; i++) {
            strategy.evaluate(new BigDecimal("100"), USDT_BALANCE, ZERO);
        }

        TradeSignal signal = strategy.evaluate(new BigDecimal("99"), USDT_BALANCE, ZERO);
        assertEquals(TradeSignal.HOLD, signal);
    }

    @Test
    void shouldHoldWhenNoBalance() {
        for (int i = 0; i < 5; i++) {
            strategy.evaluate(new BigDecimal("100"), USDT_BALANCE, ZERO);
        }

        TradeSignal signal = strategy.evaluate(new BigDecimal("97"), ZERO, ZERO);
        assertEquals(TradeSignal.HOLD, signal);
    }

    @Test
    void shouldResetState() {
        for (int i = 0; i < 5; i++) {
            strategy.evaluate(new BigDecimal("100"), USDT_BALANCE, ZERO);
        }

        strategy.reset();

        TradeSignal signal = strategy.evaluate(new BigDecimal("100"), USDT_BALANCE, ZERO);
        assertEquals(TradeSignal.HOLD, signal);
    }

    @Test
    void shouldReturnHoldWhenPriceIsNull() {
        TradeSignal signal = strategy.evaluate(null, USDT_BALANCE, ZERO);
        assertEquals(TradeSignal.HOLD, signal);
    }
}