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

    public PortfolioDto getPortfolioById(Long id) {
        return portfolioRepository.getPortfolioById(id);
    }

    public void updatePortfolio(BigDecimal balance , Double profit , Double quantity , Long id) {
         portfolioRepository.updatePortfolio(
                balance,
                profit,
                quantity,
                id
        );
    }

    public Double getPortfolioQuantityById(Long id) {
        return portfolioRepository.getPortfolioQuantityById(id);
    }

    public void setInitialPortfolio(BigDecimal balance, Double profit, Double quantity, Long id) {
        portfolioRepository.setInitialPortfolio(balance,profit,quantity,id);
    }
}
