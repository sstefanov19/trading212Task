package org.stefan.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.stefan.backend.dto.BalanceRequest;
import org.stefan.backend.service.BalanceService;

@RestController
@RequestMapping("/api/v1")
public class BalanceController {


    private final BalanceService balanceService;

    public BalanceController(BalanceService balanceService) {
        this.balanceService = balanceService;
    }

    @PostMapping("/balance")
    public ResponseEntity<?> balance(@RequestBody BalanceRequest request) {

        balanceService.createNewBalance(request.total_balance());

        return ResponseEntity.status(HttpStatus.ACCEPTED).build();

    }

    @PutMapping("/balance/{id}")
    public ResponseEntity<String> updateBalance(@PathVariable Long id, @RequestBody BalanceRequest request) {
        try {
            String message = balanceService.updateBalance(id, request.total_balance());
            return ResponseEntity.ok(message);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
