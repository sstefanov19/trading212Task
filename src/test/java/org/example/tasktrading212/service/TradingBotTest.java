package org.example.tasktrading212.service;

import org.example.tasktrading212.model.TradeSignal;
import org.example.tasktrading212.strategy.TradingStrategy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradingBotTest {

    @Mock
    private TradingStrategy strategy;

    @Mock
    private TradingService tradingService;

    private TradingBot tradingBot;

    private static final Long USER_ID = 1L;
    private static final BigDecimal PRICE = new BigDecimal("50000");
    private static final BigDecimal USDT_BALANCE = new BigDecimal("1000");
    private static final BigDecimal BTC_HOLDINGS = new BigDecimal("0.02");

    @BeforeEach
    void setUp() {
        tradingBot = new TradingBot(strategy, tradingService);
    }

    @AfterEach
    void tearDown() {
        tradingBot.shutdown();
    }

    @Test
    void start_shouldSetRunningTrue() {
        tradingBot.start(USER_ID);

        assertTrue(tradingBot.isRunning());
    }

    @Test
    void start_shouldThrowWhenUserIdIsNull() {
        assertThrows(IllegalArgumentException.class, () -> tradingBot.start(null));
    }

    @Test
    void stop_shouldSetRunningFalse() {
        tradingBot.start(USER_ID);
        tradingBot.stop();

        assertFalse(tradingBot.isRunning());
    }

    @Test
    void isRunning_shouldReturnFalseInitially() {
        assertFalse(tradingBot.isRunning());
    }

    @Test
    void onPriceUpdate_shouldExecuteBuyWhenStrategyReturnsBuy() throws InterruptedException {
        when(tradingService.getUsdtBalance(USER_ID)).thenReturn(USDT_BALANCE);
        when(tradingService.getBtcHoldings(USER_ID)).thenReturn(BTC_HOLDINGS);
        when(strategy.evaluate(PRICE, USDT_BALANCE, BTC_HOLDINGS)).thenReturn(TradeSignal.BUY);

        tradingBot.start(USER_ID);
        tradingBot.onPriceUpdate(PRICE);

        Thread.sleep(100); // Wait for async execution

        verify(tradingService).executeBuy(USER_ID, "BTCUSDT", PRICE);
    }

    @Test
    void onPriceUpdate_shouldExecuteSellWhenStrategyReturnsSell() throws InterruptedException {
        when(tradingService.getUsdtBalance(USER_ID)).thenReturn(USDT_BALANCE);
        when(tradingService.getBtcHoldings(USER_ID)).thenReturn(BTC_HOLDINGS);
        when(strategy.evaluate(PRICE, USDT_BALANCE, BTC_HOLDINGS)).thenReturn(TradeSignal.SELL);

        tradingBot.start(USER_ID);
        tradingBot.onPriceUpdate(PRICE);

        Thread.sleep(100);

        verify(tradingService).executeSell(USER_ID, "BTCUSDT", PRICE);
    }

    @Test
    void onPriceUpdate_shouldNotExecuteTradeWhenStrategyReturnsHold() throws InterruptedException {
        when(tradingService.getUsdtBalance(USER_ID)).thenReturn(USDT_BALANCE);
        when(tradingService.getBtcHoldings(USER_ID)).thenReturn(BTC_HOLDINGS);
        when(strategy.evaluate(PRICE, USDT_BALANCE, BTC_HOLDINGS)).thenReturn(TradeSignal.HOLD);

        tradingBot.start(USER_ID);
        tradingBot.onPriceUpdate(PRICE);

        Thread.sleep(100);

        verify(tradingService, never()).executeBuy(any(), any(), any());
        verify(tradingService, never()).executeSell(any(), any(), any());
    }

    @Test
    void onPriceUpdate_shouldNotProcessWhenNotRunning() throws InterruptedException {
        tradingBot.onPriceUpdate(PRICE);

        Thread.sleep(100);

        verify(strategy, never()).evaluate(any(), any(), any());
    }

    @Test
    void onPriceUpdate_shouldNotProcessAfterStop() throws InterruptedException {
        tradingBot.start(USER_ID);
        tradingBot.stop();
        tradingBot.onPriceUpdate(PRICE);

        Thread.sleep(100);

        verify(strategy, never()).evaluate(any(), any(), any());
    }
}