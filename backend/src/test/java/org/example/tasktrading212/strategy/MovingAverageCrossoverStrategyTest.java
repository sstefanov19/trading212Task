package org.example.tasktrading212.strategy;

import org.example.tasktrading212.model.TradeSignal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MovingAverageCrossoverStrategyTest {

    private MovingAverageCrossoverStrategy strategy;

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal USDT_BALANCE = new BigDecimal("1000");
    private static final BigDecimal BTC_HOLDINGS = new BigDecimal("0.01");

    @BeforeEach
    void setUp() {
        strategy = new MovingAverageCrossoverStrategy(3, 5);
    }

    @Test
    void shouldHoldWhileCollectingData() {
        for (int i = 0; i < 4; i++) {
            TradeSignal signal = strategy.evaluate(new BigDecimal("100"), USDT_BALANCE, ZERO);
            assertEquals(TradeSignal.HOLD, signal);
        }
    }

    @Test
    void shouldBuyOnBullishCrossover() {
        strategy.evaluate(new BigDecimal("105"), USDT_BALANCE, ZERO);
        strategy.evaluate(new BigDecimal("104"), USDT_BALANCE, ZERO);
        strategy.evaluate(new BigDecimal("103"), USDT_BALANCE, ZERO);
        strategy.evaluate(new BigDecimal("102"), USDT_BALANCE, ZERO);
        strategy.evaluate(new BigDecimal("101"), USDT_BALANCE, ZERO);

        TradeSignal signal = strategy.evaluate(new BigDecimal("110"), USDT_BALANCE, ZERO);
        assertEquals(TradeSignal.BUY, signal);
    }

    @Test
    void shouldSellOnBearishCrossover() {
        strategy.evaluate(new BigDecimal("105"), USDT_BALANCE, ZERO);
        strategy.evaluate(new BigDecimal("104"), USDT_BALANCE, ZERO);
        strategy.evaluate(new BigDecimal("103"), USDT_BALANCE, ZERO);
        strategy.evaluate(new BigDecimal("102"), USDT_BALANCE, ZERO);
        strategy.evaluate(new BigDecimal("101"), USDT_BALANCE, ZERO);
        TradeSignal buySignal = strategy.evaluate(new BigDecimal("115"), USDT_BALANCE, ZERO);
        assertEquals(TradeSignal.BUY, buySignal);

        strategy.evaluate(new BigDecimal("110"), ZERO, BTC_HOLDINGS);
        strategy.evaluate(new BigDecimal("100"), ZERO, BTC_HOLDINGS);
        TradeSignal sellSignal = strategy.evaluate(new BigDecimal("90"), ZERO, BTC_HOLDINGS);
        assertEquals(TradeSignal.SELL, sellSignal);
    }

    @Test
    void shouldHoldWhenNoBalance() {
        strategy.evaluate(new BigDecimal("105"), USDT_BALANCE, ZERO);
        strategy.evaluate(new BigDecimal("104"), USDT_BALANCE, ZERO);
        strategy.evaluate(new BigDecimal("103"), USDT_BALANCE, ZERO);
        strategy.evaluate(new BigDecimal("102"), USDT_BALANCE, ZERO);
        strategy.evaluate(new BigDecimal("101"), USDT_BALANCE, ZERO);

        TradeSignal signal = strategy.evaluate(new BigDecimal("110"), ZERO, ZERO);
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

    @Test
    void shouldNotBuyTwiceWithoutSelling() {
        strategy.evaluate(new BigDecimal("105"), USDT_BALANCE, ZERO);
        strategy.evaluate(new BigDecimal("104"), USDT_BALANCE, ZERO);
        strategy.evaluate(new BigDecimal("103"), USDT_BALANCE, ZERO);
        strategy.evaluate(new BigDecimal("102"), USDT_BALANCE, ZERO);
        strategy.evaluate(new BigDecimal("101"), USDT_BALANCE, ZERO);
        TradeSignal firstBuy = strategy.evaluate(new BigDecimal("110"), USDT_BALANCE, ZERO);
        assertEquals(TradeSignal.BUY, firstBuy);

        TradeSignal secondSignal = strategy.evaluate(new BigDecimal("115"), USDT_BALANCE, ZERO);
        assertEquals(TradeSignal.HOLD, secondSignal);
    }
}