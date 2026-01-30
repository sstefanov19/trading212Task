package org.example.tasktrading212.service;

import org.example.tasktrading212.model.TradeSignal;
import org.example.tasktrading212.strategy.StrategyFactory;
import org.example.tasktrading212.strategy.StrategyType;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradingBotTest {

    @Mock
    private TradingService tradingService;

    @Mock
    private StrategyFactory strategyFactory;

    @Mock
    private TradingStrategy strategy;

    private TradingBot tradingBot;

    private static final Long USER_ID = 1L;
    private static final StrategyType STRATEGY_TYPE = StrategyType.MEAN_REVERSION;
    private static final BigDecimal PRICE = new BigDecimal("50000");
    private static final BigDecimal USDT_BALANCE = new BigDecimal("1000");
    private static final BigDecimal BTC_HOLDINGS = new BigDecimal("0.02");

    @BeforeEach
    void setUp() {
        tradingBot = new TradingBot(tradingService, strategyFactory, 3L);
    }

    @AfterEach
    void tearDown() {
        tradingBot.shutdown();
    }

    @Test
    void start_shouldSetRunningTrue() {
        when(strategyFactory.createStrategy(STRATEGY_TYPE)).thenReturn(strategy);

        tradingBot.start(USER_ID, STRATEGY_TYPE);

        assertTrue(tradingBot.isRunning());
    }

    @Test
    void start_shouldThrowWhenUserIdIsNull() {
        assertThrows(IllegalArgumentException.class, () -> tradingBot.start(null, STRATEGY_TYPE));
    }

    @Test
    void stop_shouldSetRunningFalse() {
        when(strategyFactory.createStrategy(STRATEGY_TYPE)).thenReturn(strategy);

        tradingBot.start(USER_ID, STRATEGY_TYPE);
        tradingBot.stop();

        assertFalse(tradingBot.isRunning());
    }

    @Test
    void isRunning_shouldReturnFalseInitially() {
        assertFalse(tradingBot.isRunning());
    }

    @Test
    void onPriceUpdate_shouldExecuteBuyWhenStrategyReturnsBuy() {
        when(strategyFactory.createStrategy(STRATEGY_TYPE)).thenReturn(strategy);
        when(tradingService.getUsdtBalance(USER_ID)).thenReturn(USDT_BALANCE);
        when(tradingService.getBtcHoldings(USER_ID)).thenReturn(BTC_HOLDINGS);
        when(strategy.evaluate(PRICE, USDT_BALANCE, BTC_HOLDINGS)).thenReturn(TradeSignal.BUY);

        tradingBot.start(USER_ID, STRATEGY_TYPE);
        tradingBot.onPriceUpdate(PRICE);
        tradingBot.evaluateLatestPrice();

        verify(tradingService).executeBuy(USER_ID, "BTCUSDT", PRICE);
    }

    @Test
    void onPriceUpdate_shouldExecuteSellWhenStrategyReturnsSell() {
        when(strategyFactory.createStrategy(STRATEGY_TYPE)).thenReturn(strategy);
        when(tradingService.getUsdtBalance(USER_ID)).thenReturn(USDT_BALANCE);
        when(tradingService.getBtcHoldings(USER_ID)).thenReturn(BTC_HOLDINGS);
        when(strategy.evaluate(PRICE, USDT_BALANCE, BTC_HOLDINGS)).thenReturn(TradeSignal.SELL);

        tradingBot.start(USER_ID, STRATEGY_TYPE);
        tradingBot.onPriceUpdate(PRICE);
        tradingBot.evaluateLatestPrice();

        verify(tradingService).executeSell(USER_ID, "BTCUSDT", PRICE);
    }

    @Test
    void onPriceUpdate_shouldNotExecuteTradeWhenStrategyReturnsHold() {
        when(strategyFactory.createStrategy(STRATEGY_TYPE)).thenReturn(strategy);
        when(tradingService.getUsdtBalance(USER_ID)).thenReturn(USDT_BALANCE);
        when(tradingService.getBtcHoldings(USER_ID)).thenReturn(BTC_HOLDINGS);
        when(strategy.evaluate(PRICE, USDT_BALANCE, BTC_HOLDINGS)).thenReturn(TradeSignal.HOLD);

        tradingBot.start(USER_ID, STRATEGY_TYPE);
        tradingBot.onPriceUpdate(PRICE);
        tradingBot.evaluateLatestPrice();

        verify(tradingService, never()).executeBuy(any(), any(), any());
        verify(tradingService, never()).executeSell(any(), any(), any());
    }

    @Test
    void evaluateLatestPrice_shouldNotProcessWhenNotRunning() {
        tradingBot.onPriceUpdate(PRICE);
        tradingBot.evaluateLatestPrice();

        verify(strategy, never()).evaluate(any(), any(), any());
    }

    @Test
    void evaluateLatestPrice_shouldNotProcessAfterStop() {
        when(strategyFactory.createStrategy(STRATEGY_TYPE)).thenReturn(strategy);

        tradingBot.start(USER_ID, STRATEGY_TYPE);
        tradingBot.onPriceUpdate(PRICE);
        tradingBot.stop();
        tradingBot.evaluateLatestPrice();

        verify(strategy, never()).evaluate(any(), any(), any());
    }

    @Test
    void evaluateLatestPrice_shouldNotProcessWhenNoPriceReceived() {
        when(strategyFactory.createStrategy(STRATEGY_TYPE)).thenReturn(strategy);

        tradingBot.start(USER_ID, STRATEGY_TYPE);
        tradingBot.evaluateLatestPrice();

        verify(strategy, never()).evaluate(any(), any(), any());
    }
}