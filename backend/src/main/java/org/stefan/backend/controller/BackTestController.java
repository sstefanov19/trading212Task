package org.stefan.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.stefan.backend.service.BackTestService;

@RestController
@RequestMapping("/api/v1/backtest")
public class BackTestController {

    private final BackTestService backTestService;

    public BackTestController(BackTestService backTestService) {
        this.backTestService = backTestService;
    }

    @PostMapping("/{symbol}")
    public ResponseEntity<String> runBackTest(@PathVariable String symbol) {
        backTestService.runBackTest(symbol);
        return ResponseEntity.ok("Backtest finished for: " + symbol);
    }

}
