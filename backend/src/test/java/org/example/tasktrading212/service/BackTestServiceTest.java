package org.example.tasktrading212.service;

import org.example.tasktrading212.dto.BacktestResult;
import org.example.tasktrading212.dto.PricePoint;
import org.example.tasktrading212.model.BacktestPeriod;
import org.example.tasktrading212.model.TradeSignal;
import org.example.tasktrading212.repository.TrainingTradeRepository;
import org.example.tasktrading212.strategy.StrategyFactory;
import org.example.tasktrading212.strategy.StrategyType;
import org.example.tasktrading212.strategy.TradingStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BackTestServiceTest {

    @Mock
    private BinanceHistoryClient historyClient;

    @Mock
    private StrategyFactory strategyFactory;

    @Mock
    private TrainingTradeRepository trainingTradeRepository;

    @Mock
    private TradingStrategy strategy;

    private BackTestService backTestService;

    private static final Long USER_ID = 1L;
    private static final BigDecimal INITIAL_BALANCE = new BigDecimal("1000");

    @BeforeEach
    void setUp() {
        backTestService = new BackTestService(historyClient, strategyFactory, trainingTradeRepository);
    }

    @Test
    void shouldReturnEmptyResultWhenHistoryClientFails() {
        when(strategyFactory.createStrategy(StrategyType.MOVING_AVERAGE)).thenReturn(strategy);
        when(historyClient.getHistoricalPrices(anyString(), anyString(), anyInt()))
                .thenThrow(new RuntimeException("API error"));

        BacktestResult result = backTestService.runBacktest(USER_ID, INITIAL_BALANCE, StrategyType.MOVING_AVERAGE, BacktestPeriod.LAST_1_DAY);

        assertEquals(0, result.pricePoints());
        assertEquals(0, result.buyCount());
        assertEquals(0, result.sellCount());
        assertEquals(INITIAL_BALANCE, result.initialBalance());
        assertEquals(INITIAL_BALANCE, result.finalValue());
        assertEquals(BigDecimal.ZERO, result.profitLoss());
        assertTrue(result.portfolioOverTime().isEmpty());
    }

    @Test
    void shouldReturnEmptyResultWhenNoPriceData() {
        when(strategyFactory.createStrategy(StrategyType.MEAN_REVERSION)).thenReturn(strategy);
        when(historyClient.getHistoricalPrices(anyString(), anyString(), anyInt()))
                .thenReturn(Collections.emptyList());

        BacktestResult result = backTestService.runBacktest(USER_ID, INITIAL_BALANCE, StrategyType.MEAN_REVERSION, BacktestPeriod.LAST_10_MINUTES);

        assertEquals(0, result.pricePoints());
        assertEquals(INITIAL_BALANCE, result.finalValue());
    }

    @Test
    void shouldExecuteBuyAndSellAndCalculateProfitLoss() {
        LocalDateTime now = LocalDateTime.now();
        List<PricePoint> prices = List.of(
                new PricePoint(now, new BigDecimal("50000")),
                new PricePoint(now.plusMinutes(1), new BigDecimal("55000"))
        );

        when(strategyFactory.createStrategy(StrategyType.MOVING_AVERAGE)).thenReturn(strategy);
        when(historyClient.getHistoricalPrices(anyString(), anyString(), anyInt())).thenReturn(prices);
        when(strategy.evaluate(eq(new BigDecimal("50000")), any(), any())).thenReturn(TradeSignal.BUY);
        when(strategy.evaluate(eq(new BigDecimal("55000")), any(), any())).thenReturn(TradeSignal.SELL);

        BacktestResult result = backTestService.runBacktest(USER_ID, INITIAL_BALANCE, StrategyType.MOVING_AVERAGE, BacktestPeriod.LAST_1_DAY);

        assertEquals(2, result.pricePoints());
        assertEquals(1, result.buyCount());
        assertEquals(1, result.sellCount());
        assertTrue(result.profitLoss().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(result.finalValue().compareTo(INITIAL_BALANCE) > 0);
        verify(trainingTradeRepository, times(2)).save(any());
    }

    @Test
    void shouldCountOnlyHoldsWhenStrategyReturnsHold() {
        LocalDateTime now = LocalDateTime.now();
        List<PricePoint> prices = List.of(
                new PricePoint(now, new BigDecimal("50000")),
                new PricePoint(now.plusMinutes(1), new BigDecimal("50100")),
                new PricePoint(now.plusMinutes(2), new BigDecimal("50200"))
        );

        when(strategyFactory.createStrategy(StrategyType.MEAN_REVERSION)).thenReturn(strategy);
        when(historyClient.getHistoricalPrices(anyString(), anyString(), anyInt())).thenReturn(prices);
        when(strategy.evaluate(any(), any(), any())).thenReturn(TradeSignal.HOLD);

        BacktestResult result = backTestService.runBacktest(USER_ID, INITIAL_BALANCE, StrategyType.MEAN_REVERSION, BacktestPeriod.LAST_10_HOURS);

        assertEquals(3, result.pricePoints());
        assertEquals(0, result.buyCount());
        assertEquals(0, result.sellCount());
        assertEquals(0, result.profitLoss().compareTo(BigDecimal.ZERO));
        verify(trainingTradeRepository, never()).save(any());
    }

    @Test
    void shouldNotSellWhenNoBtcHoldings() {
        LocalDateTime now = LocalDateTime.now();
        List<PricePoint> prices = List.of(
                new PricePoint(now, new BigDecimal("50000"))
        );

        when(strategyFactory.createStrategy(StrategyType.MOVING_AVERAGE)).thenReturn(strategy);
        when(historyClient.getHistoricalPrices(anyString(), anyString(), anyInt())).thenReturn(prices);
        when(strategy.evaluate(any(), any(), any())).thenReturn(TradeSignal.SELL);

        BacktestResult result = backTestService.runBacktest(USER_ID, INITIAL_BALANCE, StrategyType.MOVING_AVERAGE, BacktestPeriod.LAST_1_DAY);

        assertEquals(0, result.sellCount());
        verify(trainingTradeRepository, never()).save(any());
    }

    @Test
    void shouldTrackPortfolioOverTime() {
        LocalDateTime now = LocalDateTime.now();
        List<PricePoint> prices = List.of(
                new PricePoint(now, new BigDecimal("50000")),
                new PricePoint(now.plusMinutes(1), new BigDecimal("55000"))
        );

        when(strategyFactory.createStrategy(StrategyType.MOVING_AVERAGE)).thenReturn(strategy);
        when(historyClient.getHistoricalPrices(anyString(), anyString(), anyInt())).thenReturn(prices);
        when(strategy.evaluate(eq(new BigDecimal("50000")), any(), any())).thenReturn(TradeSignal.BUY);
        when(strategy.evaluate(eq(new BigDecimal("55000")), any(), any())).thenReturn(TradeSignal.SELL);

        BacktestResult result = backTestService.runBacktest(USER_ID, INITIAL_BALANCE, StrategyType.MOVING_AVERAGE, BacktestPeriod.LAST_1_DAY);

        // Start point + buy snapshot + sell snapshot + end point
        assertTrue(result.portfolioOverTime().size() >= 3);
        // First point should be the initial balance
        assertEquals(0, result.portfolioOverTime().get(0).price().compareTo(INITIAL_BALANCE));
    }

    @Test
    void shouldPreserveInitialBalanceInResult() {
        LocalDateTime now = LocalDateTime.now();
        List<PricePoint> prices = List.of(new PricePoint(now, new BigDecimal("50000")));

        when(strategyFactory.createStrategy(StrategyType.MOVING_AVERAGE)).thenReturn(strategy);
        when(historyClient.getHistoricalPrices(anyString(), anyString(), anyInt())).thenReturn(prices);
        when(strategy.evaluate(any(), any(), any())).thenReturn(TradeSignal.HOLD);

        BigDecimal customBalance = new BigDecimal("5000");
        BacktestResult result = backTestService.runBacktest(USER_ID, customBalance, StrategyType.MOVING_AVERAGE, BacktestPeriod.LAST_1_DAY);

        assertEquals(customBalance, result.initialBalance());
    }
}