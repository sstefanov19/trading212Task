package org.example.tasktrading212.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.example.tasktrading212.model.BacktestPeriod;
import org.example.tasktrading212.strategy.StrategyType;

import java.math.BigDecimal;

public record BackTestRequest(
        @NotBlank(message = "Initial balance cannot be blank")
        @Positive(message = "Initial balance value cannot be negative")
        BigDecimal initialBalance,

        @NotBlank(message = "Strategy cannot be blank")
        StrategyType strategy,

        @NotBlank(message = "Period cannot be blank")
        BacktestPeriod period
) {
}
