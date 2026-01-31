package org.example.tasktrading212.dto;

import jakarta.validation.constraints.NotBlank;
import org.example.tasktrading212.strategy.StrategyType;

public record TradingRequest(

        @NotBlank(message = "Strategy cannot be blank")
        StrategyType strategy
){
}
