package org.example.tasktrading212.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PricePoint(LocalDateTime timestamp, BigDecimal price) {}
