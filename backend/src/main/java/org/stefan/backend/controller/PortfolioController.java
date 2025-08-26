package org.stefan.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.stefan.backend.dto.PortfolioDto;
import org.stefan.backend.dto.PortfolioRequestDto;
import org.stefan.backend.service.PortfolioService;

@RestController
@RequestMapping("/api/v1")
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @GetMapping("/portfolio/{id}")
    public ResponseEntity<?> getPortfolioById(@PathVariable Long id) {
        PortfolioDto getPortfolio =  portfolioService.getPortfolioById(id);

        return ResponseEntity.ok(getPortfolio);
    }

    @PutMapping("/portfolio/{id}")
    public ResponseEntity<String> updatePortfolio(@PathVariable Long id , @RequestBody PortfolioRequestDto request) {

        portfolioService.updatePortfolio(
                request.balance(),
                request.profit(),
                request.quantity(),
                id
        );

        return ResponseEntity.ok("Portfolio updated!");
    }
}

