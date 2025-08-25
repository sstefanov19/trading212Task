package org.stefan.backend.service;

import org.springframework.stereotype.Service;
import org.stefan.backend.dto.PortfolioDto;
import org.stefan.backend.dto.PortfolioRequestDto;

import org.stefan.backend.repository.PortfolioRepository;

import java.math.BigDecimal;

@Service
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;

    public PortfolioService(PortfolioRepository portfolioRepository) {
        this.portfolioRepository = portfolioRepository;
    }

    public PortfolioDto getPortfolio(Long id) {
        return portfolioRepository.getPortfolioFromDB(id);
    }

    public void saveToPortfolio(BigDecimal balance , Double profit , int quantity , Long id) {
         portfolioRepository.updatePortfolio(
                balance,
                profit,
                quantity,
                id
        );
    }

    public Double getPortfolioProfit(Long id) {
        return portfolioRepository.getProfit(id);
    }
}
