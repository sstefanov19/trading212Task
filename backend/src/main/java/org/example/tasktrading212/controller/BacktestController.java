package org.example.tasktrading212.controller;

import jakarta.validation.Valid;
import org.example.tasktrading212.dto.BackTestRequest;
import org.example.tasktrading212.dto.BacktestResult;
import org.example.tasktrading212.model.BacktestPeriod;
import org.example.tasktrading212.model.User;
import org.example.tasktrading212.service.BackTestService;
import org.example.tasktrading212.service.UserService;
import org.example.tasktrading212.strategy.StrategyType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/backtest")
public class BacktestController {

    private final BackTestService backTestService;
    private final UserService userService;

    public BacktestController(BackTestService backTestService, UserService userService) {
        this.backTestService = backTestService;
        this.userService = userService;
    }

    @PostMapping("/run")
    public ResponseEntity<BacktestResult> runBacktest(
            @Valid @RequestBody BackTestRequest request
            ) {
        User user = userService.getCurrentUser();

        BacktestResult result = backTestService.runBacktest(
                user.getId(), request.initialBalance(), request.strategy(), request.period()
        );

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}