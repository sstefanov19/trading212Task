package org.example.tasktrading212.service;

import org.example.tasktrading212.model.Portfolio;
import org.example.tasktrading212.repository.PortfolioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;


    public PortfolioService(PortfolioRepository portfolioRepository) {
        this.portfolioRepository = portfolioRepository;

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
    public void updatePortfolio(Long userId, BigDecimal usdtBalance, BigDecimal btcHoldings) {
        portfolioRepository.updateBalancesByUserId(userId, usdtBalance, btcHoldings);
    }
}