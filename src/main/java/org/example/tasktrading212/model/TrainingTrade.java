package org.example.tasktrading212.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrainingTrade {
    private Long id;
    private Long userId;
    private TradeType type;
    private String symbol;
    private BigDecimal price;
    private BigDecimal quantity;
    private BigDecimal total;
    private LocalDateTime timestamp;
    private String backtestId;
}