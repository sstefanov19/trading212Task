package org.example.tasktrading212.controller;

import jakarta.validation.Valid;
import org.example.tasktrading212.dto.TradeResponse;
import org.example.tasktrading212.dto.TradingRequest;
import org.example.tasktrading212.model.Trade;
import org.example.tasktrading212.model.User;
import org.example.tasktrading212.repository.PortfolioRepository;
import org.example.tasktrading212.service.PortfolioService;
import org.example.tasktrading212.service.TradingBot;
import org.example.tasktrading212.service.TradingService;
import org.example.tasktrading212.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/trading")
public class TradingController {

    private final TradingBot tradingBot;
    private final UserService userService;
    private final TradingService tradingService;

    public TradingController(TradingBot tradingBot, UserService userService, TradingService tradingService) {
        this.tradingBot = tradingBot;
        this.userService = userService;
        this.tradingService = tradingService;
    }

    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startTrading(@Valid  @RequestBody TradingRequest request) {
        User user = userService.getCurrentUser();
        tradingBot.start(user.getId(), request.strategy());
        return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                "status", "running",
                "strategy", request.strategy(),
                "message", "Trading bot started"
        ));
    }

    @PostMapping("/stop")
    public ResponseEntity<Map<String, Object>> stopTrading() {
        tradingBot.stop();
        return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                "status", "stopped",
                "message", "Trading bot stopped"
        ));
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                "running", tradingBot.isRunning()
        ));
    }

    @GetMapping
    public ResponseEntity<List<TradeResponse>> getTrades() {
        List<TradeResponse> trades = tradingService.getTrades();

        return ResponseEntity.status(HttpStatus.OK).body(trades);
    }
}