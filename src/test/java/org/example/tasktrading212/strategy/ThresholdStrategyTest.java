package org.example.tasktrading212.strategy;

import org.example.tasktrading212.model.TradeSignal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ThresholdStrategyTest {

    private ThresholdStrategy strategy;

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal USDT_BALANCE = new BigDecimal("1000");
    private static final BigDecimal BTC_HOLDINGS = new BigDecimal("0.01");

    @BeforeEach
    void setUp() throws Exception {
        strategy = new ThresholdStrategy();
        setField("buyDropPercent", new BigDecimal("0.5"));
        setField("sellGainPercent", new BigDecimal("1.0"));
        setField("stopLossPercent", new BigDecimal("2.0"));
    }

    private void setField(String fieldName, Object value) throws Exception {
        Field field = ThresholdStrategy.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(strategy, value);
    }

    private Object getField(String fieldName) throws Exception {
        Field field = ThresholdStrategy.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(strategy);
    }

    @Test
    void shouldBuyOnFirstPriceUpdate() {
        BigDecimal price = new BigDecimal("100000");

        TradeSignal signal = strategy.evaluate(price, USDT_BALANCE, ZERO);

        assertEquals(TradeSignal.BUY, signal);
    }

    @Test
    void shouldHoldWhenNoUsdtBalance() {
        BigDecimal price = new BigDecimal("100000");

        TradeSignal signal = strategy.evaluate(price, ZERO, ZERO);

        assertEquals(TradeSignal.HOLD, signal);
    }

    @Test
    void shouldHoldWhenPriceIsNull() {
        TradeSignal signal = strategy.evaluate(null, USDT_BALANCE, ZERO);

        assertEquals(TradeSignal.HOLD, signal);
    }

    @Test
    void shouldBuyOnDip() throws Exception {
        BigDecimal peakPrice = new BigDecimal("100000");
        setField("peakPriceAfterSell", peakPrice);
        setField("waitingToBuy", true);

        BigDecimal droppedPrice = new BigDecimal("99500"); // 0.5% drop

        TradeSignal signal = strategy.evaluate(droppedPrice, USDT_BALANCE, ZERO);

        assertEquals(TradeSignal.BUY, signal);
    }

    @Test
    void shouldHoldWhenDropBelowThreshold() throws Exception {
        BigDecimal peakPrice = new BigDecimal("100000");
        setField("peakPriceAfterSell", peakPrice);
        setField("waitingToBuy", true);

        BigDecimal slightDrop = new BigDecimal("99700"); // 0.3% drop

        TradeSignal signal = strategy.evaluate(slightDrop, USDT_BALANCE, ZERO);

        assertEquals(TradeSignal.HOLD, signal);
    }

    @Test
    void shouldUpdatePeakWhenPriceRises() throws Exception {
        BigDecimal initialPeak = new BigDecimal("100000");
        setField("peakPriceAfterSell", initialPeak);
        setField("waitingToBuy", true);

        BigDecimal higherPrice = new BigDecimal("101000");

        TradeSignal signal = strategy.evaluate(higherPrice, USDT_BALANCE, ZERO);

        assertEquals(TradeSignal.HOLD, signal);
        assertEquals(higherPrice, getField("peakPriceAfterSell"));
    }

    @Test
    void shouldSellOnProfit() throws Exception {
        BigDecimal buyPrice = new BigDecimal("100000");
        setField("buyPrice", buyPrice);
        setField("waitingToBuy", false);

        BigDecimal profitPrice = new BigDecimal("101000"); // 1% gain

        TradeSignal signal = strategy.evaluate(profitPrice, ZERO, BTC_HOLDINGS);

        assertEquals(TradeSignal.SELL, signal);
    }

    @Test
    void shouldHoldWhenProfitBelowThreshold() throws Exception {
        BigDecimal buyPrice = new BigDecimal("100000");
        setField("buyPrice", buyPrice);
        setField("waitingToBuy", false);

        BigDecimal smallProfit = new BigDecimal("100500"); // 0.5% gain

        TradeSignal signal = strategy.evaluate(smallProfit, ZERO, BTC_HOLDINGS);

        assertEquals(TradeSignal.HOLD, signal);
    }

    @Test
    void shouldTriggerStopLoss() throws Exception {
        BigDecimal buyPrice = new BigDecimal("100000");
        setField("buyPrice", buyPrice);
        setField("waitingToBuy", false);

        BigDecimal lossPrice = new BigDecimal("98000"); // 2% loss

        TradeSignal signal = strategy.evaluate(lossPrice, ZERO, BTC_HOLDINGS);

        assertEquals(TradeSignal.SELL, signal);
    }

    @Test
    void shouldResetToWaitingStateAfterSell() throws Exception {
        BigDecimal buyPrice = new BigDecimal("100000");
        setField("buyPrice", buyPrice);
        setField("waitingToBuy", false);

        BigDecimal profitPrice = new BigDecimal("101000");
        strategy.evaluate(profitPrice, ZERO, BTC_HOLDINGS);

        assertTrue((Boolean) getField("waitingToBuy"));
        assertNull(getField("buyPrice"));
        assertEquals(profitPrice, getField("peakPriceAfterSell"));
    }

    @Test
    void shouldSetBuyPriceAfterBuying() throws Exception {
        BigDecimal price = new BigDecimal("100000");

        strategy.evaluate(price, USDT_BALANCE, ZERO);

        assertEquals(price, getField("buyPrice"));
        assertFalse((Boolean) getField("waitingToBuy"));
    }

    @Test
    void shouldInitializeBuyPriceForExistingHoldings() throws Exception {
        setField("waitingToBuy", false);
        setField("buyPrice", null);

        BigDecimal currentPrice = new BigDecimal("100000");

        strategy.evaluate(currentPrice, ZERO, BTC_HOLDINGS);

        assertEquals(currentPrice, getField("buyPrice"));
    }
}