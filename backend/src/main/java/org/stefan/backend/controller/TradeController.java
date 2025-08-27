package org.stefan.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.stefan.backend.dto.TradeDto;
import org.stefan.backend.service.TradeService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/trade")
public class TradeController {

    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @PostMapping("/start")
    public ResponseEntity<String> startTrading() {
        boolean started = tradeService.startTrading();

        if(started) {
            return ResponseEntity.ok("Trading started");
        }
        return ResponseEntity.badRequest().body("Trading already running");
    }

    @PostMapping("/stop")
    public ResponseEntity<String> stopTrading() {
        boolean stoped = tradeService.stopTrading();

        if(stoped) {
            return ResponseEntity.ok("Trading has stopped");
        }
        return ResponseEntity.badRequest().body("Trading has already been stoped");
    }

    @GetMapping("/{source}")
    public ResponseEntity<List<TradeDto>> getAllTradesBySource(@PathVariable String source) {

        List<TradeDto> trades = tradeService.getAllTradesBySource(source);

        return  ResponseEntity.ok(trades);
    }

}
