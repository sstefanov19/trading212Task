package org.example.tasktrading212.controller;

import org.example.tasktrading212.dto.PortfolioResponse;
import org.example.tasktrading212.model.User;
import org.example.tasktrading212.service.PortfolioService;
import org.example.tasktrading212.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/portfolio")
public class PortfolioController {

    private final PortfolioService portfolioService;
    private final UserService userService;

    public PortfolioController(PortfolioService portfolioService, UserService userService) {
        this.portfolioService = portfolioService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<PortfolioResponse> getPortfolio() {
        User user = userService.getCurrentUser();
        return ResponseEntity.status(HttpStatus.OK).body(portfolioService.getPortfolioResponse(user.getId()));
    }
}