package org.example.tasktrading212.dto;

import org.example.tasktrading212.model.TradeType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TradeResponse(
        TradeType type,
        String symbol,
        BigDecimal price,
        BigDecimal quantity,
        BigDecimal total,
        LocalDateTime timestamp
) {}
