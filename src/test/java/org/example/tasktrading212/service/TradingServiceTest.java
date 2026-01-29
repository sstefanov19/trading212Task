package org.example.tasktrading212.service;

import org.example.tasktrading212.exceptions.ZeroBalanceException;
import org.example.tasktrading212.exceptions.ZeroBitcoinAvailableException;
import org.example.tasktrading212.model.Trade;
import org.example.tasktrading212.model.TradeType;
import org.example.tasktrading212.repository.TradeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradingServiceTest {

    @Mock
    private TradeRepository tradeRepository;

    @Mock
    private PortfolioService portfolioService;

    private TradingService tradingService;

    private static final Long USER_ID = 1L;
    private static final String SYMBOL = "BTCUSDT";

    @BeforeEach
    void setUp() {
        tradingService = new TradingService(tradeRepository, portfolioService);
    }

    @Test
    void executeBuy_shouldCalculateCorrectQuantityAndUpdatePortfolio() {
        BigDecimal usdtBalance = new BigDecimal("1000");
        BigDecimal btcHoldings = BigDecimal.ZERO;
        BigDecimal price = new BigDecimal("50000");

        when(portfolioService.getUsdtBalance(USER_ID)).thenReturn(usdtBalance);
        when(portfolioService.getBtcHoldings(USER_ID)).thenReturn(btcHoldings);

        tradingService.executeBuy(USER_ID, SYMBOL, price);
        BigDecimal expectedQuantity = new BigDecimal("0.02");

        verify(portfolioService).updatePortfolio(
                eq(USER_ID),
                eq(BigDecimal.ZERO.setScale(2)),
                eq(expectedQuantity)
        );
    }

    @Test
    void executeBuy_shouldSaveTradeWithCorrectDetails() {
        BigDecimal usdtBalance = new BigDecimal("1000");
        BigDecimal price = new BigDecimal("50000");

        when(portfolioService.getUsdtBalance(USER_ID)).thenReturn(usdtBalance);
        when(portfolioService.getBtcHoldings(USER_ID)).thenReturn(BigDecimal.ZERO);

        tradingService.executeBuy(USER_ID, SYMBOL, price);

        ArgumentCaptor<Trade> tradeCaptor = ArgumentCaptor.forClass(Trade.class);
        verify(tradeRepository).saveTrade(tradeCaptor.capture());

        Trade savedTrade = tradeCaptor.getValue();
        assertEquals(USER_ID, savedTrade.getUserId());
        assertEquals(TradeType.BUY, savedTrade.getType());
        assertEquals(SYMBOL, savedTrade.getSymbol());
        assertEquals(price, savedTrade.getPrice());
        assertNotNull(savedTrade.getTimestamp());
    }

    @Test
    void executeBuy_shouldThrowWhenNoBalance() {
        when(portfolioService.getUsdtBalance(USER_ID)).thenReturn(BigDecimal.ZERO);

        assertThrows(ZeroBalanceException.class, () ->
                tradingService.executeBuy(USER_ID, SYMBOL, new BigDecimal("50000"))
        );

        verify(tradeRepository, never()).saveTrade(any());
        verify(portfolioService, never()).updatePortfolio(any(), any(), any());
    }

    @Test
    void executeSell_shouldCalculateCorrectTotalAndUpdatePortfolio() {
        BigDecimal btcHoldings = new BigDecimal("0.02");
        BigDecimal usdtBalance = BigDecimal.ZERO;
        BigDecimal price = new BigDecimal("55000");

        when(portfolioService.getBtcHoldings(USER_ID)).thenReturn(btcHoldings);
        when(portfolioService.getUsdtBalance(USER_ID)).thenReturn(usdtBalance);

        tradingService.executeSell(USER_ID, SYMBOL, price);

        BigDecimal expectedTotal = new BigDecimal("1100.00");

        verify(portfolioService).updatePortfolio(
                eq(USER_ID),
                eq(expectedTotal),
                eq(BigDecimal.ZERO)
        );
    }

    @Test
    void executeSell_shouldSaveTradeWithCorrectDetails() {
        BigDecimal btcHoldings = new BigDecimal("0.02");
        BigDecimal price = new BigDecimal("55000");

        when(portfolioService.getBtcHoldings(USER_ID)).thenReturn(btcHoldings);
        when(portfolioService.getUsdtBalance(USER_ID)).thenReturn(BigDecimal.ZERO);

        tradingService.executeSell(USER_ID, SYMBOL, price);

        ArgumentCaptor<Trade> tradeCaptor = ArgumentCaptor.forClass(Trade.class);
        verify(tradeRepository).saveTrade(tradeCaptor.capture());

        Trade savedTrade = tradeCaptor.getValue();
        assertEquals(USER_ID, savedTrade.getUserId());
        assertEquals(TradeType.SELL, savedTrade.getType());
        assertEquals(SYMBOL, savedTrade.getSymbol());
        assertEquals(price, savedTrade.getPrice());
        assertEquals(btcHoldings, savedTrade.getQuantity());
    }

    @Test
    void executeSell_shouldThrowWhenNoBtc() {
        when(portfolioService.getBtcHoldings(USER_ID)).thenReturn(BigDecimal.ZERO);

        assertThrows(ZeroBitcoinAvailableException.class, () ->
                tradingService.executeSell(USER_ID, SYMBOL, new BigDecimal("55000"))
        );

        verify(tradeRepository, never()).saveTrade(any());
        verify(portfolioService, never()).updatePortfolio(any(), any(), any());
    }

    @Test
    void getUsdtBalance_shouldDelegateToPortfolioService() {
        BigDecimal expected = new BigDecimal("500");
        when(portfolioService.getUsdtBalance(USER_ID)).thenReturn(expected);

        BigDecimal result = tradingService.getUsdtBalance(USER_ID);

        assertEquals(expected, result);
    }

    @Test
    void getBtcHoldings_shouldDelegateToPortfolioService() {
        BigDecimal expected = new BigDecimal("0.05");
        when(portfolioService.getBtcHoldings(USER_ID)).thenReturn(expected);

        BigDecimal result = tradingService.getBtcHoldings(USER_ID);

        assertEquals(expected, result);
    }
}