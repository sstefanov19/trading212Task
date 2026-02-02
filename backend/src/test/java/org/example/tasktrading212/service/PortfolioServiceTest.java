package org.example.tasktrading212.service;

import org.example.tasktrading212.dto.PortfolioResponse;
import org.example.tasktrading212.model.Portfolio;
import org.example.tasktrading212.repository.PortfolioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PortfolioServiceTest {

    @Mock
    private PortfolioRepository portfolioRepository;

    @Mock
    private BinancePriceService binancePriceService;

    private PortfolioService portfolioService;

    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        portfolioService = new PortfolioService(portfolioRepository, binancePriceService);
    }

    @Test
    void createPortfolio_shouldSaveWithDefaultBalance() {
        portfolioService.createPortfolio(USER_ID);

        verify(portfolioRepository).save(any(Portfolio.class));
    }

    @Test
    void getPortfolioByUserId_shouldReturnPortfolio() {
        Portfolio portfolio = new Portfolio(USER_ID, new BigDecimal("1000"));
        when(portfolioRepository.findByUserId(USER_ID)).thenReturn(Optional.of(portfolio));

        Portfolio result = portfolioService.getPortfolioByUserId(USER_ID);

        assertEquals(USER_ID, result.getUserId());
        assertEquals(new BigDecimal("1000"), result.getInitialBalance());
    }

    @Test
    void getPortfolioByUserId_shouldThrowWhenNotFound() {
        when(portfolioRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () ->
                portfolioService.getPortfolioByUserId(USER_ID)
        );
    }

    @Test
    void getPortfolioResponse_shouldCalculateProfitLossCorrectly() {
        Portfolio portfolio = new Portfolio(1L, USER_ID, new BigDecimal("500"), new BigDecimal("0.01"), new BigDecimal("1000"));
        when(portfolioRepository.findByUserId(USER_ID)).thenReturn(Optional.of(portfolio));
        when(binancePriceService.getCurrentPrice()).thenReturn(new BigDecimal("60000"));

        PortfolioResponse response = portfolioService.getPortfolioResponse(USER_ID);

        // currentValue = 500 + (0.01 * 60000) = 500 + 600 = 1100
        assertEquals(0, new BigDecimal("1100.00").compareTo(response.currentValue()));
        // profitLoss = 1100 - 1000 = 100
        assertEquals(0, new BigDecimal("100.00").compareTo(response.profitLoss()));
        // profitLossPercent = (100 / 1000) * 100 = 10.00
        assertEquals(0, new BigDecimal("10.00").compareTo(response.profitLossPercent()));
    }

    @Test
    void getPortfolioResponse_shouldHandleNullPrice() {
        Portfolio portfolio = new Portfolio(1L, USER_ID, new BigDecimal("1000"), new BigDecimal("0.01"), new BigDecimal("1000"));
        when(portfolioRepository.findByUserId(USER_ID)).thenReturn(Optional.of(portfolio));
        when(binancePriceService.getCurrentPrice()).thenReturn(null);

        PortfolioResponse response = portfolioService.getPortfolioResponse(USER_ID);

        // With null price, BTC value = 0, currentValue = 1000 + 0 = 1000
        assertEquals(0, new BigDecimal("1000.00").compareTo(response.currentValue()));
        assertEquals(0, BigDecimal.ZERO.compareTo(response.profitLoss()));
    }

    @Test
    void getPortfolioResponse_shouldCalculateNegativeProfitLoss() {
        Portfolio portfolio = new Portfolio(1L, USER_ID, new BigDecimal("400"), new BigDecimal("0.01"), new BigDecimal("1000"));
        when(portfolioRepository.findByUserId(USER_ID)).thenReturn(Optional.of(portfolio));
        when(binancePriceService.getCurrentPrice()).thenReturn(new BigDecimal("50000"));

        PortfolioResponse response = portfolioService.getPortfolioResponse(USER_ID);

        // currentValue = 400 + (0.01 * 50000) = 400 + 500 = 900
        assertEquals(0, new BigDecimal("900.00").compareTo(response.currentValue()));
        // profitLoss = 900 - 1000 = -100
        assertTrue(response.profitLoss().compareTo(BigDecimal.ZERO) < 0);
    }

    @Test
    void getUsdtBalance_shouldReturnCorrectBalance() {
        Portfolio portfolio = new Portfolio(USER_ID, new BigDecimal("1000"));
        when(portfolioRepository.findByUserId(USER_ID)).thenReturn(Optional.of(portfolio));

        BigDecimal result = portfolioService.getUsdtBalance(USER_ID);

        assertEquals(new BigDecimal("1000"), result);
    }

    @Test
    void getBtcHoldings_shouldReturnCorrectHoldings() {
        Portfolio portfolio = new Portfolio(USER_ID, new BigDecimal("1000"));
        when(portfolioRepository.findByUserId(USER_ID)).thenReturn(Optional.of(portfolio));

        BigDecimal result = portfolioService.getBtcHoldings(USER_ID);

        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void resetPortfolio_shouldResetToInitialBalance() {
        Portfolio portfolio = new Portfolio(1L, USER_ID, new BigDecimal("500"), new BigDecimal("0.01"), new BigDecimal("1000"));
        when(portfolioRepository.findByUserId(USER_ID)).thenReturn(Optional.of(portfolio));

        portfolioService.resetPortfolio(USER_ID);

        verify(portfolioRepository).resetByUserId(USER_ID, new BigDecimal("1000"));
    }

    @Test
    void updatePortfolio_shouldDelegateToRepository() {
        BigDecimal newUsdt = new BigDecimal("800");
        BigDecimal newBtc = new BigDecimal("0.004");

        portfolioService.updatePortfolio(USER_ID, newUsdt, newBtc);

        verify(portfolioRepository).updateBalancesByUserId(USER_ID, newUsdt, newBtc);
    }
}