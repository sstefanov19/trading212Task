package org.example.tasktrading212.service;

import org.example.tasktrading212.dto.PortfolioResponse;
import org.example.tasktrading212.model.Portfolio;
import org.example.tasktrading212.repository.PortfolioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final BinancePriceService binancePriceService;

    public PortfolioService(PortfolioRepository portfolioRepository, BinancePriceService binancePriceService) {
        this.portfolioRepository = portfolioRepository;
        this.binancePriceService = binancePriceService;
    }

    public PortfolioResponse getPortfolioResponse(Long userId) {
        Portfolio portfolio = getPortfolioByUserId(userId);

        BigDecimal currentPrice = binancePriceService.getCurrentPrice();
        if (currentPrice == null) {
            currentPrice = BigDecimal.ZERO;
        }

        BigDecimal btcValue = portfolio.getBtcHoldings().multiply(currentPrice);
        BigDecimal currentValue = portfolio.getUsdtBalance().add(btcValue);
        BigDecimal profitLoss = currentValue.subtract(portfolio.getInitialBalance());
        BigDecimal profitLossPercent = portfolio.getInitialBalance().compareTo(BigDecimal.ZERO) > 0
                ? profitLoss.divide(portfolio.getInitialBalance(), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"))
                : BigDecimal.ZERO;

        return new PortfolioResponse(
                portfolio.getUserId(),
                portfolio.getUsdtBalance(),
                portfolio.getBtcHoldings(),
                portfolio.getInitialBalance(),
                currentValue.setScale(2, RoundingMode.HALF_UP),
                profitLoss.setScale(2, RoundingMode.HALF_UP),
                profitLossPercent.setScale(2, RoundingMode.HALF_UP));
    }

    public void createPortfolio(Long userId) {
        Portfolio portfolio = new Portfolio(userId,new BigDecimal(1000)); // for testing purposes
        portfolioRepository.save(portfolio);
    }

    public Portfolio getPortfolioByUserId(Long userId) {
        return portfolioRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Portfolio not found for user: " + userId));
    }

    public BigDecimal getUsdtBalance(Long userId) {
        return getPortfolioByUserId(userId).getUsdtBalance();
    }

    public BigDecimal getBtcHoldings(Long userId) {
        return getPortfolioByUserId(userId).getBtcHoldings();
    }

    @Transactional
    public void resetPortfolio(Long userId) {
        Portfolio portfolio = getPortfolioByUserId(userId);
        portfolioRepository.resetByUserId(userId, portfolio.getInitialBalance());
    }

    @Transactional
    public void updatePortfolio(Long userId, BigDecimal usdtBalance, BigDecimal btcHoldings) {
        portfolioRepository.updateBalancesByUserId(userId, usdtBalance, btcHoldings);
    }
}