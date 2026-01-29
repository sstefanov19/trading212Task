package org.example.tasktrading212.controller;

import org.example.tasktrading212.model.User;
import org.example.tasktrading212.service.TradingBot;
import org.example.tasktrading212.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/trading")
public class TradingController {

    private final TradingBot tradingBot;
    private final UserService userService;

    public TradingController(TradingBot tradingBot, UserService userService) {
        this.tradingBot = tradingBot;
        this.userService = userService;
    }

    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startTrading() {
        User user = userService.getCurrentUser();
        tradingBot.start(user.getId());
        return ResponseEntity.ok(Map.of(
                "status", "running",
                "message", "Trading bot started - will buy/sell based on strategy"
        ));
    }

    @PostMapping("/stop")
    public ResponseEntity<Map<String, Object>> stopTrading() {
        tradingBot.stop();
        return ResponseEntity.ok(Map.of(
                "status", "stopped",
                "message", "Trading bot stopped"
        ));
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(Map.of(
                "running", tradingBot.isRunning()
        ));
    }
}